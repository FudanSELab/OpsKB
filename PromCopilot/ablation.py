import argparse
import copy
import json
import os
import re

from openai import OpenAI

from constant import OPENAI_API_KEY, OPENAI_BASE_URL

llm = OpenAI(api_key=OPENAI_API_KEY, base_url=OPENAI_BASE_URL)


def read_json(file_path):
    with open(file_path, 'r') as file:
        data = json.load(file)
    return data


def write_json(data, file_path):
    if os.path.exists(file_path):
        print(f'skip exists: {file_path}')
        return
    os.makedirs(os.path.dirname(file_path), exist_ok=True)
    with open(file_path, 'w') as file:
        json.dump(data, file, indent=2)
    print(f'write json: {file_path}')


def extract_promql_response(response_text):
    match_results = re.findall(r"```[^\n]*\n(.*?)```", response_text, re.DOTALL)
    return [result for result in match_results]


def chat_complete(model, messages, temperature):
    response = llm.chat.completions.create(
        model=model,
        messages=messages,
        temperature=temperature
    )
    response_text = response.choices[0].message.content
    return response_text


def remove_metrics_info(text):
    lines = text.split('\n')
    filtered_lines = [line for line in lines if not line.strip().startswith('- (metric:')]
    return '\n'.join(filtered_lines)


def do_no_metrics_ablation(original_messages, base_dir):
    model = base_dir.split('/')[-2]
    assert model in ('gpt-3.5-turbo-0125', 'gpt-4-turbo-2024-04-09', 'qwen-72b-chat', 'deepseek-coder')
    output_path = os.path.join(base_dir, f'{model}_no_metrics.json')
    if os.path.exists(output_path):
        print(f'do_no_metrics_ablation: skip exists: {output_path}')
        return
    no_metrics_user_prompt = '1. Related metrics:\n' + '2. Domain knowledge:' + remove_metrics_info(
        original_messages[-1]['content'].split('2. Domain knowledge:')[1])
    messages = copy.deepcopy(original_messages)
    messages[-1]['content'] = no_metrics_user_prompt
    response_text = chat_complete(model, messages, 0.3)
    promql = extract_promql_response(response_text)
    data = {
        'promql': promql,
        'response_text': response_text,
        'messages': messages
    }
    write_json(data, output_path)


def do_no_triples_ablation(original_messages, base_dir):
    model = base_dir.split('/')[-2]
    assert model in ('gpt-3.5-turbo-0125', 'gpt-4-turbo-2024-04-09', 'qwen-72b-chat', 'deepseek-coder')
    output_path = os.path.join(base_dir, f'{model}_no_triples.json')
    if os.path.exists(output_path):
        print(f'do_no_triples_ablation: skip exists: {output_path}')
        return
    original_last_user_prompt = original_messages[-1]['content']
    no_triples_user_prompt = original_last_user_prompt.split("2. Domain knowledge:")[
                                 0] + '2. Domain knowledge:\n3. Question:' + \
                             original_last_user_prompt.split('3. Question:')[1]
    messages = copy.deepcopy(original_messages)
    messages[-1]['content'] = no_triples_user_prompt
    response_text = chat_complete(model, messages, 0.3)
    promql = extract_promql_response(response_text)
    data = {
        'promql': promql,
        'response_text': response_text,
        'messages': messages
    }
    write_json(data, output_path)


def do_ablation(original_messages, ablation_base_dir):
    do_no_metrics_ablation(original_messages, ablation_base_dir)
    do_no_triples_ablation(original_messages, ablation_base_dir)


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--base-dir', '-b', type=str, required=True)
    parser.add_argument('--model', '-m', type=str, required=True)
    args = parser.parse_args()
    base_dir = args.base_dir
    model = args.model

    dirs = sorted([os.path.join(base_dir, item, model) for item in os.listdir(base_dir)
                   if os.path.isdir(os.path.join(base_dir, item))])

    for dir_path in dirs:
        promql_details_path = os.path.join(dir_path, f'{model}_promql_prompt.json')
        promql_details = read_json(promql_details_path)
        ablation_base_dir = os.path.join(dir_path, 'ablation')
        do_ablation(promql_details['messages'], ablation_base_dir)


# python3 ablation.py -b ./log/test-06-03 -m gpt-3.5-turbo-0125
if __name__ == '__main__':
    main()
