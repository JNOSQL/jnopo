
# JNOPO Game

> Multiplayer Jokenpo (Rock-Paper-Scissors) game powered by Quarkus.

---

## Overview

JNOPO Game is a cloud-ready, real-time implementation of the classic Jokenpo game, built with [Quarkus](https://quarkus.io/). It features WebSocket-based multiplayer gameplay, modern Java 21 features, and seamless deployment to cloud platforms like OpenShift.

> [!TIP]
> This module is designed to be the backend for interactive game clients and can be deployed as a container or native binary.

---

## Features

- **Live multiplayer gameplay** via WebSockets
- **RESTful API** for game management
- **JSON-B** for data serialization
- **Hot reload** in dev mode
- **Native executable** support (GraalVM)
- **Cloud deployment** with Kubernetes/Openshift manifests
- **Dockerfiles** for JVM, native, and micro builds

---

## Getting Started

### Prerequisites

- Java 21+
- Maven 3.8+
- (Optional) Docker, GraalVM, OpenShift CLI (`oc`)

### Run in Development Mode

```bash
./mvnw compile quarkus:dev
```

- Access the Quarkus Dev UI at [http://localhost:8080/q/dev](http://localhost:8080/q/dev)

### Build & Run

```bash
# Build JVM jar
./mvnw package

# Run the app
java -jar target/quarkus-app/quarkus-run.jar
```

#### Build an Über-jar

```bash
./mvnw package -Dquarkus.package.type=uber-jar
java -jar target/*-runner.jar
```

#### Build Native Executable

```bash
./mvnw package -Pnative
# Or, using container build:
./mvnw package -Pnative -Dquarkus.native.container-build=true
./target/coffewithjava-jokenpo-1.0.0-SNAPSHOT-runner
```

---

## Cloud & Container Deployment

### Docker

Dockerfiles for JVM, native, and micro builds are provided in `src/main/docker/`.

### Kubernetes / OpenShift

- Kubernetes manifests: `src/main/k8s/`
- Deploy to OpenShift Developer Sandbox:

---

### Deploy on OpenShift Developer Sandbox (Free)

> [!TIP]
> You can deploy this app for free using the OpenShift Developer Sandbox.

**Steps:**

1. **Create a free account:**
	- Sign up at [OpenShift Developer Sandbox](https://developers.redhat.com/developer-sandbox)

2. **Install the OpenShift CLI (`oc`):**
	- Download and install from [OpenShift CLI Downloads](https://mirror.openshift.com/pub/openshift-v4/clients/oc/latest/)

3. **Log in to your OpenShift Sandbox:**
	```bash
	oc login --token=<YOUR TOKEN> --server=https://<OPENSHIFT URL WITH PORT>
	```

4. **Deploy the application:**
	```bash
	./mvnw install -Dquarkus.kubernetes.deploy=true -DskipTests
	```

---

## API & WebSocket Guides

- [JSON-B](https://quarkus.io/guides/rest-json): JSON Binding support
- [WebSockets](https://quarkus.io/guides/websockets): Real-time communication

---

## Project Structure

```
jnopo-game/
├── src/
│   ├── main/
│   │   ├── java/
│   │   ├── docker/
│   │   ├── k8s/
│   │   └── resources/
│   └── test/
├── pom.xml
├── README.md
```

---

## Admonitions

> [!IMPORTANT]
> This module is stateless and does not persist game data. Integrate with a database or cache for persistent sessions.

> [!NOTE]
> For core game logic, see the `jnopo-core` module.

---

## Community & Support

- [JNOSQL Organization](https://github.com/jnosql)
- Issues and feature requests: Use the GitHub Issues tab

---

## Quick Links

- [Quarkus Documentation](https://quarkus.io/guides/)
- [JNOSQL Main Repository](https://github.com/jnosql/jnopo)

---

_Ready to play? Deploy and challenge your friends!_
