# CHAT project

all files are present due to download and start for testing
functionality:

- all data stored in PostgreSQL (rooms, private chats, users, messages)
- authentication - users can create an account or use the login functionality with before created credentials
- messaging

## Docker

#### all related containers for run are present

`db - postgresql`
`amqp - rabbitmq`
`api - java backend`

#### Build the container command

```Bash
    docker compose up -build
```

## Kubernetes

before start we need new Docker container

```Bash
    docker build -t pupils-chat-api:latest .
```

after the container is ready we go with kubernetes commands

Checks if the Kubernetes cluster is online and if the local kubectl tool has the correct permissions to communicate with it.

```Bash
    kubectl get nodes
```

The standard command to submit the deployment and service configurations to the cluster.

```Bash
    kubectl apply -f k8s-manifest.yaml
```

A bypass command used to force the deployment when the cluster throws an OpenAPI schema validation error (common when the cluster is just waking up or versions mismatch).

```Bash
    kubectl apply -f k8s-manifest.yaml --validate=false
```

Lists all active pods and the -w (watch) flag keeps the terminal open to stream real-time status changes (e.g., transitioning from ContainerCreating to Running). Press Ctrl+C to exit.

```Bash
    kubectl get pods -w
```

Lists all active Services, showing their internal cluster IPs and mapped external NodePorts.

```Bash
    kubectl get svc
```

Creates a direct, secure tunnel linking port 30088 on the local Windows machine to port 8088 on the target Kubernetes service.

```Bash
    kubectl port-forward service/chat-api-service 30088:8088
```
