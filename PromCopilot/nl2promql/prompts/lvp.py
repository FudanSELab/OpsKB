lvp_system = """Given a Prometheus metric and its associated labels, identify any descriptions in the given question that refer to these labels. Enclose your results within triple backticks.
"""

lvp_exemplars = [
    [
        {'role': 'user', 'content': """1. Metric Information:
{"name": "node_cpu_seconds_total", "type": "counter", "description": "Seconds the CPUs spent in each mode."}
2. Metric Labels:
The prometheus metric `node_cpu_seconds_total` has 6 types of labels: cpu, container, instance, mode, job and pod. Below are examples of these 6 label types (For each label, provide an example of a possible value):
```
- cpu: cpu=0
- container: container=node-exporter
- instance: instance=10.176.122.151:9100
- mode: mode=idle
- job: job=node-exporter
- pod: pod=prometheus-prometheus-node-exporter-75lpk
```
3. Question: Compare cpu time spend in user mode and system mode on k8s-node1 over the past 5 minutes.
"""},
        {'role': 'assistant', 'content': """Segments related to the metric's labels within the sentence.
```
- cpu: []  # Not Found
- container: []  # Not Found
- instance: []  # Not Found
- mode: ["user mode", "system mode"]
- job: []  # Not Found
- pod: []  # Not Found
```
"""}
    ],
]


lvp_prompt = [{'role': 'system', 'content': lvp_system}] +\
                [msg for exemplar in lvp_exemplars for msg in exemplar]

