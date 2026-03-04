import ast
import datetime
import json
import os
import re

import tiktoken

from constant import KG_ENTITIES, RESOURCE_LABELS, INPUT_MAX_TOKEN
from .prompts import metric_user


def read_json(file_path):
    with open(file_path, 'r') as file:
        data = json.load(file)
    return data


def write_json(data, file_path):
    st = datetime.datetime.now().timestamp()
    os.makedirs(os.path.dirname(file_path), exist_ok=True)
    with open(file_path, 'w') as file:
        json.dump(data, file, indent=2)
    return datetime.datetime.now().timestamp() - st


def chat_complete(llm, model, messages, temperature):
    st = datetime.datetime.now().timestamp()
    encoding = tiktoken.get_encoding('cl100k_base')
    tokens = sum([len(encoding.encode(msg['content'])) for msg in messages])
    if tokens > INPUT_MAX_TOKEN:
        raise RuntimeError(f'too many tokens: {tokens}. (should <= {INPUT_MAX_TOKEN})')

    response = llm.chat.completions.create(
        model=model,
        messages=messages,
        temperature=temperature
    )
    response_text = response.choices[0].message.content
    ed = datetime.datetime.now().timestamp()
    return response_text, ed - st


def post_process(meta_paths):
    result = []
    for path in meta_paths:
        if not path:
            continue
        if path[0][1] != '?':
            result.append(path)
        else:
            # start with '?'
            first_mention_idx = -1
            for i in range(len(path)):
                if path[i][1] != '?' and path[i][0] in KG_ENTITIES:
                    first_mention_idx = i
                    break
            # all '?', throw away
            if first_mention_idx == -1:
                continue
            # not all '?', divide into left and right
            left = list(reversed(path[:first_mention_idx + 1]))
            for item in left:
                if item[1] == '->':
                    item[1] = '<-'
                elif item[1] == '<-':
                    item[1] = '->'
            right = path[first_mention_idx:]
            result.append(left)
            result.append(right)
    return result


def extract_path_response(response_text):
    match_results = re.findall(r"```(.*?)```", response_text, re.DOTALL)
    if len(match_results) == 0:
        return ''
    # assert len(match_results) == 1, f'[extract_path_response] error: find multiple or zero ```(.*?)``` in \n{response_text}\n'
    return match_results[0].strip()


def extract_metric_response(response_text):
    match_results = re.findall(r"```(.*?)```", response_text, re.DOTALL)
    if len(match_results) == 0:
        return ''
    # assert len(match_results) == 1, f'[extract_metric_response] error: find multiple or zero ```(.*?)``` in \n{response_text}\n'
    return match_results[0].strip()


def extract_lvp_response(response_text):
    match_results = re.findall(r"```(.*?)```", response_text, re.DOTALL)
    # assert len(match_results) == 1, f'[extract_lvp_response] error: find multiple or zero ```(.*?)``` in \n{response_text}\n'
    return match_results[0].strip()


def extract_promql_response(response_text):
    match_results = re.findall(r"```[^\n]*\n(.*?)```", response_text, re.DOTALL)
    return [result for result in match_results]


def parse_metrics_description(response_text):
    return ast.literal_eval(response_text)

def parse_path_extracted_text(extracted_text):
    pattern = r'\(([^:]+):\s*([^)]+)\)|--\s*([\w]+)\s*->|<-\s*([\w]+)\s*--'

    meta_paths = []
    for line in extracted_text.split('\n'):
        matches = re.findall(pattern, line.strip())
        path = []
        for match in matches:
            if match[0]:
                path.append([match[0].strip(), match[1].strip()])
            elif match[2]:
                path.append([match[2].strip(), '->'])
            elif match[3]:
                path.append([match[3].strip(), '<-'])
        meta_paths.append(path)

    return meta_paths


def parse_metric_extracted_text(extracted_text):
    pattern = re.compile(r'- ({.*?})')
    matches = pattern.findall(extracted_text)
    try:
        result = [json.loads(metric) for metric in matches]
    except json.decoder.JSONDecodeError:
        return []
    return result


def parse_lvp_extracted_text(extracted_text):
    lines = extracted_text.split('\n')
    label_mentions = {}
    for line in lines:
        if ':' in line and '# Not Found' not in line:
            label, mentions = line.split(':', 1)
            label = label.strip().strip('-').strip()
            mentions = mentions.strip()
            if mentions != '[]':
                items = mentions.strip('[] ').replace('"', '').split(',')
                label_mentions[label] = [item.strip() for item in items if item.strip()]
    return label_mentions


def retrieve_desc_entities(ess, meta_paths, top_k, match_type):
    desc_entities = {(item[0], item[1]): []
                     for path in meta_paths for item in path if item[0] in KG_ENTITIES and item[1] != '?'}
    for entity_type, description in desc_entities:
        desc_entities[(entity_type, description)] += ess.search(entity_type, description, top_k, match_type)
    return desc_entities


def retrieve_lvp_entities(ess, label, mention, top_k, match_type):
    return [result['_source'] for result in
            ess.scoped_search('label_value_pair', mention, top_k, 'label.keyword', [label], match_type)]


def link_metric_lvps(metric, lvp_lst):
    lvp_lst = [lvp for lvp in lvp_lst if lvp['label'] not in RESOURCE_LABELS]
    return [[
        {
            'type': 'metric',
            'properties': metric
        },
        {
            'type': 'has',
            'start_node_type': 'metric',
            'end_node_type': 'label_value_pair',
            'reverse': False
        },
        {
            'type': 'label_value_pair',
            'properties': lvp
        }
    ] for lvp in lvp_lst]


def explore_kg(kgs, meta_paths, desc_entities):
    expand_details = []
    kg_paths = []
    for meta_path in meta_paths:
        sequence = ','.join([item[0] for item in meta_path])
        start_entity_type = meta_path[0][0]
        if (meta_path[0][0], meta_path[0][1]) not in desc_entities:
            continue  # metric
        start_entities = desc_entities[(meta_path[0][0], meta_path[0][1])]
        for start_entity in start_entities:
            cypher, path_sequences = kgs.path_expand(start_entity_type, start_entity['_source']['name'], sequence)
            expand_details.append({'cypher': cypher, 'meta_path': meta_path})
            kg_paths.append(path_sequences)
    return expand_details, kg_paths


def check_kg_path(meta_path, path, desc_entities):
    for i in range(len(path)):
        meta_item, item = tuple(meta_path[i]), path[i]
        if meta_item in desc_entities:
            out_of_scope = True
            for entity in desc_entities[meta_item]:
                if entity['_source']['name'] == item['properties']['name']:
                    out_of_scope = False
                    break
            if out_of_scope:
                return False
        else:
            if item['type'] == 'requests' or item['type'] == 'calls':
                if (item['reverse'] and meta_item[1] == '->') or (not item['reverse'] and meta_item[1] == '<-'):
                    return False
    return True


def filter_kg_paths(expand_details, kg_paths, desc_entities):
    result = []
    for expand_detail, paths in zip(expand_details, kg_paths):
        meta_path = expand_detail['meta_path']
        max_len = max(len(path) for path in paths)
        for path in paths:
            if len(path) != max_len:
                continue
            if check_kg_path(meta_path, path, desc_entities):
                result.append(path)
    return result


def build_metric_user_msg(retrieved_metrics, query):
    related_metrics = {item['_source']['name']: item['_source']
                       for result in retrieved_metrics.values() for item in result}
    scores = sorted([(item['_score'], item['_source']['name'])
                     for result in retrieved_metrics.values() for item in result], reverse=True)
    metrics_list = []
    used = set()
    for _, metric in scores:
        if metric in used:
            continue
        used.add(metric)
        m = related_metrics[metric]
        metrics_list.append(
            "- " + str({"name": m["name"], "type": m["type"], "description": m["description"]}).replace(chr(39),
                                                                                                        chr(34)))
    metrics_list_str = chr(10).join(metrics_list)
    return metric_user.format(query=query, metrics_list=metrics_list_str)


def entity_rep(entity):
    if entity['type'] == 'api' and 'description' in entity['properties']:
        return f'{entity["properties"]["name"]} {{"description": "{entity["properties"]["description"]}"}}'
    else:
        return entity['properties']['name']


def triple_str(src, rel, dst, reverse):
    if reverse:
        src, dst = dst, src
    return f'- ({src["type"]}: {entity_rep(src)})' \
           f' --{rel["type"]}-> ' \
           f'({dst["type"]}: {entity_rep(dst)})'


def extract_triples(paths):
    m_triples, lvp_triples, o_triples = set(), set(), set()
    for path in paths:
        for i in range(1, len(path), 2):
            if path[i]['reverse']:
                start_entity_type = path[i + 1]['type']
            else:
                start_entity_type = path[i - 1]['type']
            if start_entity_type == 'metric':
                m_triples.add(triple_str(path[i - 1], path[i], path[i + 1], path[i]['reverse']))
            elif start_entity_type == 'label_value_pair':
                lvp_triples.add(triple_str(path[i - 1], path[i], path[i + 1], path[i]['reverse']))
            else:
                o_triples.add(triple_str(path[i - 1], path[i], path[i + 1], path[i]['reverse']))
    return sorted(m_triples), sorted(lvp_triples), sorted(o_triples)


def build_lvp_user_msg(query, metric, kgs):
    _, lvp_lst = kgs.get_metric_labels_examples(metric['name'])
    labels = [lvp.split('=')[0] for lvp in lvp_lst]
    if len(labels) == 0:
        labels_str = ''
    elif len(labels) == 1:
        labels_str = labels[0]
    else:
        labels_str = f'{", ".join(labels[:-1])} and {labels[-1]}'

    usr_msg = ['1. Metric Information:',
               json.dumps(metric),
               '2. Metric Labels:',
               f'The prometheus metric `{metric["name"]}` has {len(labels)} types of labels: {labels_str}. '
               f'Below are examples of these {len(labels)} label types '
               f'(For each label, provide an example of a possible value):',
               '```']
    usr_msg.extend([f'- {lvp.split("=")[0]}: {lvp}' for lvp in lvp_lst])
    usr_msg.append('```')
    usr_msg.append(f'3. Question: {query}\n')
    return '\n'.join(usr_msg)
