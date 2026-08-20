> **North-South = outside ↔ inside**
> **East-West = service ↔ service**

---

# 1. The Big Picture

Imagine your microservice system:

```text
                         INTERNET
                            │
                            │
                       North-South
                            │
                            ↓
                    ┌──────────────┐
                    │ API Gateway  │
                    └──────┬───────┘
                           │
              ┌────────────┼────────────┐
              ↓            ↓            ↓
           Loan          Risk        Payment
              ↕            ↕            ↕
           East-West traffic
```

There are **two fundamentally different types of communication** happening.

### North-South

```text
Internet → Your system
Your system → Internet
```

### East-West

```text
Loan → Risk
Risk → Payment
Payment → Notification
```

And we generally use different infrastructure for each.

---

# 2. North-South Traffic

Think of your system as a building.

```text
               OUTSIDE
                  │
                  ↓
          ┌───────────────┐
          │ System        │
          │               │
          │ Microservices │
          └───────────────┘
```

Traffic crossing the boundary is **North-South**.

For example:

```text
Mobile App
    ↓
Internet
    ↓
API Gateway
    ↓
Loan Service
```

The API Gateway is the **front door** of your system.

It can handle:

* TLS termination
* authentication
* API key validation
* rate limiting
* routing
* logging
* request filtering

So:

> **API Gateway = edge traffic management**

---

# 3. East-West Traffic

Now imagine the request is already inside your system.

```text
Loan Service
     ↓
Risk Service
     ↓
Payment Service
```

This is **East-West traffic**.

It's communication **between internal services**.

The API Gateway isn't necessarily the right tool for this.

Instead:

> **Service Mesh manages East-West communication.**

---

# 4. Why Not Put Everything Through the API Gateway?

You might initially think:

```text
Loan
 ↓
API Gateway
 ↓
Risk
```

Then:

```text
Risk
 ↓
API Gateway
 ↓
Payment
```

It seems simple.

But imagine 100 services:

```text
Service A ──┐
Service B ──┤
Service C ──┤
Service D ──┼──→ Central Gateway
Service E ──┤
Service F ──┤
...          │
Service Z ──┘
```

Now **every internal request** goes through one central component.

That creates problems.

---

# 5. Additional Network Hop

Suppose Loan wants Risk.

Without a central proxy:

```text
Loan
  │
  │ network
  ↓
Risk
```

One network communication.

With a central proxy:

```text
Loan
  │
  ↓
Central Proxy
  │
  ↓
Risk
```

Now you've added another hop.

For a single request this may seem insignificant.

But imagine:

```text
Loan
 ↓
Risk
 ↓
Payment
 ↓
Customer
 ↓
Notification
```

and every call goes through a central gateway.

You can end up with:

```text
Loan
 ↓
Gateway
 ↓
Risk
 ↓
Gateway
 ↓
Payment
 ↓
Gateway
 ↓
Customer
 ↓
Gateway
 ↓
Notification
```

The centralized proxy becomes part of almost every internal interaction.

---

# 6. Central Proxy Can Become a Bottleneck

Imagine:

```text
              Central Proxy
             /      |      \
            /       |       \
          100       100      100
        services  services  services
```

Every internal call passes through it.

The proxy now needs:

* massive throughput
* high availability
* scaling
* monitoring
* failure handling

And if it goes down:

```text
Central Proxy ❌
      ↓
Internal communication breaks
      ↓
Many services affected
```

You've created a potentially critical centralized dependency.

---

# 7. Service Mesh's Solution

Instead of having **one central proxy**, distribute proxies alongside the services.

For example:

```text
┌──────────────────┐
│ Loan Pod         │
│                  │
│ Loan App         │
│    ↓             │
│ Envoy Proxy      │
└────────┬─────────┘
         │
         │
┌────────▼─────────┐
│ Risk Pod         │
│                  │
│ Envoy Proxy      │
│    ↓             │
│ Risk App         │
└──────────────────┘
```

Now:

```text
Loan App
   ↓
Local Proxy
   ↓
Network
   ↓
Risk Proxy
   ↓
Risk App
```

There isn't one giant central proxy handling all internal traffic.

---

# 8. Why "Sidecar"?

The proxy runs beside the application.

For example, a Kubernetes Pod:

```text
┌──────────────────────────┐
│ Pod                      │
│                          │
│ ┌──────────────┐         │
│ │ Loan App     │         │
│ └──────┬───────┘         │
│        │                 │
│ ┌──────▼───────┐         │
│ │ Envoy        │         │
│ │ Sidecar      │         │
│ └──────────────┘         │
└──────────────────────────┘
```

So every service instance has its own networking proxy.

---

# 9. Client-Side Load Balancing

This is an important feature.

Suppose Risk has:

```text
Risk-1
Risk-2
Risk-3
```

Loan's local proxy can decide:

```text
Loan
 ↓
Envoy
 ├──→ Risk-1
 ├──→ Risk-2
 └──→ Risk-3
```

It can perform load balancing locally.

For example:

```text
Request 1 → Risk-1
Request 2 → Risk-2
Request 3 → Risk-3
```

This is commonly called **client-side load balancing** because the proxy close to the caller makes the backend selection.

---

# 10. Mutual TLS

The Service Mesh can also provide **mTLS** between services.

Instead of:

```text
Loan ──HTTP──→ Risk
```

you can have:

```text
Loan Proxy
    │
    │ mTLS
    ↓
Risk Proxy
```

Both sides authenticate each other.

This gives you:

> **Service identity + encrypted internal communication**

And importantly, the Loan and Risk applications don't have to implement certificate management themselves.

---

# 11. Distributed Tracing

Suppose:

```text
Client
 ↓
Gateway
 ↓
Loan
 ↓
Risk
 ↓
Payment
```

A Service Mesh can help propagate tracing information across these calls.

For example:

```text
Request ID = ABC123

Gateway
   ↓
Loan
   ↓
Risk
   ↓
Payment
```

Then your observability system can reconstruct the request path.

You can discover:

```text
Loan:     50ms
Risk:    800ms  ← bottleneck
Payment:  40ms
```

This is extremely useful in distributed systems.

---

# 12. Why Local Proxies Matter

Compare these two designs.

### Central Proxy

```text
Loan
 ↓
Central Proxy
 ↓
Risk
```

### Service Mesh

```text
Loan
 ↓
Local Proxy
 ↓
Network
 ↓
Risk Proxy
 ↓
Risk
```

The mesh still has network communication, obviously.

The important point is that you're not forcing every internal interaction through a **centralized intermediary**.

The proxies are distributed with the services.

---

# 13. Don't Misunderstand "No Extra Network Hop"

The phrase:

> "without adding extra network hops"

needs to be understood carefully.

The Service Mesh doesn't magically eliminate the network hop between Loan and Risk.

There is still:

```text
Loan → Risk
```

over the network.

What it avoids is introducing a **central intermediary hop**:

```text
Loan → Central Proxy → Risk
```

Instead, the proxy is local:

```text
Loan App
 ↓
Local Proxy
 ↓
Risk Proxy
 ↓
Risk App
```

The sidecars add local processing, but they avoid sending traffic through a distant centralized proxy.

---

# 14. Full Architecture

Now combine everything:

```text
                         INTERNET
                            │
                            │
                      NORTH-SOUTH
                            │
                            ↓
                  ┌─────────────────┐
                  │  API Gateway    │
                  │                 │
                  │ TLS             │
                  │ Auth            │
                  │ Rate Limiting   │
                  │ Routing         │
                  └────────┬────────┘
                           │
              ┌────────────┼────────────┐
              ↓            ↓            ↓

        ┌───────────┐ ┌───────────┐ ┌───────────┐
        │ Loan Pod  │ │ Risk Pod  │ │Payment Pod│
        │           │ │           │ │           │
        │ Loan App  │ │ Risk App  │ │Payment    │
        │    ↓      │ │    ↓      │ │    ↓      │
        │  Envoy    │←→│  Envoy   │←→│  Envoy   │
        └───────────┘ └───────────┘ └───────────┘
              ↑            ↑            ↑
              └────────────┴────────────┘
                    EAST-WEST
                     TRAFFIC
```

So:

```text
             North-South
                  ↓
            API Gateway
                  ↓
          ┌───────────────┐
          │ Microservices │
          └───────────────┘
                  ↕
             East-West
                  ↕
            Service Mesh
```

---

# 15. What Does Each One Own?

This is the most useful way to memorize it.

| Concern                           | API Gateway   | Service Mesh |
| --------------------------------- | ------------- | ------------ |
| External traffic                  | ✅             | ❌            |
| Internal service traffic          | ❌/not primary | ✅            |
| TLS termination                   | ✅             | —            |
| mTLS between services             | —             | ✅            |
| API authentication                | ✅             | —            |
| Rate limiting external APIs       | ✅             | —            |
| Service-to-service load balancing | —             | ✅            |
| Distributed tracing               | Can help      | ✅            |
| Traffic splitting                 | Can help      | ✅            |
| Hide internal architecture        | ✅             | ❌            |
| Business logic                    | ❌             | ❌            |

The last row is very important:

> **Neither API Gateway nor Service Mesh should become your business-logic layer.**

---

# 16. The Mental Model

Think of your system as a building.

### API Gateway = Front Door

```text
Outside
   ↓
🚪 Front Door
   ↓
Inside
```

It decides:

* Who can enter?
* Where should they go?
* How much traffic can they send?

### Service Mesh = Internal Road Network

Once you're inside:

```text
Loan ───── Risk
 │          │
 │          │
Payment ─ Notification
```

The mesh manages how services communicate:

* secure communication
* service identity
* routing
* load balancing
* tracing
* retries
* timeouts

---

# 17. Exam-Level Answer

If they ask:

> **Why do we use an API Gateway for North-South traffic and a Service Mesh for East-West traffic?**

A strong answer:

> **North-South traffic crosses the system boundary, so an API Gateway provides a centralized edge for concerns such as authentication, TLS termination, rate limiting, and external API routing. East-West traffic occurs between internal services, where a centralized proxy would introduce unnecessary network hops and become a bottleneck. A Service Mesh distributes proxies alongside service instances, allowing capabilities such as mTLS, client-side load balancing, and tracing to be applied to service-to-service communication without centralizing all internal traffic.**

And the one-line version:

> **API Gateway controls the front door; Service Mesh controls the internal communication network.**
