from neo4j import GraphDatabase


class KGSearcher:
    def __init__(self, configs):
        self.configs = configs
        self.driver = None

    def connect(self):
        self.driver = GraphDatabase.driver(self.configs['url'], auth=(self.configs['user'], self.configs['password']))
        self.driver.verify_connectivity()

    def close(self):
        if self.driver:
            self.driver.close()

    def __apoc_expand(self, cypher):
        records, _, _ = self.driver.execute_query(cypher)
        path_sequences = []
        for path, hops in records:
            path_sequence = []
            nodes, relationships = path.nodes, path.relationships
            for node, rel in zip(nodes, relationships):
                node_dict = {
                    'type': next(iter(node.labels)),
                    'properties': dict(node.items())
                }
                path_sequence.append(node_dict)
                rel_dict = {
                    'type': rel.type,
                    'start_node_type': next(iter(rel.start_node.labels)),
                    'end_node_type': next(iter(rel.end_node.labels)),
                    'reverse': rel.start_node.element_id != node.element_id
                }
                path_sequence.append(rel_dict)
            last_node = path.nodes[-1]
            last_node_dict = {
                'type': next(iter(last_node.labels)),
                'properties': dict(last_node.items())
            }
            path_sequence.append(last_node_dict)

            path_sequences.append(path_sequence)

        return path_sequences

    def path_expand(self, start_entity_type, start_entity_name, sequence):
        max_level = (len(sequence.split(',')) - 1) // 2
        min_level = 0
        cypher = f"MATCH (start:{start_entity_type} {{name: '{start_entity_name}'}})\n" \
                 f"CALL apoc.path.expandConfig(start, {{\n" \
                 f"    sequence: '{sequence}',\n" \
                 f"    minLevel: {min_level},\n" \
                 f"    maxLevel: {max_level},\n" \
                 f"    bfs: true\n" \
                 f"}})\n" \
                 f"YIELD path\n" \
                 f"RETURN path, length(path) AS hops\n" \
                 f"ORDER BY hops;"
        path_sequences = self.__apoc_expand(cypher)

        return cypher, path_sequences

    def metric_expand(self, metric_name, k8s_entities):
        white_list_statements = '\n'.join([f'  OR (wn:{entity_type} AND wn.name in {str(list(entity_names))})'
                                           for entity_type, entity_names in k8s_entities.items()])
        cypher = f"MATCH (start:metric {{name: '{metric_name}'}})\n" \
                 f"MATCH (wn)\n" \
                 f"WHERE (start)-[:has]->(wn:label_value_pair)\n" \
                 f"{white_list_statements}\n" \
                 f"WITH start, collect(wn) AS whitelist_nodes\n" \
                 f"CALL apoc.path.expandConfig(start, {{\n" \
                 f"  sequence: 'metric, has, label_value_pair, related_to, " \
                 f"container|deployment|namespace|node|pod|replicaset|service|statefulset|api',\n" \
                 f"  minLevel: 1,\n" \
                 f"  maxLevel: 2,\n" \
                 f"  whitelistNodes: whitelist_nodes,\n" \
                 f"  bfs: true\n" \
                 f"}})\n" \
                 f"YIELD path\n" \
                 f"RETURN path, length(path) AS hops\n" \
                 f"ORDER BY hops;"
        path_sequences = self.__apoc_expand(cypher)

        return cypher, path_sequences

    def get_metric_labels_examples(self, name):
        cypher = f"MATCH (metric:metric {{name: '{name}'}})-[:has]->(end:label_value_pair)\n" \
                 "WITH metric, end.label AS label, COLLECT(end) AS label_pairs\n" \
                 "RETURN apoc.coll.randomItem(label_pairs) AS label_value_pair;"
        records, _, _ = self.driver.execute_query(cypher)
        return cypher, [dict(dict(record.items())['label_value_pair'].items())['name'] for record in records]

    def get_all_metrics(self):
        cypher = f"MATCH (m:metric)\n" \
                 "RETURN m.name AS name;"
        records, _, _ = self.driver.execute_query(cypher)
        return cypher, sorted([dict(record.items())['name'] for record in records])

    def get_reachable_node_types(self, metrics_names, hop):
        cypher = f"WITH {metrics_names} AS metrics_names\n" \
                 f"UNWIND metrics_names AS metric_name\n" \
                 f"MATCH (m:metric {{name: metric_name}})-[*{hop}]->(n)\n" \
                 f"RETURN metric_name, collect(distinct labels(n)) AS reachable_node_types;"
        records, _, _ = self.driver.execute_query(cypher)
        result = {name: [] for name in metrics_names}
        for record in records:
            d = dict(record)
            result[d['metric_name']] = sorted(set(t for i in d['reachable_node_types'] for t in i))
        return cypher, result
