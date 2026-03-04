import os
from dotenv import load_dotenv

load_dotenv()

HISTORY_CSV = "data/history.csv"
HISTORY_EMBEDDING_CSV = "data/history_embedding.csv"
QUESTIONS_CSV = "data/question.csv"
QUESTIONS_EMBEDDING_CSV = "data/question_embedding.csv"

EMBEDDING_MODEL = "text-embedding-3-large"

# CHAT_MODEL = "deepseek-coder"
# CHAT_MODEL = "gpt-3.5-turbo-0125"
CHAT_MODEL = "gpt-4-turbo-2024-04-09"
# CHAT_MODEL = "qwen-72b-chat"

# TOP_N = 0
# TOP_N = 1
# TOP_N = 3
TOP_N = 10

OPENAI_API_KEY = os.getenv("OPENAI_API_KEY")
OPENAI_BASE_URL = os.getenv("OPENAI_BASE_URL")

PROMETHEUS_BASE_URL = "http://10.176.122.154:19090"
