import argparse
import datetime
import traceback

import pandas as pd

from constant import *
from nl2promql import NL2PromQL


def query(transformer, question, record_directory, time_id, skip_exists=True):
    complete_configs = {
        'query': question,
        'record_directory': record_directory,
        'time_id': time_id,
    }
    base_dir = os.path.join(record_directory, time_id)
    if skip_exists and os.path.exists(base_dir):
        print(f'[skip_exists] {base_dir}')
        return

    transformer.complete(complete_configs)


def main():
    parser = argparse.ArgumentParser()
    group = parser.add_mutually_exclusive_group(required=True)
    group.add_argument('--file', '-f', type=str)
    group.add_argument('--question', '-q', type=str)
    parser.add_argument('--model', '-m', type=str, required=True)
    args = parser.parse_args()

    assert args.model in ('gpt-3.5-turbo-0125', 'gpt-4-turbo-2024-04-09', 'qwen-72b-chat', 'deepseek-coder')

    configs = {
        'llm': {
            'api_key': OPENAI_API_KEY,
            'base_url': OPENAI_BASE_URL,
        },
        'es': {
            'url': ES_URL,
            'all_mappings': ES_ALL_MAPPINGS,
        },
        'neo4j': {
            'url': NEO4J_URL,
            'user': NEO4J_USER,
            'password': NEO4J_PASSWORD,
        },
        'metrics_components': {
            'data_path': './data/middle/metrics_components.json',
            'batch_size': 20,
        },
        'path': {
            'record_file': 'path.json',
            'llm': {
                'model': args.model,  # 'gpt-3.5-turbo-0125', 'gpt-4-turbo-2024-04-09'
                'temperature': 0.3,
            },
            'es': {
                'top_k': 1,  # for each entity in meta-path
                'match_type': 'best_fields'
            },
        },
        'md': {
            'record_file': 'md.json',
            'llm': {
                'model': args.model,  # 'gpt-3.5-turbo-0125', 'gpt-4-turbo-2024-04-09'
                'temperature': 0.3,
            },
        },
        'metric': {
            'record_file': 'metric.json',
            'llm': {
                'model': args.model,  # 'gpt-3.5-turbo-0125', 'gpt-4-turbo-2024-04-09'
                'temperature': 0.3,
            },
            'es': {
                'top_k': 10,  # for each metric description
                'match_type': 'best_fields'
            },
        },
        'lvp': {
            'record_file': 'lvp.json',
            'llm': {
                'model': args.model,  # 'gpt-3.5-turbo-0125', 'gpt-4-turbo-2024-04-09'
                'temperature': 0.3,
            },
            'es': {
                'top_k': 1,  # for each lvp mention in sentence
                'match_type': 'best_fields'
            },
        },
        'promql': {
            'record_file': 'promql_prompt.json',
            'llm': {
                'model': args.model,  # 'gpt-3.5-turbo-0125', 'gpt-4-turbo-2024-04-09'
                'temperature': 0.3,
            }
        },
    }

    with NL2PromQL(configs) as transformer:
        if args.file:
            df = pd.read_csv(args.file)
            for _, row in df.iterrows():
                idx, question = row['idx'], row['question']
                try:
                    print(f'{idx:03}')
                    query(transformer,
                          question,
                          os.path.join('./log/test', f'{idx:03}'),
                          args.model)
                except Exception as e:
                    print(f'##### {idx}. {question} #####')
                    print('Exception:')
                    print(e)
                    traceback.print_exc()
        elif args.question:
            try:
                query(transformer,
                      args.question,
                      os.path.join('./log/debug', 'anon'),
                      datetime.datetime.now().strftime("%Y-%m-%d-%H-%M-%S"))
            except Exception as e:
                print(f'##### {args.question} #####')
                print('Exception:')
                print(e)
                traceback.print_exc()
        else:
            assert False


if __name__ == '__main__':
    main()
