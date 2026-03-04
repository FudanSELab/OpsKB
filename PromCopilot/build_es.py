from constant.es import *
from db.es import ESBuilder


def build_es():
    configs = {
        'url': ES_URL,
        'settings': ES_SETTINGS,
        'all_mappings': ES_ALL_MAPPINGS,
        'batch_size': ES_BULK_BATCH_SIZE,
        'data_dir': ES_DATA_DIR
    }
    es_builder = ESBuilder(configs)
    es_builder.build()


if __name__ == '__main__':
    build_es()
