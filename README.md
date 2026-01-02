# Chat Application Infrastructure Demo (Scala + Kubernetes)

This repository contains a demo infrastructure setup for a **Scala‑based chat application**, deployed on **Kubernetes**, packaged with **Helm**, and monitored using **Prometheus** and **Grafana**.

---

## Prerequisites

Before you begin, make sure the following tools are installed:

- **Docker**
- **Kubernetes** + **kubectl**
- **Minikube**
- **Helm**
- Any additional dependencies required by your environment

---

##  Getting Started

### Clone the repository

\`\`\`bash
git clone <your_repo_url>
cd <your_repo>
\`\`\`

---

### Start a Kubernetes cluster with Minikube

\`\`\`bash
minikube start --driver=docker
\`\`\`

---

### Deploy the stack using Helm

\`\`\`bash
helm upgrade --install chat-stack helm/chat-stack -n chat-system --create-namespace
\`\`\`

This command installs (or upgrades) the entire chat infrastructure into the \`chat-system\` namespace.

---

##  API Verification

### Forward the chat server port

\`\`\`bash
kubectl port-forward svc/chat-server 8080:8080 -n chat-system
\`\`\`

### Test the metrics endpoint

\`\`\`bash
curl http://localhost:8080/metrics
\`\`\`

If everything is running correctly, you should see Prometheus‑formatted metrics.

---

##  Testing the Client

### Connect to the server pod

\`\`\`bash
kubectl get pods -n chat-system
kubectl exec -it <your_server_pod_name> -n chat-system -- bash
\`\`\`

### Run the client

\`\`\`bash
./run-client.sh --server=http://localhost:8080 --list-rooms
\`\`\`

This command lists available chat rooms via the running server.

---

## Monitoring with Prometheus & Grafana

### Get the Minikube IP

\`\`\`bash
minikube ip
\`\`\`

Let’s call it:

\`\`\`
<your_minikube_ip>
\`\`\`

---

### Access Prometheus

Open in your browser:

\`\`\`
http://<your_minikube_ip>:30090/
\`\`\`

To verify that metrics are being collected, run the query:

\`\`\`
up
\`\`\`

You should see your chat server listed as \`UP\`.

---

### Access Grafana

Open in your browser:

\`\`\`
http://<your_minikube_ip>:30300/
\`\`\`

Default credentials:

\`\`\`
Username: admin
Password: admin
\`\`\`

From here, you can:

- Add Prometheus as a data source
- Explore metrics
- Build dashboards for your chat application

---

## Summary

You now have a fully working demo environment that includes:

- A Scala chat server running in Kubernetes  
- A Helm‑managed deployment  
- A client for interacting with the server  
- Prometheus for metrics collection  
- Grafana for visualization  

