# Reverse Proxy — Detailed Exam Summary

## 1. Core Definition

A **Reverse Proxy** is a server/component that sits between external clients and internal backend services.

```text
Internet
   │
   ↓
Reverse Proxy
   │
   ├── Loan Service
   ├── Risk Service
   └── Payment Service
```

The client talks to the proxy rather than directly to the internal services.

Its primary architectural purpose is:

> **Control and protect the boundary between external clients and internal services.**

This is why it is commonly used as the foundation of an **API Gateway**.

---

# 2. Why Put a Reverse Proxy at the Edge?

Without one:

```text
Internet
   │
   ├──→ Loan Service
   ├──→ Risk Service
   ├──→ Payment Service
   └──→ Customer Service
```

Now external clients need to know about your internal architecture.

With a Reverse Proxy:

```text
                    Internal System
                         │
Internet                 │
   │                     │
   ↓                     │
Reverse Proxy ───────────┼──→ Loan
                         ├──→ Risk
                         ├──→ Payment
                         └──→ Customer
```

Externally, you can expose something like:

```text
api.bank.com
```

while internally you can have:

```text
loan-service.internal
risk-service.internal
payment-service.internal
```

The external client doesn't need to know your internal topology.

---

# 3. North-South Traffic

This is an important exam term.

**North-South traffic** means traffic crossing the boundary of your system/data center.

For example:

```text
             INTERNET
                 │
                 │
           North-South
                 │
                 ↓
          Reverse Proxy
                 │
        ┌────────┼────────┐
        ↓        ↓        ↓
      Loan      Risk    Payment
```

So:

```text
External Client
      ↓
Internal System
```

is **North-South traffic**.

The Reverse Proxy sits at this boundary.

---

# 4. Reverse Proxy as an API Gateway

An API Gateway is commonly built using reverse-proxy capabilities.

For example:

```text
                    API Gateway
                         │
          ┌──────────────┼──────────────┐
          ↓              ↓              ↓
      /loans          /risk         /payments
          ↓              ↓              ↓
       Loan            Risk          Payment
```

The gateway can determine:

```text
/api/loans/*    → Loan Service
/api/risk/*     → Risk Service
/api/payments/* → Payment Service
```

So the Reverse Proxy provides the **intermediary mechanism**, while the API Gateway is the broader **API-facing architectural role**.

---

# 5. Cross-Cutting Concerns

This is one of the most important parts of the section.

A Reverse Proxy is a good place for **generic infrastructure concerns** that apply across many services.

The key word is:

> **Generic**

The proxy shouldn't care about Loan business rules or Risk business rules.

But it can care about things such as:

```text
Authentication
Rate limiting
TLS
Logging
Traffic routing
```

---

# 6. Rate Limiting

Suppose a client sends:

```text
10,000 requests/sec
```

You don't necessarily want every microservice to independently implement:

```text
RateLimiter
```

Instead:

```text
Client
  ↓
Reverse Proxy
  ↓
Rate Limiter
  ↓
Services
```

For example:

```text
100 requests/minute/client
```

Once the limit is exceeded:

```http
429 Too Many Requests
```

The important point:

> Rate limiting is **generic infrastructure behavior**, not Loan/Risk/Payment business logic.

---

# 7. API Key Validation

Suppose clients must send:

```http
X-API-Key: abc123
```

The Reverse Proxy can validate it:

```text
Client
   ↓
API Key
   ↓
Reverse Proxy
   │
   ├── Invalid → 401/403
   │
   └── Valid
         ↓
      Service
```

This prevents every service from having to independently implement the same basic API-key validation mechanism.

Again:

> **Generic security concern → reasonable proxy responsibility.**

---

# 8. Logging

The proxy sees every external request:

```text
Client
  ↓
Proxy
  ↓
Service
```

So it can provide standardized infrastructure-level logging:

```text
timestamp
client
HTTP method
path
status
latency
request ID
```

For example:

```text
POST /api/loans
status=201
latency=125ms
requestId=abc123
```

This gives you a consistent view of incoming traffic.

---

# 9. TLS Termination

Another important responsibility is **TLS termination**.

Instead of making the edge connection:

```text
Client
   ↓ HTTPS
Loan Service
```

you can have:

```text
Client
   ↓ HTTPS
Reverse Proxy
   ↓
Internal Service
```

The Reverse Proxy handles the TLS connection and certificates.

This is called:

> **TLS termination**

The proxy performs the TLS handshake and decrypts the traffic before forwarding it.

---

# 10. Important Security Nuance

TLS termination at the edge doesn't necessarily mean:

> "Internal traffic should always be plain HTTP."

For sensitive systems, internal traffic can also be encrypted.

For example:

```text
Client
   ↓ HTTPS
Gateway
   ↓ mTLS
Loan
   ↓ mTLS
Risk
```

This is one area where **Service Mesh** becomes useful.

So don't memorize:

> Reverse Proxy = HTTPS outside, HTTP inside.

Instead memorize:

> **The proxy can terminate TLS at the external boundary; internal encryption is an independent architectural decision.**

---

# 11. The Most Important Part: Dumb Pipes, Smart Endpoints

This is probably the **most exam-worthy concept** in this section.

The proxy should generally be:

> **Smart enough to handle infrastructure concerns, but dumb enough not to contain business logic.**

Good:

```text
Proxy
 ├── TLS
 ├── Authentication
 ├── Rate limiting
 ├── Logging
 └── Routing
```

Services:

```text
Loan Service
 └── Loan business logic

Risk Service
 └── Risk business logic

Payment Service
 └── Payment business logic
```

This keeps the business intelligence inside the services.

---

# 12. What is a "Smart Pipe"?

A **smart pipe** is when the intermediary starts making business decisions.

For example:

```text
Client
   ↓
API Gateway
   ↓
Check Loan
   ↓
Call Risk
   ↓
Check Risk Score
   ↓
Call Payment
   ↓
Apply Business Rule
   ↓
Return Result
```

Now the Gateway knows a lot about the business.

That's dangerous.

---

# 13. Why is Smart Pipe dangerous?

Suppose you have:

```text
             Gateway
          /     |     \
        Loan   Risk   Payment
```

And the Gateway contains:

```text
if riskScore > X
and loanStatus == Y
and paymentStatus == Z
then approveLoan()
```

Now the Gateway is coupled to all three domains.

A change in Risk might require a Gateway change.

A change in Payment might require a Gateway change.

A change in Loan might require a Gateway change.

You have created:

> **Centralized organizational coupling.**

---

# 14. Deployment Coupling

This is particularly important for microservices.

Imagine:

```text
Loan Team
Risk Team
Payment Team
       ↓
   API Gateway
```

If the Gateway contains business logic for all three, then:

```text
Risk changes
    ↓
Gateway must change
    ↓
Gateway must be tested
    ↓
Gateway deployed
```

Now Risk cannot truly evolve independently.

This contradicts an important microservice goal:

> **Independent deployability.**

---

# 15. Protocol Transformation — The Nuance

Your source specifically mentions things like:

```text
SOAP → REST
```

The important point isn't:

> "Protocol transformation is always bad."

It can be useful.

The concern is **where the transformation and business behavior live**.

For example, if you have a legacy system:

```text
Modern Service
      ↓
Integration Adapter
      ↓
SOAP Legacy System
```

a dedicated adapter can be perfectly reasonable.

The problem is turning the central API Gateway into:

```text
Gateway
 ├── SOAP conversion
 ├── REST conversion
 ├── business rules
 ├── orchestration
 ├── data transformation
 └── service-specific logic
```

Now your gateway becomes a **central integration engine**.

That's essentially moving toward the problems we discussed with **ESBs**.

---

# 16. Call Aggregation — Another Nuance

Suppose a mobile application needs:

```text
Loan
Customer
Payment
```

The Gateway could theoretically do:

```text
Mobile
  ↓
Gateway
 ├──→ Loan
 ├──→ Customer
 └──→ Payment
        ↓
   Combined response
```

This isn't automatically wrong.

The important distinction is:

### Simple representation aggregation

> "Fetch these resources and combine their responses."

Potentially reasonable.

### Business orchestration

> "Call Loan, inspect its result, then decide whether to call Risk, then inspect Risk, then call Payment according to business rules."

Now you've moved business logic into the gateway.

**That's the dangerous part.**

---

# 17. Reverse Proxy vs ESB

This connects directly to your previous question.

### Reverse Proxy

```text
Client
  ↓
Proxy
  ↓
Service
```

Focus:

> **Edge traffic management**

Typical responsibilities:

* TLS
* authentication
* rate limiting
* routing
* logging
* load balancing

### ESB

```text
System A
   ↓
  ESB
   ↓
System B
```

Focus:

> **Enterprise integration**

Typical responsibilities:

* protocol conversion
* message transformation
* orchestration
* routing
* integration

If you turn your Reverse Proxy into:

```text
Routing
+
Transformation
+
Orchestration
+
Business Logic
+
Protocol Conversion
```

you start moving toward an **ESB-like centralized integration layer**.

That's exactly the architectural danger Newman is warning about.

---

# 18. The Golden Rule

For your exam, remember this:

> **Put generic, cross-cutting infrastructure concerns at the proxy; keep business intelligence inside the endpoints/services.**

### Good

```text
                 Proxy
           ┌──────┼──────┐
           ↓      ↓      ↓
          TLS   Auth   Rate Limit
                  │
                  ↓
              Services
           ┌──────┼──────┐
           ↓      ↓      ↓
         Loan    Risk   Payment
         Logic   Logic  Logic
```

### Bad

```text
                 Proxy
           ┌──────┼──────┐
           ↓      ↓      ↓
         TLS     Auth   Business Logic
                         ↓
                      Loan Rules
                      Risk Rules
                      Payment Rules
```

---

# 19. Exam Questions You Should Be Able to Answer

### Q: What is the primary role of a Reverse Proxy?

> To act as an intermediary at the system boundary, controlling and routing traffic between external clients and internal services while hiding the internal topology.

### Q: What is North-South traffic?

> Traffic crossing the boundary between external clients and the internal system.

### Q: Give examples of cross-cutting concerns suitable for a Reverse Proxy.

> TLS termination, rate limiting, API-key validation, authentication policies, logging, and routing.

### Q: Why shouldn't business logic be placed in the proxy?

> Because it creates centralized coupling between services and teams, turning the proxy into a bottleneck and reducing independent deployability.

### Q: What does "Dumb Pipes, Smart Endpoints" mean?

> Infrastructure components should handle generic communication concerns while business logic remains inside the services that own that business domain.

---

## Final mental model

Keep this picture in your head:

```text
                     INTERNET
                         │
                         │
                  North-South
                         │
                         ↓
               ┌──────────────────┐
               │  Reverse Proxy   │
               │                  │
               │  TLS             │
               │  Authentication  │
               │  Rate Limiting   │
               │  Logging         │
               │  Routing         │
               └────────┬─────────┘
                        │
             ┌──────────┼──────────┐
             ↓          ↓          ↓
           Loan        Risk      Payment
             │          │          │
          Smart       Smart      Smart
         Endpoint    Endpoint   Endpoint
```

**Proxy = generic infrastructure intelligence.**

**Services = business intelligence.**

That's the core principle behind this entire section.
