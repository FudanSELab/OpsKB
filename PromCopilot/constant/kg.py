# set your neo4j ip and port
NEO4J_URL = 'neo4j://10.176.122.153:7687'
# set your neo4j username and password
NEO4J_USER = 'neo4j'
NEO4J_PASSWORD = 'promcopilot'


KG_ENTITIES = {
    'container',
    'deployment',
    'namespace',
    'node',
    'pod',
    'replicaset',
    'service',
    'api',
    'statefulset'
}

RESOURCE_LABELS = {
    'container_id',
    'deployment',
    'namespace',
    'ip',
    'internal_ip',
    'host_ip',
    'pod_ip',
    'instance',
    'node',
    'nodename',
    'pod',
    'replicaset',
    'service',
    'statefulset',
    'client',
    'server',
    'span_name',
}


# knowledge graph: additional indices (default index is name)
KG_ADDITIONAL_INDICES = {
    'container': ['id'],
    'node': ['ip'],
    'pod': ['ip']
}

# knowledge graph: entity and relationship entities data dir
KG_DATA_BASE_DIR = './data/import/'

# knowledge graph: generated cypher saved path
KG_BUILD_CYPHER_PATH = './data/middle/build_kg.cypher'

# knowledge graph: neo4j import dir (remote)
NEO4J_BASE_DIR = '/var/lib/neo4j/import'
