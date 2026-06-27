# Dockerfile Complete Review

---

# What is a Dockerfile?

A **Dockerfile** is a text file containing instructions that Docker uses to **build a Docker image**.

Think of it as a **recipe**.

* Dockerfile = Recipe
* Docker Image = Cake
* Docker Container = Eating the cake (running application)

---

# Docker Build Process

```
Dockerfile
      │
      ▼
docker build
      │
      ▼
Docker Image
      │
      ▼
docker run
      │
      ▼
Running Container
```

---

# Dockerfile Syntax

A Dockerfile contains one instruction per line.

Example:

```dockerfile
FROM openjdk:21-jdk

WORKDIR /app

COPY target/demo.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java","-jar","app.jar"]
```

---

# Dockerfile Instructions

## 1. FROM

### Purpose

Specifies the **base image**.

Every Dockerfile usually starts with `FROM`.

Example:

```dockerfile
FROM openjdk:21-jdk
```

Meaning:

> Build this image using the official OpenJDK 21 image.

Without a base image, Docker does not know where to start.

---

## 2. WORKDIR

### Purpose

Sets the working directory inside the container.

Example:

```dockerfile
WORKDIR /app
```

If the directory does not exist, Docker creates it.

After this instruction, all commands execute inside:

```
/app
```

---

## 3. COPY

### Purpose

Copies files from the host computer into the Docker image.

Example:

```dockerfile
COPY target/demo.jar app.jar
```

Meaning:

Copy

```
target/demo.jar
```

from your computer into the image as

```
app.jar
```

---

## 4. ADD

### Purpose

Similar to `COPY`, but has extra features.

Example:

```dockerfile
ADD project.zip /app
```

Can:

* Copy files
* Extract local archives automatically
* Download files from URLs (not commonly recommended)

Most projects prefer `COPY` because it is simpler and more predictable.

---

## Difference Between COPY and ADD

| COPY                       | ADD                                     |
| -------------------------- | --------------------------------------- |
| Copies files               | Copies files                            |
| Simple                     | Can extract archives                    |
| Recommended for most cases | Only use when extra features are needed |

---

## 5. RUN

### Purpose

Executes commands **while building the image**.

Example:

```dockerfile
RUN mkdir logs
```

Another example:

```dockerfile
RUN apt-get update
RUN apt-get install -y curl
```

These commands run only during:

```
docker build
```

They are **not executed** every time the container starts.

---

## 6. EXPOSE

### Purpose

Documents which port the application listens on.

Example:

```dockerfile
EXPOSE 8080
```

Important:

`EXPOSE` **does not publish the port**.

It only informs users that the application uses port 8080.

The port is actually published using:

```bash
docker run -p 8080:8080 app
```

---

## 7. ENV

### Purpose

Creates environment variables.

Example:

```dockerfile
ENV JAVA_HOME=/opt/java
```

The variable is available inside the container.

---

## 8. CMD

### Purpose

Provides the **default command** or **default arguments**.

Example:

```dockerfile
CMD ["Hello"]
```

Usually used together with `ENTRYPOINT`.

CMD can be overridden when running the container.

---

## 9. ENTRYPOINT

### Purpose

Defines the **main command** that always runs when the container starts.

Example:

```dockerfile
ENTRYPOINT ["java","-jar","app.jar"]
```

When you run:

```bash
docker run spring-app
```

Docker automatically executes:

```bash
java -jar app.jar
```

---

# RUN vs CMD vs ENTRYPOINT

| Instruction | Executed During | Purpose                                         |
| ----------- | --------------- | ----------------------------------------------- |
| RUN         | docker build    | Executes commands while building the image      |
| CMD         | docker run      | Default command or arguments                    |
| ENTRYPOINT  | docker run      | Main command executed when the container starts |

---

# Build Time vs Run Time

## Build Time

Executed during:

```bash
docker build
```

Instructions:

* FROM
* WORKDIR
* COPY
* ADD
* RUN
* ENV
* EXPOSE (stores metadata)

---

## Run Time

Executed during:

```bash
docker run
```

Instructions:

* ENTRYPOINT
* CMD

---

# Spring Boot Dockerfile

```dockerfile
FROM openjdk:21-jdk

WORKDIR /app

COPY target/demo.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java","-jar","app.jar"]
```

---

# Explanation

### FROM

Use OpenJDK 21 as the base image.

---

### WORKDIR

Set the working directory to:

```
/app
```

---

### COPY

Copy the Spring Boot JAR into the container.

---

### EXPOSE

Document that the application listens on port 8080.

---

### ENTRYPOINT

Start the Spring Boot application.

Equivalent command:

```bash
java -jar app.jar
```

---

# Docker Commands

## Build an Image

```bash
docker build -t spring-app .
```

Explanation:

```
build
```

Build an image.

```
-t
```

Assign a tag (name).

```
.
```

Current directory containing the Dockerfile.

---

## Run a Container

```bash
docker run -p 8080:8080 spring-app
```

Meaning:

```
Host Port : Container Port
```

```
8080 : 8080
```

The application becomes accessible at:

```
http://localhost:8080
```

---

# Complete Docker Workflow

```
Write Dockerfile
        │
        ▼
docker build
        │
        ▼
FROM
WORKDIR
COPY
RUN
EXPOSE
        │
        ▼
Docker Image
        │
        ▼
docker run
        │
        ▼
ENTRYPOINT
CMD
        │
        ▼
Running Container
```

---

# High-Priority Exam Questions

## What is a Dockerfile?

A Dockerfile is a text file containing instructions used to build a Docker image.

---

## What is the purpose of FROM?

Specifies the base image.

---

## What is the purpose of WORKDIR?

Sets the current working directory inside the image.

---

## What is the purpose of COPY?

Copies files from the host machine into the Docker image.

---

## What is the purpose of RUN?

Executes commands during the image build process.

---

## What is the purpose of EXPOSE?

Documents the network port used by the application.

---

## What is the purpose of ENTRYPOINT?

Defines the main command executed whenever the container starts.

---

## What is the purpose of CMD?

Provides the default command or default arguments. It can be overridden when starting the container.

---

## Difference Between RUN, CMD and ENTRYPOINT

| RUN                              | CMD                       | ENTRYPOINT               |
| -------------------------------- | ------------------------- | ------------------------ |
| Build time                       | Run time                  | Run time                 |
| Executes during `docker build`   | Default command/arguments | Main startup command     |
| Cannot be overridden after build | Easily overridden         | Normally always executes |

---

# Memory Tips

## Build Time

```
FROM
WORKDIR
COPY
ADD
RUN
ENV
EXPOSE
```

Think:

**Prepare the image.**

---

## Run Time

```
ENTRYPOINT
CMD
```

Think:

**Start the application.**

---

## Easy Trick

**RUN**

> Build the image.

**ENTRYPOINT**

> Start the application.

**CMD**

> Default options for the application.
