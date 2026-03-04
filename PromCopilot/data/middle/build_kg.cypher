LOAD CSV WITH HEADERS FROM 'file:///api.csv' AS row
MERGE (api:api {name: row.name})
  ON CREATE SET api.name = row.name, api.method = row.method, api.uri = row.uri, api.service = row.service, api.description = row.description;
LOAD CSV WITH HEADERS FROM 'file:///deployment.csv' AS row
MERGE (deployment:deployment {name: row.name})
  ON CREATE SET deployment.name = row.name;
LOAD CSV WITH HEADERS FROM 'file:///node.csv' AS row
MERGE (node:node {name: row.name})
  ON CREATE SET node.name = row.name, node.ip = row.ip;
LOAD CSV WITH HEADERS FROM 'file:///statefulset.csv' AS row
MERGE (statefulset:statefulset {name: row.name})
  ON CREATE SET statefulset.name = row.name;
LOAD CSV WITH HEADERS FROM 'file:///namespace.csv' AS row
MERGE (namespace:namespace {name: row.name})
  ON CREATE SET namespace.name = row.name;
LOAD CSV WITH HEADERS FROM 'file:///label_value_pair.csv' AS row
MERGE (label_value_pair:label_value_pair {name: row.name})
  ON CREATE SET label_value_pair.name = row.name, label_value_pair.label = row.label, label_value_pair.value = row.value;
LOAD CSV WITH HEADERS FROM 'file:///pod.csv' AS row
MERGE (pod:pod {name: row.name})
  ON CREATE SET pod.name = row.name, pod.ip = row.ip;
LOAD CSV WITH HEADERS FROM 'file:///metric.csv' AS row
MERGE (metric:metric {name: row.name})
  ON CREATE SET metric.name = row.name, metric.type = row.type, metric.description = row.description, metric.keywords = row.keywords;
LOAD CSV WITH HEADERS FROM 'file:///replicaset.csv' AS row
MERGE (replicaset:replicaset {name: row.name})
  ON CREATE SET replicaset.name = row.name;
LOAD CSV WITH HEADERS FROM 'file:///service.csv' AS row
MERGE (service:service {name: row.name})
  ON CREATE SET service.name = row.name, service.description = row.description, service.ip = row.ip;
LOAD CSV WITH HEADERS FROM 'file:///container.csv' AS row
MERGE (container:container {id: row.id})
  ON CREATE SET container.name = row.name, container.id = row.id;
CREATE INDEX api_index_name FOR (e:api) ON (e.name);
CREATE INDEX deployment_index_name FOR (e:deployment) ON (e.name);
CREATE INDEX node_index_name FOR (e:node) ON (e.name);
CREATE INDEX statefulset_index_name FOR (e:statefulset) ON (e.name);
CREATE INDEX namespace_index_name FOR (e:namespace) ON (e.name);
CREATE INDEX label_value_pair_index_name FOR (e:label_value_pair) ON (e.name);
CREATE INDEX pod_index_name FOR (e:pod) ON (e.name);
CREATE INDEX metric_index_name FOR (e:metric) ON (e.name);
CREATE INDEX replicaset_index_name FOR (e:replicaset) ON (e.name);
CREATE INDEX service_index_name FOR (e:service) ON (e.name);
CREATE INDEX container_index_name FOR (e:container) ON (e.name);
CREATE INDEX container_index_id FOR (e:container) ON (e.id);
CREATE INDEX node_index_ip FOR (e:node) ON (e.ip);
CREATE INDEX pod_index_ip FOR (e:pod) ON (e.ip);
CALL db.awaitIndexes();
LOAD CSV WITH HEADERS FROM 'file:///label_value_pair-statefulset-related_to-name-name.csv' AS row
MATCH (src:label_value_pair {name: row.label_value_pair})
MATCH (dst:statefulset {name: row.statefulset})
MERGE (src)-[rel:related_to]->(dst);
LOAD CSV WITH HEADERS FROM 'file:///label_value_pair-deployment-related_to-name-name.csv' AS row
MATCH (src:label_value_pair {name: row.label_value_pair})
MATCH (dst:deployment {name: row.deployment})
MERGE (src)-[rel:related_to]->(dst);
LOAD CSV WITH HEADERS FROM 'file:///label_value_pair-service-related_to-name-name.csv' AS row
MATCH (src:label_value_pair {name: row.label_value_pair})
MATCH (dst:service {name: row.service})
MERGE (src)-[rel:related_to]->(dst);
LOAD CSV WITH HEADERS FROM 'file:///service-service-requests-name-name.csv' AS row
MATCH (src:service {name: row.src})
MATCH (dst:service {name: row.dst})
MERGE (src)-[rel:requests]->(dst);
LOAD CSV WITH HEADERS FROM 'file:///deployment-replicaset-controls-name-name.csv' AS row
MATCH (src:deployment {name: row.deployment})
MATCH (dst:replicaset {name: row.replicaset})
MERGE (src)-[rel:controls]->(dst);
LOAD CSV WITH HEADERS FROM 'file:///service-pod-targets-name-name.csv' AS row
MATCH (src:service {name: row.service})
MATCH (dst:pod {name: row.pod})
MERGE (src)-[rel:targets]->(dst);
LOAD CSV WITH HEADERS FROM 'file:///label_value_pair-node-related_to-name-ip.csv' AS row
MATCH (src:label_value_pair {name: row.label_value_pair})
MATCH (dst:node {ip: row.node})
MERGE (src)-[rel:related_to]->(dst);
LOAD CSV WITH HEADERS FROM 'file:///label_value_pair-pod-related_to-name-name.csv' AS row
MATCH (src:label_value_pair {name: row.label_value_pair})
MATCH (dst:pod {name: row.pod})
MERGE (src)-[rel:related_to]->(dst);
LOAD CSV WITH HEADERS FROM 'file:///metric-label_value_pair-has-name-name.csv' AS row
MATCH (src:metric {name: row.metric})
MATCH (dst:label_value_pair {name: row.label_value_pair})
MERGE (src)-[rel:has]->(dst);
LOAD CSV WITH HEADERS FROM 'file:///label_value_pair-pod-related_to-name-ip.csv' AS row
MATCH (src:label_value_pair {name: row.label_value_pair})
MATCH (dst:pod {ip: row.pod})
MERGE (src)-[rel:related_to]->(dst);
LOAD CSV WITH HEADERS FROM 'file:///namespace-deployment-encapsulates-name-name.csv' AS row
MATCH (src:namespace {name: row.namespace})
MATCH (dst:deployment {name: row.deployment})
MERGE (src)-[rel:encapsulates]->(dst);
LOAD CSV WITH HEADERS FROM 'file:///replicaset-pod-manages-name-name.csv' AS row
MATCH (src:replicaset {name: row.replicaset})
MATCH (dst:pod {name: row.pod})
MERGE (src)-[rel:manages]->(dst);
LOAD CSV WITH HEADERS FROM 'file:///label_value_pair-node-related_to-name-name.csv' AS row
MATCH (src:label_value_pair {name: row.label_value_pair})
MATCH (dst:node {name: row.node})
MERGE (src)-[rel:related_to]->(dst);
LOAD CSV WITH HEADERS FROM 'file:///label_value_pair-api-related_to-name-name.csv' AS row
MATCH (src:label_value_pair {name: row.label_value_pair})
MATCH (dst:api {name: row.api})
MERGE (src)-[rel:related_to]->(dst);
LOAD CSV WITH HEADERS FROM 'file:///namespace-pod-encapsulates-name-name.csv' AS row
MATCH (src:namespace {name: row.namespace})
MATCH (dst:pod {name: row.pod})
MERGE (src)-[rel:encapsulates]->(dst);
LOAD CSV WITH HEADERS FROM 'file:///deployment-pod-manages-name-name.csv' AS row
MATCH (src:deployment {name: row.deployment})
MATCH (dst:pod {name: row.pod})
MERGE (src)-[rel:manages]->(dst);
LOAD CSV WITH HEADERS FROM 'file:///pod-container-contains-name-id.csv' AS row
MATCH (src:pod {name: row.pod})
MATCH (dst:container {id: row.container})
MERGE (src)-[rel:contains]->(dst);
LOAD CSV WITH HEADERS FROM 'file:///statefulset-pod-manages-name-name.csv' AS row
MATCH (src:statefulset {name: row.statefulset})
MATCH (dst:pod {name: row.pod})
MERGE (src)-[rel:manages]->(dst);
LOAD CSV WITH HEADERS FROM 'file:///namespace-statefulset-encapsulates-name-name.csv' AS row
MATCH (src:namespace {name: row.namespace})
MATCH (dst:statefulset {name: row.statefulset})
MERGE (src)-[rel:encapsulates]->(dst);
LOAD CSV WITH HEADERS FROM 'file:///namespace-replicaset-encapsulates-name-name.csv' AS row
MATCH (src:namespace {name: row.namespace})
MATCH (dst:replicaset {name: row.replicaset})
MERGE (src)-[rel:encapsulates]->(dst);
LOAD CSV WITH HEADERS FROM 'file:///service-api-provides-name-name.csv' AS row
MATCH (src:service {name: row.service})
MATCH (dst:api {name: row.api})
MERGE (src)-[rel:provides]->(dst);
LOAD CSV WITH HEADERS FROM 'file:///label_value_pair-replicaset-related_to-name-name.csv' AS row
MATCH (src:label_value_pair {name: row.label_value_pair})
MATCH (dst:replicaset {name: row.replicaset})
MERGE (src)-[rel:related_to]->(dst);
LOAD CSV WITH HEADERS FROM 'file:///api-api-calls-name-name.csv' AS row
MATCH (src:api {name: row.src})
MATCH (dst:api {name: row.dst})
MERGE (src)-[rel:calls]->(dst);
LOAD CSV WITH HEADERS FROM 'file:///node-pod-hosts-name-name.csv' AS row
MATCH (src:node {name: row.node})
MATCH (dst:pod {name: row.pod})
MERGE (src)-[rel:hosts]->(dst);
LOAD CSV WITH HEADERS FROM 'file:///namespace-service-encapsulates-name-name.csv' AS row
MATCH (src:namespace {name: row.namespace})
MATCH (dst:service {name: row.service})
MERGE (src)-[rel:encapsulates]->(dst);
LOAD CSV WITH HEADERS FROM 'file:///label_value_pair-namespace-related_to-name-name.csv' AS row
MATCH (src:label_value_pair {name: row.label_value_pair})
MATCH (dst:namespace {name: row.namespace})
MERGE (src)-[rel:related_to]->(dst);
LOAD CSV WITH HEADERS FROM 'file:///label_value_pair-container-related_to-name-name.csv' AS row
MATCH (src:label_value_pair {name: row.label_value_pair})
MATCH (dst:container {name: row.container})
MERGE (src)-[rel:related_to]->(dst);
CALL db.labels() YIELD label
WITH collect(label) AS nodeLabels
CALL db.relationshipTypes() YIELD relationshipType
WITH nodeLabels, collect(relationshipType) AS relationshipTypes
CALL gds.graph.project(
    'chatkgops',
    nodeLabels,
    relationshipTypes
)
YIELD nodeCount, relationshipCount
RETURN nodeCount, relationshipCount;
