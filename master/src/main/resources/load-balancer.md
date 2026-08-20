1. **Load Balancer**
2. **Reverse Proxy / API Gateway**
3. **North-South vs East-West**
4. **Service Mesh**
5. **How they fit together + exam scenarios**

Let's start with **Load Balancer**.

---

# 1. Load Balancer

The simplest definition:

> **A Load Balancer distributes incoming requests across multiple instances of a service.**

Suppose you have:

```text
Client
   │
   ↓
Loan Service
```

Initially:

```text
                ┌──────────────┐
Client ────────→│ Loan Service │
                │  Instance 1  │
                └──────────────┘
```

If traffic increases, one instance may not be enough.

So you deploy:

```text
                 ┌──────────────┐
            ┌───→│ Instance 1   │
            │    └──────────────┘
Client ───→ LB
            │    ┌──────────────┐
            ├───→│ Instance 2   │
            │    └──────────────┘
            │
            │    ┌──────────────┐
            └───→│ Instance 3   │
                 └──────────────┘
```

Now the Load Balancer decides:

```text
Request 1 → Instance 1
Request 2 → Instance 2
Request 3 → Instance 3
Request 4 → Instance 1
...
```

This is **horizontal scaling**.

---

# 2. Why not just expose all instances directly?

You might think:

```text
Client
 ├── Instance 1
 ├── Instance 2
 └── Instance 3
```

But now the client needs to know:

* where each instance is
* which instances are alive
* when a new instance appears
* when an instance disappears

That's undesirable.

We want:

```text
Client
   ↓
One stable endpoint
   ↓
Load Balancer
   ↓
Dynamic instances
```

The client doesn't need to know about the individual servers.

This gives us an important abstraction:

> **The Load Balancer hides the physical topology of service instances.**

---

# 3. The main purpose: Scale + Availability

There are two major benefits.

### Scaling

Instead of:

```text
1 instance
↓
1000 requests/sec
```

we can have:

```text
3 instances
↓
~300 requests/sec each
```

Obviously the actual capacity depends on the workload, but the architectural idea is **horizontal scaling**.

---

### Availability

Suppose:

```text
Instance 1 → healthy
Instance 2 → healthy
Instance 3 → DOWN
```

The Load Balancer can stop sending traffic to Instance 3:

```text
                 ┌──────────────┐
            ┌───→│ Instance 1   │
            │    └──────────────┘
Client ───→ LB
            │    ┌──────────────┐
            └───→│ Instance 2   │
                 └──────────────┘

                 Instance 3 ❌
```

The service can remain available.

So:

> **Load balancing improves both scalability and resilience.**

---

# 4. Health Checks

This is an important senior-level concept.

How does the Load Balancer know Instance 3 is dead?

It performs **health checks**.

For example:

```http
GET /health
```

Instance responds:

```http
200 OK
```

So:

```text
Instance 1 → Healthy
Instance 2 → Healthy
Instance 3 → Unhealthy
```

The Load Balancer removes Instance 3 from its routing pool.

When Instance 3 recovers:

```text
Instance 3 → Healthy
```

the Load Balancer can add it back.

---

# 5. Routing Algorithms

The Load Balancer needs a strategy for choosing an instance.

The simplest one:

### Round Robin

```text
Request 1 → A
Request 2 → B
Request 3 → C
Request 4 → A
Request 5 → B
Request 6 → C
```

Other strategies include:

### Least Connections

Send the request to the instance currently handling the fewest connections.

```text
A → 10 connections
B → 3 connections
C → 7 connections

New request → B
```

### Weighted Routing

Maybe:

```text
A → weight 2
B → weight 1
```

Then A receives roughly twice as much traffic.

Useful when instances don't have identical capacity.

---

# 6. The DNS Problem

This is one of the more interesting points from your material.

Imagine DNS returns:

```text
loan.example.com
        ↓
10.0.0.1
10.0.0.2
10.0.0.3
```

The client may cache these addresses according to DNS TTL.

Now:

```text
10.0.0.2 → DEAD
```

But some clients may still have:

```text
10.0.0.2
```

cached.

They continue sending requests there.

So DNS isn't necessarily good enough as the mechanism for dynamically managing service instances.

---

# 7. Load Balancer solves this

Instead:

```text
loan.example.com
       ↓
Load Balancer
       ↓
 ┌─────┼─────┐
 ↓     ↓     ↓
 A     B     C
```

The client knows only:

```text
Load Balancer IP
```

The LB knows:

```text
A → healthy
B → dead
C → healthy
```

So when B dies:

```text
Client
   ↓
same LB
   ↓
A or C
```

The client doesn't need to know anything changed.

That's a very important architectural abstraction.

---

# 8. Load Balancer doesn't necessarily mean hardware

Historically, you might hear:

> "Load balancer = hardware appliance."

Not anymore.

It can be:

* hardware
* software
* cloud-managed
* Kubernetes component
* reverse proxy with load-balancing capabilities

For example, something like NGINX can perform both:

```text
Reverse Proxy
+
Load Balancing
```

This is why people sometimes confuse the two concepts.

---

# 9. Load Balancer vs Reverse Proxy

This is where your next topic begins.

A **Load Balancer's primary concern** is:

> **Where should this request go among available instances?**

A **Reverse Proxy's primary concern** is:

> **Act as an intermediary between clients and internal services.**

They can overlap technically.

For example:

```text
             ┌─────────────────────┐
Client ────→ │ Reverse Proxy / LB  │
             └──────────┬──────────┘
                        │
                ┌───────┼───────┐
                ↓       ↓       ↓
               S1      S2      S3
```

This single component might:

* terminate TLS
* authenticate
* rate-limit
* route requests
* load-balance
* log traffic

So:

> **"Load balancer" and "reverse proxy" describe different primary roles, even though one component can perform both.**

---

# 10. One subtle but important point

Load balancing does **not** automatically mean your service is highly available.

Imagine:

```text
Load Balancer
      ↓
 ┌────┴────┐
 A         B
```

but both A and B depend on:

```text
      ↓
   One DB
      ↓
   💥 DEAD
```

Your service may still be unavailable.

So the Load Balancer is only **one part of resilience**.

Similarly, if all instances are running on the same physical machine:

```text
        Machine A
    ┌───────────────┐
    │ LB            │
    │ Service 1     │
    │ Service 2     │
    │ Service 3     │
    └───────────────┘
```

then:

```text
Machine A 💥
```

takes everything down.

Real resilience requires thinking about **failure domains**, not just number of instances.

---

# 11. Statelessness matters

There's another important connection.

Suppose you have:

```text
Instance A
Instance B
Instance C
```

and a user's session exists only in memory:

```text
User → Instance A
       ↓
   session = X
```

Next request:

```text
User → Instance B
```

Instance B doesn't know about session X.

That's a problem.

For horizontally scalable services, we generally prefer **stateless application instances**, with shared state stored elsewhere when needed:

```text
                ┌── Instance A ──┐
Client → LB ────┼── Instance B ──┼──→ Redis/DB
                └── Instance C ──┘
```

This makes distributing requests much easier.

---

# 12. Exam mental model

When you hear **Load Balancer**, immediately think:

```text
                Load Balancer
                     │
          ┌──────────┼──────────┐
          ↓          ↓          ↓
       Instance   Instance   Instance
          1          2          3
```

And remember these keywords:

* **Horizontal scaling**
* **Traffic distribution**
* **Health checks**
* **Failure isolation**
* **Routing algorithms**
* **Stable endpoint**
* **Dynamic backend pool**

---

## Your turn

Let's test this before moving to **Reverse Proxy**.

Suppose we have:

```text
Client
   ↓
DNS
   ↓
10.0.0.1
10.0.0.2
10.0.0.3
```

and:

```text
10.0.0.2 → crashes
```

Answer these:

1. **Why can DNS caching cause a problem here?**
2. **How does putting a Load Balancer behind the DNS name improve this?**
3. **What are the two main benefits of having multiple service instances?**
4. **What is the difference between round-robin and least-connections routing?**
5. **Why does statelessness make horizontal scaling easier?**