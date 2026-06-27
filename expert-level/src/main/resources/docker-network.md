# Docker Networking Review

---

# What is Docker Networking?

Docker networking allows **containers to communicate** with:

- Other containers
- The host machine
- External networks (Internet)

Each container has its own network interface and IP address.

---

# Why is Docker Networking Needed?

Without networking:

- Containers cannot communicate.
- Applications like Spring Boot cannot connect to databases.
- Users cannot access applications running inside containers.

---

# Common Network Types

| Network | Purpose |
|----------|---------|
| bridge | Default network for containers on one host |
| host | Container shares the host's network |
| none | No network access |
| custom bridge | User-created network for communication between containers |

---

# Bridge Network

## What is it?

The **default Docker network**.

Containers on the same bridge network can communicate with each other.

Example:

```
Spring Boot Container
        │
        │
Bridge Network
        │
        │
MySQL Container
```

---

# Host Network

## What is it?

The container shares the host computer's network.

- No separate container IP.
- Uses the host's ports directly.

Example:

```
Host
 ├── Spring Boot
 ├── MySQL
```

---

# None Network

## What is it?

The container has **no network connection**.

It cannot:

- Access the Internet
- Connect to other containers
- Receive requests

---

# Port Mapping

Containers have their own ports.

To access them from the host, use:

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

Now the application is available at:

```
http://localhost:8080
```

---

# Expose vs Publish

## EXPOSE

```dockerfile
EXPOSE 8080
```

- Documents that the application listens on port 8080.
- Does **not** make the port accessible from outside the container.

---

## Publish Port

```bash
docker run -p 8080:8080 spring-app
```

Actually maps the container's port to the host so external clients can connect.

---

# Docker Compose Networking

Docker Compose automatically creates a **private bridge network**.

Example:

```yaml
services:

  app:

  mysql:
```

Both containers are on the same network.

The Spring Boot application can connect to MySQL using the service name:

```text
mysql
```

instead of an IP address.

Example:

```properties
spring.datasource.url=jdbc:mysql://mysql:3306/demo
```

Notice:

Use:

```
mysql
```

Not:

```
localhost
```

because each container has its own localhost.

---

# Container Communication

```
Spring Boot
      │
      │
Docker Network
      │
      │
MySQL
```

Containers communicate using:

- Service names (Compose)
- Container names
- IP addresses (less common)

---

# Important Exam Point

Inside Docker:

```
localhost
```

means:

> **The current container**

NOT another container.

---

# Docker Network Commands

Create a network:

```bash
docker network create my-network
```

List networks:

```bash
docker network ls
```

Inspect a network:

```bash
docker network inspect my-network
```

Connect a container:

```bash
docker network connect my-network container-name
```

---

# High-Priority Exam Questions

## What is Docker networking?

Docker networking enables communication between containers, the host machine, and external networks.

---

## What is the default Docker network?

The **bridge** network.

---

## What is the purpose of port mapping?

Port mapping allows users on the host machine to access services running inside a container.

Example:

```bash
docker run -p 8080:8080 spring-app
```

---

## What is the difference between EXPOSE and `-p`?

| EXPOSE | `-p` |
|---------|------|
| Documents the port used by the application | Publishes the port to the host |
| Does not make the port accessible | Makes the application accessible from outside the container |

---

## Why does Docker Compose use service names?

Docker Compose creates a private network where services can communicate using their service names instead of IP addresses.

Example:

```
mysql
```

instead of:

```
192.168.x.x
```

---

## Why should you not use `localhost` to connect to another container?

Because `localhost` refers to **the current container**, not another container.

---

# Memory Tips

## Network Types

```
Bridge  → Default network
Host    → Uses host's network
None    → No networking
```

---

## EXPOSE vs -p

```
EXPOSE
    ↓
Documentation only

-p
    ↓
Actually opens the port
```

---

## Container Communication

```
Container A
      │
      ▼
Docker Network
      ▲
      │
Container B
```

Use:

- Service name (`mysql`)
- Container name

Avoid using:

```
localhost
```

unless referring to the same container.