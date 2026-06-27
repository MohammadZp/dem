# Dockerfile, Spring Boot JAR & Docker Compose

---

# Docker

## What is Docker?

Docker is a platform that allows applications to run inside **containers**.

A container packages:

- Application code
- Dependencies
- Runtime
- Configuration

This ensures the application runs the same way on every machine.

---

## What is a Container?

A **container** is a lightweight, isolated environment that runs an application along with all of its dependencies.

Unlike a virtual machine, containers share the host operating system kernel, making them faster and more efficient.

---

## What is a Docker Image?

A **Docker Image** is a read-only template used to create containers.

Think of it as a blueprint.

One image can create many containers.

---

# Dockerfile

## What is a Dockerfile?

A **Dockerfile** is a text file containing instructions that Docker uses to build an image.

Each instruction becomes a layer in the image.

---

## Why use a Dockerfile?

A Dockerfile allows you to:

- Build images automatically
- Package applications consistently
- Share the build process
- Avoid manual configuration

---

# Common Dockerfile Instructions

## FROM

Specifies the base image.

Example:

```dockerfile
FROM openjdk:21-jdk
```

Every Dockerfile usually begins with `FROM`.

---

## WORKDIR

Sets the working directory inside the container.

```dockerfile
WORKDIR /app
```

---

## COPY

Copies files from the host computer into the container.

```dockerfile
COPY target/demo.jar app.jar
```

---

## EXPOSE

Documents which port the application uses.

```dockerfile
EXPOSE 8080
```

---

## ENTRYPOINT

Specifies the command executed when the container starts.

```dockerfile
ENTRYPOINT ["java","-jar","app.jar"]
```

---

# Spring Boot Dockerfile

Example:

```dockerfile
FROM openjdk:21-jdk

WORKDIR /app

COPY target/demo.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java","-jar","app.jar"]
```

---

## Explanation

### FROM

Uses the official OpenJDK image.

```dockerfile
FROM openjdk:21-jdk
```

---

### WORKDIR

Creates and switches to:

```
/app
```

---

### COPY

Copies the Spring Boot JAR into the container.

```dockerfile
COPY target/demo.jar app.jar
```

---

### EXPOSE

Indicates that the application listens on port **8080**.

```dockerfile
EXPOSE 8080
```

---

### ENTRYPOINT

Runs the Spring Boot application.

```dockerfile
ENTRYPOINT ["java","-jar","app.jar"]
```

---

# Building an Image

Command:

```bash
docker build -t spring-app .
```

Explanation:

- `build` → Build an image
- `-t` → Assign a name (tag)
- `.` → Current directory (contains the Dockerfile)

---

# Running a Container

Command:

```bash
docker run -p 8080:8080 spring-app
```

Explanation:

```
Host Port : Container Port
```

```
8080 : 8080
```

The application becomes available at:

```
http://localhost:8080
```

---

# Docker Compose

## What is Docker Compose?

Docker Compose is a tool for defining and running **multiple containers** using a single configuration file.

Instead of running several `docker run` commands, you define all services in one file.

---

## Why use Docker Compose?

Docker Compose simplifies development by starting multiple services together, such as:

- Spring Boot
- MySQL
- PostgreSQL
- Redis

using one command.

---

## Compose File

Docker Compose uses:

```
docker-compose.yml
```

or (newer versions)

```
compose.yaml
```

---

# Example Docker Compose

```yaml
version: "3"

services:

  app:
    build: .
    ports:
      - "8080:8080"

  mysql:
    image: mysql:8
    environment:
      MYSQL_ROOT_PASSWORD: root
      MYSQL_DATABASE: demo
```

---

## Explanation

### services

Defines all containers.

```yaml
services:
```

---

### build

Builds an image using the Dockerfile.

```yaml
build: .
```

---

### image

Uses an existing Docker image.

```yaml
image: mysql:8
```

---

### ports

Maps host ports to container ports.

```yaml
ports:
  - "8080:8080"
```

---

### environment

Defines environment variables.

```yaml
environment:
  MYSQL_ROOT_PASSWORD: root
```

---

# Docker Compose Commands

Start all containers:

```bash
docker compose up
```

Stop all containers:

```bash
docker compose down
```

Build images:

```bash
docker compose build
```

---

# Dockerfile vs Docker Compose

| Dockerfile | Docker Compose |
|------------|----------------|
| Builds one image | Manages multiple containers |
| Defines how an image is built | Defines how containers work together |
| Uses Dockerfile syntax | Uses YAML |

---

# Docker Image vs Container

| Image | Container |
|--------|-----------|
| Blueprint | Running instance |
| Read-only | Executable |
| Created from Dockerfile | Created from an image |

---

# High-Priority Exam Questions

## What is Docker?

Docker is a platform for developing, packaging, and running applications inside containers.

---

## What is a container?

A lightweight, isolated environment that contains an application and all of its dependencies.

---

## What is a Docker image?

A read-only template used to create containers.

---

## What is a Dockerfile?

A text file containing instructions for building a Docker image.

---

## What is the purpose of the `FROM` instruction?

It specifies the base image for the Docker image.

---

## What is the purpose of `COPY`?

Copies files from the host machine into the Docker image.

---

## What is the purpose of `EXPOSE`?

Documents the network port used by the application.

---

## What is the purpose of `ENTRYPOINT`?

Specifies the command that runs when the container starts.

---

## What is Docker Compose?

Docker Compose is a tool for defining and running multiple Docker containers using a YAML configuration file.

---

## Why is Docker Compose useful?

It allows multiple services (such as Spring Boot and MySQL) to be started, stopped, and managed together with a single command.

---

# Memory Tips

## Docker Workflow

```
Dockerfile
      ↓
docker build
      ↓
Docker Image
      ↓
docker run
      ↓
Container
```

---

## Dockerfile Instructions

| Instruction | Purpose |
|-------------|---------|
| FROM | Base image |
| WORKDIR | Working directory |
| COPY | Copy files |
| EXPOSE | Application port |
| ENTRYPOINT | Startup command |

Remember:

- **FROM** → Start with an image.
- **WORKDIR** → Choose where to work.
- **COPY** → Copy your application.
- **EXPOSE** → Tell Docker which port is used.
- **ENTRYPOINT** → Run the application.

---

## Docker Compose

Think:

> **One file to run many containers.**

Example:

Spring Boot + MySQL

```
docker compose up
```

Starts everything automatically.