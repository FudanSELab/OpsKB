import os

# Load the OpenAI API key and base URL from environment variables.
# These need to be manually configured in the environment.
OPENAI_API_KEY = os.getenv('OPENAI_API_KEY')
OPENAI_BASE_URL = os.getenv('OPENAI_BASE_URL')

INPUT_MAX_TOKEN = 32000
