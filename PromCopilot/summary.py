import argparse
import json
import os
from ast import literal_eval

import pandas as pd


def write_json(data, file_path):
    if os.path.exists(file_path):
        print(f'skip exists: {file_path}')
        return
    with open(file_path, 'w') as file:
        json.dump(data, file, indent=2)
    print(f'write json: {file_path}')


def read_json(file_path):
    with open(file_path, 'r') as file:
        data = json.load(file)
    return data


def rq1(base_dir, model):
    dirs = sorted([os.path.join(base_dir, item, model) for item in os.listdir(base_dir)
                   if os.path.isdir(os.path.join(base_dir, item))])
    data = {}
    for dir_path in dirs:
        path_details_path = os.path.join(dir_path, f'{model}_path.json')
        metrics_details_path = os.path.join(dir_path, f'{model}_metric.json')
        lvp_details_path = os.path.join(dir_path, f'{model}_lvp.json')
        promql_details_path = os.path.join(dir_path, f'{model}_promql_prompt.json')
        if not os.path.exists(path_details_path) or not os.path.exists(metrics_details_path) \
                or not os.path.exists(lvp_details_path) or not os.path.exists(promql_details_path):
            continue

        path_details = read_json(path_details_path)
        metrics_details = read_json(metrics_details_path)
        lvp_details = read_json(lvp_details_path)
        promql_details = read_json(promql_details_path)

        idx = dir_path.split('/')[-2]
        question_id = f'{idx}. {path_details["query"]}'
        data[question_id] = {
            'idx': idx,
            'question': path_details['query'],
            'metrics': [m['name'] for m in metrics_details['relevant_metrics']],
            'lvp': [lvp[2]['properties']['name'] for lvp in lvp_details],
            'promql': promql_details['promql'],
            'metrics_right': '_',  # human label in the future: 'T' or 'F'
            'promql_syntax_valid': '_',  # human label in the future: 'T' or 'F'
            'promql_right': '_',  # human label in the future: 'T' or 'F'
            'result_right': '_',  # human label in the future: 'T' or 'F'
        }
    write_json(data, os.path.join(base_dir, f'{model}_result_rq1.json'))


def cal(retrieved, all_relevant):
    precision = len(retrieved & all_relevant) / len(retrieved) if len(retrieved) != 0 else 0
    recall = len(retrieved & all_relevant) / len(all_relevant) if len(all_relevant) != 0 else 1
    return {
        'precision': precision,
        'recall': recall,
        'f1': 2 * (precision * recall) / (precision + recall) if (precision + recall) > 0 else 0,
    }


def accuracy_detail(ground_truth, last_user_prompt):
    retrieved_metrics = {line.split(':')[0][2:] for line in
                         last_user_prompt.split('2. Domain knowledge:')[0][len('1. Related metrics:'):].strip().split(
                             '\n')}
    all_relevant_metrics = set(literal_eval(ground_truth['metrics']))

    knowledge_st, knowledge_ed = last_user_prompt.index('2. Domain knowledge:') + len(
        '2. Domain knowledge:'), last_user_prompt.index('3. Question')
    retrieved_triples = {line[2:].strip() for line in last_user_prompt[knowledge_st: knowledge_ed].strip().split('\n')}
    all_relevant_triples = set([item[2:].strip() for item in literal_eval(ground_truth['triples'])])

    return {
        'metrics': cal(retrieved_metrics, all_relevant_metrics),
        'triples': cal(retrieved_triples, all_relevant_triples)
    }


def rq2(base_dir, model, qa_path):
    qa_df = pd.read_csv(qa_path)
    qa_dict = {}
    for _, row in qa_df.iterrows():
        qa_dict[int(row['idx'])] = {
            'metrics': row['metrics'],
            'triples': row['triples']
        }

    dirs = sorted([os.path.join(base_dir, item, model) for item in os.listdir(base_dir)
                   if os.path.isdir(os.path.join(base_dir, item))])
    data = {
        'details': [],
    }
    for dir_path in dirs:
        idx = int(dir_path.split('/')[-2])
        promql_details = read_json(os.path.join(dir_path, f'{model}_promql_prompt.json'))
        last_user_prompt = promql_details['messages'][-1]['content']
        data['details'].append(accuracy_detail(qa_dict[idx], last_user_prompt))

    metrics = {
        'precision': 0,
        'recall': 0,
        'f1': 0
    }
    triples = {
        'precision': 0,
        'recall': 0,
        'f1': 0
    }
    cnt = 0
    for item in data['details']:
        cnt += 1
        for res in ('precision', 'recall', 'f1'):
            metrics[res] += item['metrics'][res]
            triples[res] += item['triples'][res]

    data['avg'] = {
        'metrics': {
            'precision': metrics['precision'] / cnt,
            'recall': metrics['recall'] / cnt,
            'f1': metrics['f1'] / cnt
        },
        'triples': {
            'precision': triples['precision'] / cnt,
            'recall': triples['recall'] / cnt,
            'f1': triples['f1'] / cnt
        }
    }

    write_json(data, os.path.join(base_dir, f'{model}_result_rq2.json'))


def rq3(base_dir, model):
    dirs = sorted([os.path.join(base_dir, item, model) for item in os.listdir(base_dir)
                   if os.path.isdir(os.path.join(base_dir, item))])
    data_no_metrics = {}
    data_no_triples = {}
    for dir_path in dirs:
        path_details_path = os.path.join(dir_path, f'{model}_path.json')
        path_details = read_json(path_details_path)
        idx = dir_path.split('/')[-2]
        question_id = f'{idx}. {path_details["query"]}'

        no_metrics_path = os.path.join(dir_path, 'ablation', f'{model}_no_metrics.json')
        no_triples_path = os.path.join(dir_path, 'ablation', f'{model}_no_triples.json')
        no_metrics_result = read_json(no_metrics_path)
        no_triples_result = read_json(no_triples_path)
        data_no_metrics[question_id] = {
            'idx': idx,
            'question': path_details['query'],
            'promql': no_metrics_result['promql'],
            'response_text': no_metrics_result['response_text'],
            'metrics_right': '_',  # human label in the future: 'T' or 'F'
            'promql_syntax_valid': '_',  # human label in the future: 'T' or 'F'
            'promql_right': '_',  # human label in the future: 'T' or 'F'
            'result_right': '_',  # human label in the future: 'T' or 'F'
        }
        data_no_triples[question_id] = {
            'idx': idx,
            'question': path_details['query'],
            'promql': no_triples_result['promql'],
            'response_text': no_triples_result['response_text'],
            'metrics_right': '_',  # human label in the future: 'T' or 'F'
            'promql_syntax_valid': '_',  # human label in the future: 'T' or 'F'
            'promql_right': '_',  # human label in the future: 'T' or 'F'
            'result_right': '_',  # human label in the future: 'T' or 'F'
        }
    write_json(data_no_metrics, os.path.join(base_dir, f'{model}_result_rq3_no_metrics.json'))
    write_json(data_no_triples, os.path.join(base_dir, f'{model}_result_rq3_no_triples.json'))


def do_rq(args):
    base_dir = args.base_dir
    model = args.model
    rq = args.rq
    if rq == 1:
        rq1(base_dir, model)
    elif rq == 2:
        rq2(base_dir, model, args.qa_path)
    else:
        rq3(base_dir, model)


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--base-dir', '-b', type=str, required=True)
    parser.add_argument('--model', '-m', type=str, required=True)
    parser.add_argument('--rq', '-q', type=int, choices=[1, 2, 3], required=True)
    parser.add_argument('--qa-path', '-a', type=str)
    args = parser.parse_args()
    if args.rq == 2 and args.qa_path is None:
        parser.error("The --qa-path/-a argument is required when --rq/-q is 2.")
    do_rq(args)


# python3 summary.py -b ./log/test-05-29 -m gpt-3.5-turbo-0125 -q 1
# python3 summary.py -b ./log/test-05-29 -m gpt-3.5-turbo-0125 -q 2 -a ./data/question/qa.csv
# python3 summary.py -b ./log/test-05-29 -m gpt-3.5-turbo-0125 -q 3
if __name__ == '__main__':
    main()
