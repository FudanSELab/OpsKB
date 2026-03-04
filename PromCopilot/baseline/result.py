import pandas as pd


def calculate(top_n: int, chat_model: str, embedding_model: str):
    df = pd.read_csv(f"result/{chat_model}/k={top_n}/k={top_n}_model={chat_model}_embedding={embedding_model}_labels.csv")
    rows = len(df)
    syntax = df["syntax"].sum()
    metric = df["metric"].sum()
    promql = df["promql"].sum()
    result = df["result"].sum()
    print(f"k={top_n} model={chat_model} embedding={embedding_model}")
    print(f"├─ metric: {metric} / {rows} = {metric / rows}")
    print(f"├─ syntax: {syntax} / {rows} = {syntax / rows}")
    print(f"├─ promql: {promql} / {rows} = {promql / rows}")
    print(f"└─ result: {result} / {rows} = {result / rows}\n")


if __name__ == "__main__":
    from config import *

    # calculate(TOP_N, CHAT_MODEL, EMBEDDING_MODEL)

    calculate(0, "gpt-3.5-turbo-0125", "text-embedding-3-large")
    calculate(1, "gpt-3.5-turbo-0125", "text-embedding-3-large")
    calculate(3, "gpt-3.5-turbo-0125", "text-embedding-3-large")
    calculate(10, "gpt-3.5-turbo-0125", "text-embedding-3-large")

    calculate(0, "gpt-4-turbo-2024-04-09", "text-embedding-3-large")
    calculate(1, "gpt-4-turbo-2024-04-09", "text-embedding-3-large")
    calculate(3, "gpt-4-turbo-2024-04-09", "text-embedding-3-large")
    calculate(10, "gpt-4-turbo-2024-04-09", "text-embedding-3-large")

    calculate(0, "qwen-72b-chat", "text-embedding-3-large")
    calculate(1, "qwen-72b-chat", "text-embedding-3-large")
    calculate(3, "qwen-72b-chat", "text-embedding-3-large")
    calculate(10, "qwen-72b-chat", "text-embedding-3-large")

    calculate(0, "deepseek-coder", "text-embedding-3-large")
    calculate(1, "deepseek-coder", "text-embedding-3-large")
    calculate(3, "deepseek-coder", "text-embedding-3-large")
    calculate(10, "deepseek-coder", "text-embedding-3-large")
