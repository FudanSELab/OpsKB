import re
import pandas as pd
from openai import OpenAI
from collections import defaultdict


def embed_questions(questions: list[str], client: OpenAI, embedding_model: str):
    response = client.embeddings.create(model=embedding_model, input=questions)
    for i, be in enumerate(response.data):
        assert i == be.index  # double check embeddings are in same order as input
    embeddings = [e.embedding for e in response.data]
    return embeddings


def gen_labels_csv(top_n: int, chat_model: str, embedding_model: str):
    filepath = f"result/{chat_model}/k={top_n}/k={top_n}_model={chat_model}_embedding={embedding_model}_labels.csv"
    df = pd.DataFrame({"idx": list(range(1, 231)), "syntax": "", "metric": "", "promql": "", "result": ""})
    df.to_csv(filepath, index=False)


def count_promql_word(promql: str):
    matches = re.findall(r"'[^']*'|\"[^\"]*\"", promql)
    promql = re.sub(r"'[^']*'|\"[^\"]*\"", "<*>", promql)
    promql = re.sub(r"[,={}()\[\]~]", " ", promql)
    words = promql.split()

    rep_idx = 0
    final_words = []
    for word in words:
        if word == "<*>":
            final_words.append(matches[rep_idx])
            rep_idx += 1
        else:
            final_words.append(word)

    return len(final_words)


def length_distribution(df: pd.DataFrame, len_method=count_promql_word):
    count = defaultdict(lambda: 0)
    for _, row in df.iterrows():
        length = len_method(row["promql"])
        count[length] += 1
    return count


def add_idx_for_history(history_csv: str, history_embedding_csv: str):
    history_df = pd.read_csv(history_csv)
    history_df["idx"] = [f'h{i:03}' for i in range(1, 51)]
    history_df.to_csv(history_csv, index=False)

    history_with_embedding_df = pd.read_csv(history_embedding_csv)
    history_with_embedding_df["idx"] = [f'h{i:03}' for i in range(1, 51)]
    history_with_embedding_df.to_csv(history_embedding_csv, index=False)


if __name__ == "__main__":
    from config import *

    # Embed the questions to be tested.
    client_embedding = OpenAI(api_key=OPENAI_API_KEY, base_url=OPENAI_BASE_URL)
    df = pd.read_csv(QUESTIONS_CSV)
    questions = df["question"].to_list()
    df["embedding"] = embed_questions(questions, client_embedding, EMBEDDING_MODEL)
    df = df.drop(columns=["promql"])
    df.to_csv(QUESTIONS_EMBEDDING_CSV, index=False)

    # Distribution of promql length
    result = length_distribution(pd.read_csv("data/full.csv"))
    df = pd.DataFrame.from_dict(result, orient="index").sort_index()
    df = df.reset_index()
    df.columns = ["length", "count"]
    df.to_csv("data/count.csv", index=False)

    # Utils help labeling.
    gen_labels_csv(TOP_N, CHAT_MODEL, EMBEDDING_MODEL)
