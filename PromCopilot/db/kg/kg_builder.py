import csv
import os

from py2neo import Graph

from constant.kg import *


class KGBuilder:
    def __init__(self, configs):
        self.configs = configs
        self.graph = Graph(configs['url'], auth=(configs['user'], configs['password']))

    def build(self):
        self.generate_and_save_cypher()
        # rsync_import_data()
        self.clean_neo4j()
        self.do_build_kg()

    def generate_and_save_cypher(self):
        cypher = self.generate_cypher()
        save_path = self.configs['cypher_output_path']
        dir_name = os.path.dirname(save_path)
        if not os.path.exists(dir_name):
            os.makedirs(dir_name)
        with open(save_path, 'w') as f:
            f.write(cypher)
            f.write('\n')
            print(
                f'save cypher file: {save_path}')

    def clean_neo4j(self):
        self.graph.run("""CALL gds.graph.list()
                         YIELD graphName
                         WITH graphName
                         CALL gds.graph.drop(graphName)
                         YIELD graphName as droppedGraph
                         RETURN droppedGraph;
                      """)
        self.graph.run("CALL apoc.schema.assert({},{})")
        self.graph.run("MATCH (n) DETACH DELETE n")
        print('clean neo4j done.')

    def do_build_kg(self):
        with open(self.configs['cypher_output_path'], 'r') as f:
            cypher = f.read()
            statements = cypher.split(';')
            for statement in statements:
                if statement.strip():
                    self.graph.run(statement)
        print(f'build kg done: cypher {self.configs["cypher_output_path"]} has been executed.')

    def generate_cypher(self):
        cypher_list = []
        self.merge_nodes(cypher_list)
        self.build_indices(cypher_list)
        self.merge_relations(cypher_list)
        project_graph(cypher_list)
        cypher = '\n'.join(cypher_list)
        return cypher

    def merge_nodes(self, cypher_list):
        entity_dir = os.path.join(self.configs['kg_data_base_dir'], 'entity')
        for entity in os.listdir(entity_dir):
            path = os.path.join(entity_dir, entity)
            merge_node(cypher_list, entity, path)

    def build_indices(self, cypher_list):
        entity_dir = os.path.join(self.configs['kg_data_base_dir'], 'entity')
        for entity in os.listdir(entity_dir):
            build_index(cypher_list, entity)
        build_additional_index(cypher_list)
        cypher_list.append('CALL db.awaitIndexes();')

    def merge_relations(self, cypher_list):
        rel_dir = os.path.join(self.configs['kg_data_base_dir'], 'relationship')
        for rel in os.listdir(rel_dir):
            merge_relation(cypher_list, rel)


def entity_type_to_key(entity_type):
    return 'id' if entity_type == 'container' else 'name'


def merge_node(cypher_list, entity, path):
    with open(path, 'r') as f:
        headers = next(csv.reader(f))

    entity_type = entity[:-4]
    file_uri = 'file:///' + entity
    key = entity_type_to_key(entity_type)

    cypher_list.append(f'LOAD CSV WITH HEADERS FROM \'{file_uri}\' AS row')
    cypher_list.append(f'MERGE ({entity_type}:{entity_type} {{{key}: row.{key}}})')

    attrs = ', '.join([f'{entity_type}.{h} = row.{h}' for h in headers])
    cypher_list.append(f'  ON CREATE SET {attrs};')


def build_index(cypher_list, entity):
    entity_type = entity[:-4]
    cypher_list.append(f'CREATE INDEX {entity_type}_index_name FOR (e:{entity_type}) ON (e.name);')


def build_additional_index(cypher_list):
    for entity_type in KG_ADDITIONAL_INDICES:
        for col in KG_ADDITIONAL_INDICES[entity_type]:
            cypher_list.append(f'CREATE INDEX {entity_type}_index_{col} FOR (e:{entity_type}) ON (e.{col});')


def merge_relation(cypher_list, rel):
    # rel format: {0:src_entity}-{1:dst_entity}-{2:rel_rel}-{3:src_entity_col}-{4:dst_entity_col}
    src_entity, dst_entity, rel_rel, src_col, dst_col = rel[:-4].split('-')

    src_entity_col, dst_entity_col = src_entity, dst_entity
    if src_entity == dst_entity:
        src_entity_col, dst_entity_col = 'src', 'dst'

    file_uri = 'file:///' + rel
    cypher_list.append(f'LOAD CSV WITH HEADERS FROM \'{file_uri}\' AS row')
    cypher_list.append(f'MATCH (src:{src_entity} {{{src_col}: row.{src_entity_col}}})')
    cypher_list.append(f'MATCH (dst:{dst_entity} {{{dst_col}: row.{dst_entity_col}}})')
    cypher_list.append(f'MERGE (src)-[rel:{rel_rel}]->(dst);')


def project_graph(cypher_list):
    cypher_list += [
        'CALL db.labels() YIELD label',
        'WITH collect(label) AS nodeLabels',
        'CALL db.relationshipTypes() YIELD relationshipType',
        'WITH nodeLabels, collect(relationshipType) AS relationshipTypes',
        'CALL gds.graph.project(',
        "    'chatkgops',",
        '    nodeLabels,',
        '    relationshipTypes',
        ')',
        'YIELD nodeCount, relationshipCount',
        'RETURN nodeCount, relationshipCount;'
    ]


# def rsync_import_data():
#     entity_dir = os.path.join(KG_DATA_BASE_DIR, 'Entity')
#     rel_dir = os.path.join(KG_DATA_BASE_DIR, 'Relationship')
#     rsync_entity_command = f'rsync -avvzh {entity_dir}/ k8s-master:{NEO4J_BASE_DIR}/'
#     rsync_relationship_command = f'rsync -avvzh {rel_dir}/ k8s-master:{NEO4J_BASE_DIR}/'
#     os.system(rsync_entity_command)
#     os.system(rsync_relationship_command)
#     print(f'rsync entity: {rsync_entity_command}')
#     print(f'rsync relationship: {rsync_relationship_command}')
