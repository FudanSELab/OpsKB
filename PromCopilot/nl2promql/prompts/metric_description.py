md_system = """Task Description:
Given a question, please extract the descriptions of metrics mentioned in the sentence as well as the system component type it belongs to. System component types include container, deployment, namespace, node, pod, replicaset, statefulset, service, and API.
Answer Formats:
Your answer should be an array of tuples. Each tuple should contain two elements: the first is the description of the metric, and the second is the type of component it relates to.
"""

md_exemplars = [
    [
        {'role': 'user', 'content': 'Question: Show the total memory allocation for the pods of the admin user service on node k8s-node3 or k8s-node4 over the last hour.'},
        {'role': 'assistant', 'content': '[("total memory allocation", "pod")]'}
    ]
]

md_prompt = [{'role': 'system', 'content': md_system}] +\
            [msg for exemplar in md_exemplars for msg in exemplar]
