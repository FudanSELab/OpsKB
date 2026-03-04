import re
import pandas as pd
from scipy import spatial


def cosine_similarity(x, y):
    return 1 - spatial.distance.cosine(x, y)


def rank_by_relatedness(embedding: list[float], df: pd.DataFrame, top_n=3, relatedness_fn=cosine_similarity):
    if top_n == 0:
        return []
    relatedness_Q_A = [(relatedness_fn(embedding, row["embedding"]), row["question"], row["promql"]) for _, row in df.iterrows()]
    relatedness_Q_A.sort(key=lambda x: x[0], reverse=True)
    return relatedness_Q_A[:top_n]


def query_messages(system_prompt: str, embedding: list[float], query: str, df: pd.DataFrame, top_n=3):
    relatedness_Q_A = rank_by_relatedness(embedding, df, top_n=top_n)
    messages = [{"role": "system", "content": system_prompt}]
    for _, question, promql in relatedness_Q_A:
        messages.append({"role": "user", "content": question})
        messages.append({"role": "assistant", "content": f"```\n{promql}\n```"})
    messages.append({"role": "user", "content": query})
    return messages


def extract_promql(response_message: str):
    matches = re.findall(r"```(.*?)```", response_message, re.DOTALL)
    if len(matches) > 0:
        text = re.sub(r"#.*?(\n|$)", "", matches[0])
        text = text.strip().replace('\n', ' ')
        if text.startswith(("promql", "PromQL")):
            text = text[6:].strip()
        elif text.startswith(("prometheus", "Prometheus")):
            text = text[10:].strip()
    else:
        text = ""
    return text


if __name__ == "__main__":
    from config import *
    from openai import OpenAI
    from ast import literal_eval

    client_chat = OpenAI(api_key=OPENAI_API_KEY, base_url=OPENAI_BASE_URL)

    df = pd.read_csv(HISTORY_EMBEDDING_CSV)
    df["embedding"] = df["embedding"].apply(literal_eval)

    with open("system.txt", "r", encoding="utf-8") as fr:
        SYSTEM_PROMPT = fr.read()

    # generate results
    results = []
    result_file = f"result/{CHAT_MODEL}/k={TOP_N}/k={TOP_N}_model={CHAT_MODEL}_embedding={EMBEDDING_MODEL}.json"
    questions = pd.read_csv(QUESTIONS_EMBEDDING_CSV)
    questions["embedding"] = questions["embedding"].apply(literal_eval)
    cache = pd.read_json(result_file) if os.path.exists(result_file) else pd.DataFrame()

    for _, row in questions[questions["idx"] > cache.iloc[-1]["idx"]].iterrows():
        idx = row["idx"]
        query = row["question"]
        embedding = row["embedding"]
        print(f"{idx}. {query}")

        # generate messages
        messages = query_messages(SYSTEM_PROMPT, embedding, query, df, TOP_N)

        # generate promql
        response = client_chat.chat.completions.create(model=CHAT_MODEL, messages=messages, temperature=0.0)
        response_message = response.choices[0].message.content
        promql = extract_promql(response_message)
        print(promql)

        results.append({"idx": idx, "question": query, "messages": messages[1:], "answer": response_message, "promql": promql})

    final = pd.concat([cache, pd.DataFrame(results)], ignore_index=True)
    final.to_json(result_file, orient="records")
