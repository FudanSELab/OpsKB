from elasticsearch import Elasticsearch


class ESSearcher:
    def __init__(self, configs):
        self.es = Elasticsearch(configs['url'])
        self.all_mappings = configs['all_mappings']

    def search(self, index, query_text, top_k, match_type='best_fields'):
        query_body = {
            'query': {
                'multi_match': {
                    'query': query_text,
                    'fields': list(self.all_mappings[index]['mappings']['properties'].keys()),
                    'type': match_type
                }
            },
            'size': top_k
        }

        response = self.es.search(index=index, body=query_body)
        return response['hits']['hits']

    def scoped_search(self, index, query_text, top_k, filter_term, names, match_type='best_fields'):
        query = {
            'query': {
                'bool': {
                    'must': {
                        'multi_match': {
                            'query': query_text,
                            'fields': list(self.all_mappings[index]['mappings']['properties'].keys()),
                            'type': match_type
                        }
                    },
                    'filter': {
                        'terms': {
                            f'{filter_term}': names
                        }
                    }
                }
            },
            'size': top_k
        }

        response = self.es.search(index=index, body=query)
        return response['hits']['hits']
