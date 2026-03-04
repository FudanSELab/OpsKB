metric_system = """Please filter and return the relevant metrics from the provided metrics list that pertains to the given question. These metrics will be used to construct Prometheus Query Language (PromQL) queries corresponding to the question.
Instructions:
Step 1. Analyze the question to determine which metrics from the provided Metrics List are related to the query.
Step 2. Return a list of these relevant metrics. Ensure that the list of returned metrics is enclosed in triple backticks (```).
"""

metric_exemplars = [
    [
        {'role': 'user', 'content': """Metrics List:
- {"name": "container_ulimits_soft", "type": "gauge", "description": "Soft ulimit values for the container root process."}
- {"name": "container_fs_usage_bytes", "type": "gauge", "description": "Number of bytes that are consumed by the container on this filesystem."}
- {"name": "container_spec_memory_limit_bytes", "type": "gauge", "description": "Memory limit for the container."}
- {"name": "container_fs_limit_bytes", "type": "gauge", "description": "Number of bytes that can be consumed by the container on this filesystem."}
- {"name": "container_memory_usage_bytes", "type": "gauge", "description": "Current memory usage in bytes, including all memory regardless of when it was accessed"}
Question: Show the filesystem usage and limit bytes for all containers of the auth service."""},
        {'role': 'assistant', 'content': """Step 1. Analyze the question and identify the relevant metrics:
The relevant metrics for the filesystem usage and limit bytes for all containers of the auth service are:
- The metric "container_fs_usage_bytes" indicates the number of bytes currently consumed by the container on the filesystem. This measures the actual filesystem usage by the container.
- The metric "container_fs_limit_bytes" denotes the maximum number of bytes that can be consumed by the container on this filesystem. This represents the limit set for filesystem usage by the container.
Step 2. Relevant Metrics List:
```
- {"name": "container_fs_usage_bytes", "type": "gauge", "description": "Number of bytes that are consumed by the container on this filesystem."}
- {"name": "container_fs_limit_bytes", "type": "gauge", "description": "Number of bytes that can be consumed by the container on this filesystem."}
```
"""}
    ],
]

metric_user = """Metrics List:
{metrics_list}
Question: {query}
"""

metric_prompt = [{'role': 'system', 'content': metric_system}] +\
                [msg for exemplar in metric_exemplars for msg in exemplar]
