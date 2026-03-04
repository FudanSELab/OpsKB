from constant.kg import *
from db.kg import KGBuilder


def build_kg():
    configs = {
        'url': NEO4J_URL,
        'user': NEO4J_USER,
        'password': NEO4J_PASSWORD,
        'kg_data_base_dir': KG_DATA_BASE_DIR,
        'cypher_output_path': KG_BUILD_CYPHER_PATH
    }
    kg_builder = KGBuilder(configs)
    kg_builder.build()


if __name__ == '__main__':
    build_kg()
