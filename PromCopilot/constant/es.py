# es batch size used in doc building
ES_BULK_BATCH_SIZE = 1024

# entity data dir
ES_DATA_DIR = './data/import/entity'

# set your elasticsearch ip and port
ES_URL = 'http://10.176.122.153:9200'

# es index settings
ES_SETTINGS = {
    "analysis": {
        "analyzer": {
            "default": {
                "type": "english"
            }
        }
    },
    "similarity": {
        "default": {
            "type": "BM25",
            "k1": 1.2,
            "b": 0.75
        }
    }
}

# es mappings
ES_ALL_MAPPINGS = {
    "container": {
        "mappings": {
            "properties": {
                "name": {"type": "text", "fields": {"keyword": {"type": "keyword"}}},
                "id": {"type": "text"},
            }
        }
    },
    "deployment": {
        "mappings": {
            "properties": {
                "name": {"type": "text", "fields": {"keyword": {"type": "keyword"}}},
            }
        }
    },
    "metric": {
        "mappings": {
            "properties": {
                "name": {"type": "text", "fields": {"keyword": {"type": "keyword"}}},
                "type": {"type": "text"},
                "description": {"type": "text"},
                "keywords": {"type": "text"},
            }
        }
    },
    "label_value_pair": {
        "mappings": {
            "properties": {
                "name": {"type": "text", "fields": {"keyword": {"type": "keyword"}}},
                "label": {"type": "text", "fields": {"keyword": {"type": "keyword"}}},
                "value": {"type": "text"},
            }
        }
    },
    "namespace": {
        "mappings": {
            "properties": {
                "name": {"type": "text", "fields": {"keyword": {"type": "keyword"}}},
            }
        }
    },
    "node": {
        "mappings": {
            "properties": {
                "name": {"type": "text", "fields": {"keyword": {"type": "keyword"}}},
                "ip": {"type": "text"},
            }
        }
    },
    "pod": {
        "mappings": {
            "properties": {
                "name": {"type": "text", "fields": {"keyword": {"type": "keyword"}}},
                "ip": {"type": "text"},
            }
        }
    },
    "replicaset": {
        "mappings": {
            "properties": {
                "name": {"type": "text", "fields": {"keyword": {"type": "keyword"}}},
            }
        }
    },
    "service": {
        "mappings": {
            "properties": {
                "name": {"type": "text", "fields": {"keyword": {"type": "keyword"}}},
                "description": {"type": "text"},
                "ip": {"type": "text"},
            }
        }
    },
    "statefulset": {
        "mappings": {
            "properties": {
                "name": {"type": "text", "fields": {"keyword": {"type": "keyword"}}},
            }
        }
    },
    "api": {
        "mappings": {
            "properties": {
                "name": {"type": "text", "fields": {"keyword": {"type": "keyword"}}},
                "method": {"type": "text"},
                "uri": {"type": "text"},
                "service": {"type": "text"},
                "description": {"type": "text"},
            }
        }
    }
}
