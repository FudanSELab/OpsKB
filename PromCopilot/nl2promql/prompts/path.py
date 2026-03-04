path_system = """Task Description:
This task aims to utilize the knowledge graph to obtain relevant domain knowledge to generate a Prometheus Query Language (PromQL) corresponding to a question. Please based on the given knowledge graph schema (including an entity list and a relationship list), analyze the entities contained in the provided question and the relationships between them, and generate meta-paths to represent them to help retrieve the domain knowledge from the knowledge graph to answer the question.

Knowledge Graph Schema:
1. Entity List:
- container: A docker container, managed within Kubernetes.
- deployment: A Kubernetes resource for managing automated deployment.
- namespace: A Kubernetes feature that partitions cluster resources.
- node: A worker machine in Kubernetes that hosts Pods.
- pod: The smallest deployable unit in Kubernetes, containing one or more containers.
- replicaset: A Kubernetes resource for managing a specified number of pod replicas.
- service: A Kubernetes abstraction that defines a set of Pods and policies for accessing them.
- api: An interface of a service, designed to provide functionalities to other services within the system.
- statefulset: A Kubernetes resource for managing stateful Pods in Kubernetes.
2. Relationship List:
- node--hosts->pod: A node hosts multiple pods.
- namespace--encapsulates->deployment: A namespace encapsulates multiple deployments.
- namespace--encapsulates->pod: A namespace encapsulates multiple pods.
- namespace--encapsulates->replicaset: A namespace encapsulates multiple replicasets.
- namespace--encapsulates->service: A namespace encapsulates multiple services.
- namespace--encapsulates->statefulset: A namespace encapsulates multiple statefulsets.
- replicaset--manages->pod: A replicaset manages multiple pods.
- deployment--controls->replicaset: A deployment controls one or more replicasets.
- statefulset--manages->pod: A statefulset manages multiple pods.
- pod--contains->container: A pod contains one or more containers.
- service--requests->service: A service sends requests to other services in order to fulfill its operations or to obtain necessary data for processing.
- service--targets->pod: A service targets one or more pods.
- service--provides->api: A service provides multiple APIs, each offering different functionalities.
- api--calls->api: An API calls other APIs during execution.

Instructions:
Step 1. Analyze the query to identify the necessary entities and relationships, and construct meta-paths that logically connect these based on the provided schema, in order to explore the knowledge in the knowledge graph needed to answer the query.
Step 2. Merge any paths that can be combined from the results of the Step 1 analysis. List each meta-path clearly, indicating the involved entities and their relationships.

Meta-Path Formats:
1. Meta-paths are formatted as sequences of entities connected by relationships. In some instances, a meta-path may consist only of a single entity. These paths map out routes in the knowledge graph to locate and extract relevant data.
2. Each entity is defined by its type and either specified by a description from the query or denoted by a placeholder ('?'). The placeholder '?' indicates that the information for that entity is unknown and requires further exploration within the knowledge graph.
3. Relationships are directional, marked by arrows.

Notes:
1. You should not invent or fabricate entities or relationships that do not exist in the given Knowledge Graph Schema.
2. The extracted entity description should specifically refer to a single, distinct entity within the knowledge graph.
3. The description of the first entity in each meta-path should not be a question mark placeholder ('?')."""

path_exemplars = [
    [
        {'role': 'user', 'content': 'Query: Show the total memory allocation for the pods of the admin user service on node k8s-node3 or k8s-node4 over the last hour.'},
        {'role': 'assistant', 'content': """Step 1. Think step by step:
- We need to know which pods of the admin user service are on either of the specified nodes, "k8s-node3" or "k8s-node4". To obtain this information, according to the given knowledge graph schema, we can construct two meta-paths to explore the knowledge graph: (service: admin user service) --targets-> (pod: ?) <-hosts-- (node: k8s-node3) and (service: admin user service) --targets-> (pod: ?) <-hosts-- (node: k8s-node4). Here, the names of the pods are unknown, so we use a question mark (?) to indicate this.
Step 2. meta-path list:
```
- (service: admin user service) --targets-> (pod: ?) <-hosts-- (node: k8s-node3)
- (service: admin user service) --targets-> (pod: ?) <-hosts-- (node: k8s-node4)
```"""}
    ],
    [
        {'role': 'user', 'content': 'Query: What are the memory limits and usage values for the top three memory consuming containers in the food delivery service on node k8s-node4?'},
        {'role': 'assistant', 'content': """Step 1. Think step by step:
- First, we need to determine which pods are part of the "food delivery service" and hosted on the node "k8s-node4". This involves constructing a meta-path that connects the service to its pods on a specific node: (service: food delivery service) --targets-> (pod: ?) <-hosts-- (node: k8s-node4), where "?" signifies the unknown pod names.
- Next, we aim to identify containers within these specific pods. This requires an additional meta-path that describes the relationship between pods and their contained containers: (service: food delivery service) --targets-> (pod: ?) --contains-> (container: ?).
Step 2. meta-path list:
```
- (service: food delivery service) --targets-> (pod: ?) <-hosts-- (node: k8s-node4)
- (service: food delivery service) --targets-> (pod: ?) --contains-> (container: ?)
```"""}
    ],
    [
        {'role': 'user', 'content': 'Query: Calculate the sum of the number of Inodes for all pods of the service that has the API for creating new stations.'},
        {'role': 'assistant', 'content': """Step 1. Think step by step:
- First, identify the service that provides the API for creating new stations. This involves a meta-path that connects the specific API to its service: (api: API for creating new stations) <-provides-- (service: ?). The "?" represents the unknown service that offers this specific API.
- Next, determine which pods are part of this service. This requires extending the meta-path to include the pods targeted by this service: (service: ?) --targets-> (pod: ?).
Step 2. meta-path list:
```
- (api: API for creating new stations) <-provides-- (service: ?) --targets-> (pod: ?)
```"""}
    ],
    [
        {'role': 'user', 'content': 'Query: Show the total disk I/O operations for all pods of services that call the ts-rebook-service in the last hour.'},
        {'role': 'assistant', 'content': """Step 1. Think step by step:
- We need to identify which services are making requests to the "ts-rebook-service". This involves creating a meta-path to show the interaction between services: (service: ts-rebook-service) <-requests-- (service: ?), where "?" represents the unknown services that are dependent on or interact with the "ts-rebook-service". We also need to establish which pods are targeted by these services. This requires an extension of the previous meta-path to include pods: (service: ?) --targets-> (pod: ?), with "?" indicating unknown pod names that are part of the services making requests to "ts-rebook-service".
Step 2. meta-path list:
```
- (service: ts-rebook-service) <-requests-- (service: ?) --targets-> (pod: ?)
```"""}
    ],
    [
        {'role': 'user', 'content': 'Query: Calculate the average CPU usage for all nodes hosting the order service over the last week.'},
        {'role': 'assistant', 'content': """Step 1. Think step by step:
- We need to identify which nodes are hosting the pods of the "order service". This involves using a meta-path that connects the service to its pods and then identifies the nodes hosting these pods: (service: order service) --targets-> (pod: ?) <-hosts-- (node: ?), where "?" represents unknown pod names and the nodes they are hosted on.
Step 2. meta-path list:
```
- (service: order service) --targets-> (pod: ?) <-hosts-- (node: ?)
```"""}
    ]
]

path_prompt = [{'role': 'system', 'content': path_system}] + [msg for exemplar in path_exemplars for msg in exemplar]
