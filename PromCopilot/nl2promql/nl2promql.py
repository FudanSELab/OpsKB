import copy

from openai import OpenAI

from db import ESSearcher, KGSearcher
from .prompts import path_prompt, metric_prompt, lvp_prompt, promql_prompt, md_prompt
from .utils import *


class NL2PromQL:
    def __init__(self, configs):
        self.configs = copy.deepcopy(configs)
        self.llm = OpenAI(api_key=configs['llm']['api_key'], base_url=configs['llm']['base_url'])
        self.ess = ESSearcher(configs['es'])
        self.kgs = KGSearcher(configs['neo4j'])
        self.metrics_components = None

    def __enter__(self):
        self.kgs.connect()
        self.metrics_components = self.load_metrics_components()
        return self

    def __exit__(self, exc_type, exc_val, exc_tb):
        self.kgs.close()

    def load_metrics_components(self):
        data_path = self.configs['metrics_components']['data_path']
        if os.path.exists(data_path):
            return read_json(data_path)

        _, metrics_names = self.kgs.get_all_metrics()
        metrics_components = {name: [] for name in metrics_names}
        step = self.configs['metrics_components']['batch_size']
        for i in range(0, len(metrics_names), step):
            _, batch = self.kgs.get_reachable_node_types(metrics_names[i: i + step], 2)
            metrics_components.update(batch)

        component_metrics = {}
        for name, components in metrics_components.items():
            for c in components:
                if c not in component_metrics:
                    component_metrics[c] = []
                component_metrics[c].append(name)

        write_json(component_metrics, data_path)
        return component_metrics

    def complete(self, complete_configs):
        assert complete_configs['query'] and complete_configs['record_directory'] and complete_configs['time_id']
        st = datetime.datetime.now().timestamp()
        query = complete_configs['query']
        record_directory = os.path.join(complete_configs['record_directory'], complete_configs['time_id'])

        explore_path_result, path_api_duration, path_write_json_duration = self.explore_path(query, record_directory)
        metrics_description, md_api_duration, md_write_json_duration = self.get_metrics_description(query,
                                                                                                    record_directory)
        relevant_metrics, metric_api_duration, metric_write_json_duration = self.get_relevant_metrics(
            metrics_description, query, record_directory)
        metrics_connect_paths = self.metrics_connect(explore_path_result, relevant_metrics)
        lvp_paths, lvp_api_duration, lvp_write_json_duration = self.get_lvp_paths(query, relevant_metrics,
                                                                                  record_directory)

        promql, promql_api_duration, promql_write_json_duration = self.get_promql(query,
                                                                                  relevant_metrics,
                                                                                  explore_path_result,
                                                                                  metrics_connect_paths,
                                                                                  lvp_paths,
                                                                                  record_directory)
        ed = datetime.datetime.now().timestamp()
        write_json_results_duration = path_write_json_duration + md_write_json_duration + metric_write_json_duration + \
                                      lvp_write_json_duration + promql_write_json_duration
        write_json({
            'total': ed - st,
            'total_except_api': ed - st - write_json_results_duration - path_api_duration - md_api_duration - metric_api_duration - lvp_api_duration - promql_api_duration,
            'write_json_results_duration': write_json_results_duration,
            'path_api_duration': path_api_duration,
            'md_api_duration': md_api_duration,
            'metric_api_duration': metric_api_duration,
            'lvp_api_duration': lvp_api_duration,
            'promql_api_duration': promql_api_duration
        }, os.path.join(record_directory, 'time.json'))
        return promql

    def get_metrics_description(self, query, record_directory):
        model = self.configs['md']['llm']['model']
        messages = md_prompt + [{'role': 'user', 'content': f'Question: {query}'}]
        temperature = self.configs['md']['llm']['temperature']
        response_text, api_duration = chat_complete(self.llm, model, messages, temperature)
        metrics_description = parse_metrics_description(response_text)

        write_json_duration = write_json({
            'metrics_description': metrics_description,
            'response_text': response_text
        }, os.path.join(record_directory, f'{model}_{self.configs["md"]["record_file"]}'))
        return metrics_description, api_duration, write_json_duration

    def explore_path(self, query, record_directory):
        model = self.configs['path']['llm']['model']
        messages = path_prompt + [{'role': 'user', 'content': f'Query: {query}'}]
        temperature = self.configs['path']['llm']['temperature']
        response_text, api_duration = chat_complete(self.llm, model, messages, temperature)

        extracted_text = extract_path_response(response_text)
        extracted_meta_paths = parse_path_extracted_text(extracted_text)
        meta_paths = post_process(extracted_meta_paths)

        # (entity_type, description) -> []
        top_k, match_type = self.configs['path']['es']['top_k'], self.configs['path']['es']['match_type']
        desc_entities = retrieve_desc_entities(self.ess, meta_paths, top_k, match_type)

        expand_details, kg_paths = explore_kg(self.kgs, meta_paths, desc_entities)
        filtered_kg_paths = filter_kg_paths(expand_details, kg_paths, desc_entities)

        explore_path_result = {
            'query': query,
            'meta_paths': meta_paths,
            'desc_entities': {str(k): v for k, v in desc_entities.items()},
            'extracted_meta_paths': extracted_meta_paths,
            'filtered_kg_paths': filtered_kg_paths,
            'kg_paths': kg_paths,  # [[path1, path2, ...], [...], [...], ...]
            'expand_details': expand_details,  # [{cypher, meta_path}, {...}, {...}, ...],
            'details': {
                'extracted_text': extracted_text,
                'response_text': response_text,
                'model': model,
                'temperature': temperature,
                'messages': messages
            }
        }
        write_json_duration = write_json(explore_path_result,
                                         os.path.join(record_directory,
                                                      f'{model}_{self.configs["path"]["record_file"]}'))
        return explore_path_result, api_duration, write_json_duration

    def get_relevant_metrics(self, metrics_description, query, record_directory):
        metrics_description = set(metrics_description)
        if len(metrics_description) == 0:
            metrics_description = {query}
        top_k, match_type = self.configs['metric']['es']['top_k'], self.configs['metric']['es']['match_type']

        retrieved_metrics = {}
        for description, component_text in metrics_description:
            component = component_text.lower()
            if component not in self.metrics_components:
                retrieved_metrics[str((description, 'all'))] = self.ess.search('metric', description, top_k, match_type)
            else:
                retrieved_metrics[str((description, component))] = self.ess.scoped_search('metric', description, top_k,
                                                                                          'name.keyword',
                                                                                          self.metrics_components[
                                                                                              component],
                                                                                          match_type)

        metric_user_msg = build_metric_user_msg(retrieved_metrics, query)
        model = self.configs['metric']['llm']['model']
        messages = metric_prompt + [{'role': 'user', 'content': metric_user_msg}]
        temperature = self.configs['metric']['llm']['temperature']
        response_text, api_duration = chat_complete(self.llm, model, messages, temperature)
        extracted_text = extract_metric_response(response_text)
        relevant_metrics = parse_metric_extracted_text(extracted_text)

        get_relevant_metrics_result = {
            'relevant_metrics': relevant_metrics,
            'details': {
                'extracted_text': extracted_text,
                'response_text': response_text,
                'model': model,
                'temperature': temperature,
                'messages': messages,
                'retrieved_metrics': retrieved_metrics,
            }
        }

        write_json_duration = write_json(get_relevant_metrics_result, os.path.join(record_directory,
                                                                                   f'{model}_{self.configs["metric"]["record_file"]}'))
        return relevant_metrics, api_duration, write_json_duration

    def metrics_connect(self, explore_path_result, relevant_metrics):
        entities = {}

        desc_entities = explore_path_result['desc_entities']
        for desc, results in desc_entities.items():
            et = eval(desc)[0]
            for item in results:
                if et not in entities:
                    entities[et] = set()
                entities[et].add(item['_source']['name'])

        filtered_kg_paths = explore_path_result['filtered_kg_paths']
        for path in filtered_kg_paths:
            for item in path:
                if 'properties' in item:
                    if item['type'] not in entities:
                        entities[item['type']] = set()
                    entities[item['type']].add(item['properties']['name'])

        metric_names = {m['name'] for m in relevant_metrics}
        metrics_connect_paths = []
        for metric in metric_names:
            _, metric_expand_paths = self.kgs.metric_expand(metric, entities)
            metrics_connect_paths.extend(metric_expand_paths)
        return metrics_connect_paths

    def get_promql(self, query, relevant_metrics, explore_path_result, metrics_connect_paths, lvp_paths,
                   record_directory):
        user_msg = ['1. Related metrics:']
        user_msg += sorted(
            {f'- {m["name"]}: type: {m["type"]}, description: {m["description"]}' for m in relevant_metrics})

        user_msg.append('2. Domain knowledge:')
        paths = explore_path_result['filtered_kg_paths'] + [path for path in metrics_connect_paths
                                                            if len(path) == 5] + lvp_paths
        m_triples, lvp_triples, o_triples = extract_triples(paths)
        user_msg += m_triples
        user_msg += lvp_triples
        user_msg += o_triples

        user_msg.append(f'3. Question: {query}')

        promql_user = '\n'.join(user_msg)

        messages = promql_prompt + [{'role': 'user', 'content': promql_user}]

        model = self.configs['promql']['llm']['model']
        temperature = self.configs['promql']['llm']['temperature']
        response_text, api_duration = chat_complete(self.llm, model, messages, temperature)
        promql = extract_promql_response(response_text)
        write_json_duration = write_json({
            'promql': promql,
            'response_text': response_text,
            'messages': messages,
        }, os.path.join(record_directory, f'{model}_{self.configs["promql"]["record_file"]}'))
        return promql, api_duration, write_json_duration

    def get_lvp_paths(self, query, relevant_metrics, record_directory):
        model = self.configs['lvp']['llm']['model']
        temperature = self.configs['lvp']['llm']['temperature']
        top_k, match_type = self.configs['lvp']['es']['top_k'], self.configs['lvp']['es']['match_type']

        lvp_paths = []
        api_duration = 0
        for metric in relevant_metrics:
            lvp_user = build_lvp_user_msg(query, metric, self.kgs)
            messages = lvp_prompt + [{'role': 'user', 'content': lvp_user}]
            response_text, m_duratioon = chat_complete(self.llm, model, messages, temperature)
            api_duration += m_duratioon
            extracted_text = extract_lvp_response(response_text)
            label_mentions = parse_lvp_extracted_text(extracted_text)
            lvp_lst = []
            for label, mentions in label_mentions.items():
                for mention in mentions:
                    lvp_lst.extend(retrieve_lvp_entities(self.ess, label, mention, top_k, match_type))

            lvp_paths.extend(link_metric_lvps(metric, lvp_lst))

        write_json_duration = write_json(lvp_paths, os.path.join(record_directory,
                                                                 f'{model}_{self.configs["lvp"]["record_file"]}'))
        return lvp_paths, api_duration, write_json_duration
