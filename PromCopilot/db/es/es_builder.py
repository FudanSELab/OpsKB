import os

import pandas as pd
from elasticsearch import Elasticsearch, helpers


class ESBuilder:
    def __init__(self, configs):
        self.es = Elasticsearch(configs['url'])
        self.settings = configs['settings']
        self.all_mappings = configs['all_mappings']
        self.insert_batch_size = configs['batch_size']
        self.data_dir = configs['data_dir']

    def build(self):
        self.delete_indices()
        self.create_indices()
        self.insert_all()

    def delete_indices(self):
        for index in self.all_mappings.keys():
            self.delete_index(index)

    def delete_index(self, index):
        if self.es.indices.exists(index=index):
            self.es.indices.delete(index=index)
            print(f'delete index {index}.')
        else:
            print(f'delete index named {index} is not exists.')

    def create_indices(self):
        body = {
            "settings": self.settings,
            "mappings": None
        }
        for index, mappings in self.all_mappings.items():
            body['mappings'] = mappings['mappings']
            self.es.indices.create(index=index, body=body)
            print(f'create index: {index}')

    def insert_all(self):
        for index, mappings in self.all_mappings.items():
            df = pd.read_csv(os.path.join(self.data_dir, f'{index}.csv'))
            df.fillna('', inplace=True)
            properties = mappings['mappings']['properties'].keys()
            actions = []
            for _, row in df.iterrows():
                source_data = {prop: row[prop] for prop in properties}
                actions.append({'_source': source_data})
                if len(actions) == self.insert_batch_size:
                    self.bulk_insert(index, actions)
                    actions = []
            if len(actions) > 0:
                self.bulk_insert(index, actions)

    def bulk_insert(self, index, actions):
        success, _ = helpers.bulk(self.es, actions, index=index)
        print(f'successfully insert {success} docs into {index}.')
