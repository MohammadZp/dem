# 1. TLS — Transport Layer Security

TLS provides three main security properties:

> **Encryption + Integrity + Server Authentication**

Imagine:

```text
Client                         Server
  │                              │
  │────────── TLS ──────────────→│
  │                              │
```

Instead of sending:

```text
password=123
```

as readable data, TLS encrypts the communication.

---

## 2. What does TLS actually protect?

### A. Confidentiality

Someone intercepting the network traffic shouldn't be able to read it.

```text
Client ─── 🔒 encrypted ───→ Server
```

### B. Integrity

An attacker shouldn't be able to modify:

```text
amount=100
```

into:

```text
amount=100000
```

without detection.

### C. Authentication

The client can verify:

> "Am I actually talking to the server I intended to talk to?"

This is where **certificates** come in.

---

# 3. The Certificate

Suppose you connect to:

```text
https://bank.com
```

The server presents a certificate saying roughly:

```text
I am bank.com

Public Key:
XYZ...

Signed by:
Trusted CA
```

The client verifies the certificate.

The certificate is signed by a **Certificate Authority (CA)** that the client trusts.

```text
             CA
             │
             │ signs
             ↓
      Server Certificate
             │
             ↓
          Server
```

So the client can establish:

> "This server really owns the identity `bank.com`."

---

# 4. What happens during TLS?

Very simplified:

```text
Client                         Server
  │                              │
  │──── ClientHello ───────────→│
  │                              │
  │←── ServerHello + Certificate │
  │                              │
  │   Verify certificate         │
  │                              │
  │──── Key establishment ──────→│
  │                              │
  │════ Encrypted communication ═│
```

The handshake establishes shared cryptographic keys.

After that, application data is encrypted.

---

# 5. Important: TLS doesn't mean "the data is encrypted forever"

TLS protects the **connection** between two endpoints.

For example:

```text id="p0cyw5"
Client
  │
  │ TLS
  ↓
API Gateway
```

The connection between Client and Gateway is protected.

But what happens after the Gateway?

```text id="q8g4qy"
Client
   │
   │ TLS
   ↓
Gateway
   │
   │ ???
   ↓
Loan Service
```

You need to separately decide how the internal connection is protected.

This leads us to mTLS.

---

# 6. mTLS — Mutual TLS

**mTLS = Mutual TLS**

Normal TLS generally authenticates:

```text
Client ─────────→ Server
          verifies
          Server
```

With mTLS:

```text
Client ←────────→ Server
       both authenticate
```

Both sides present certificates.

```text id="9g4qbp"
Loan Service
     │
     │ "Prove who you are."
     ↓
Risk Service

Risk Service
     │
     │ "You prove who you are too."
     ↓
Loan Service
```

So both services establish their identities.

---

# 7. TLS vs mTLS

### Normal TLS

```text
Client
  │
  │ "Prove you're the real server."
  ↓
Server
```

The client verifies the server.

### mTLS

```text
Client
  │ ←── authenticate ──→ │
Server
```

Both verify each other.

---

# 8. Why is mTLS useful for Microservices?

Imagine your internal network:

```text id="5q9v3j"
Loan
 ↓
Risk
 ↓
Payment
```

Without strong service identity, you might only know:

```text
10.0.0.15
```

But IP addresses aren't really identities.

With mTLS:

```text id="f2t8yq"
Loan
Certificate:
service=loan

       ↓ mTLS

Risk
Certificate:
service=risk
```

Risk can verify:

> "This request actually came from the Loan service."

This gives you **service identity**.

---

# 9. mTLS + Encryption

mTLS provides both:

### Authentication

```text
Who are you?
```

### Encryption

```text
Can anyone read our communication?
No.
```

So:

```text id="q3z2jg"
Loan
  │
  │ 🔒 encrypted
  │
  │ 🪪 authenticated
  ↓
Risk
```

---

# 10. Why Service Mesh Loves mTLS

Without a Service Mesh, every Java service might need to manage:

```text id="f0i5ae"
Certificates
Private keys
Certificate rotation
TLS configuration
Trust stores
Handshake configuration
```

Imagine doing that across:

```text id="x0qz6k"
100 microservices
```

Painful.

With a Service Mesh:

```text id="6m2t7a"
Loan App
    ↓
Loan Proxy
    │
    │ mTLS
    ↓
Risk Proxy
    ↓
Risk App
```

The proxies handle the networking security.

Your Java application can simply communicate normally with its local proxy.

---

# 11. Where Does the Certificate Come From?

This is where the **CA (Certificate Authority)** comes in.

Conceptually:

```text id="6z8x1b"
             CA
             │
       ┌─────┴─────┐
       ↓           ↓
 Loan Certificate  Risk Certificate
       ↓           ↓
   Loan Proxy    Risk Proxy
```

The CA establishes trust.

The Loan proxy can verify:

```text
"Risk's certificate was signed by a CA I trust."
```

And Risk can verify:

```text
"Loan's certificate was signed by a CA I trust."
```

---

# 12. Certificate Rotation

Certificates expire.

A good Service Mesh can automatically:

```text id="3q4v9a"
Generate certificate
       ↓
Distribute certificate
       ↓
Use certificate
       ↓
Rotate before expiration
```

This is another major reason mTLS is attractive through a Service Mesh.

You don't want developers manually managing certificates for every service.

---

# 13. API Gateway + TLS vs Service Mesh + mTLS

Now connect this to what we just learned.

### External traffic

```text id="7i6jkl"
Mobile App
    │
    │ HTTPS / TLS
    ↓
API Gateway
```

The Gateway authenticates the external connection.

### Internal traffic

```text id="e7u4z0"
Loan Proxy
    │
    │ mTLS
    ↓
Risk Proxy
```

The Service Mesh provides mutual authentication between services.

So a common architecture is:

```text id="f8m7q2"
Internet
   │
   │ TLS
   ↓
API Gateway
   │
   │
   ↓
Loan Proxy
   │
   │ mTLS
   ↓
Risk Proxy
   │
   │ mTLS
   ↓
Payment Proxy
```

---

# 14. One Important Terminology Detail

You may hear:

> **TLS termination**

This means the TLS connection ends at a component.

For example:

```text id="f9q1pr"
Client
  │
  │ HTTPS
  ↓
Gateway
  ↑
TLS terminates here
```

The Gateway decrypts the request.

If the Gateway then talks to Loan over HTTP:

```text id="p7d4jj"
Client
   │ HTTPS
   ↓
Gateway
   │ HTTP
   ↓
Loan
```

the internal connection isn't encrypted.

Alternatively:

```text id="l6k2o0"
Client
   │ HTTPS
   ↓
Gateway
   │ mTLS
   ↓
Loan
```

Now you have encryption and mutual authentication internally too.

---

# 15. Exam Mental Model

Memorize this:

### TLS

> **"I need to know that I'm talking securely to the server."**

```text
Client ── TLS ──→ Server
                  ↑
             Server identity
```

### mTLS

> **"Both sides need to prove who they are."**

```text
Client ←── mTLS ──→ Server
   ↑                    ↑
identity             identity
```

And in microservices:

> **mTLS gives services a cryptographically verifiable identity and encrypts their communication.**

---

## Final cheat sheet

|                       | TLS           | mTLS              |
| --------------------- | ------------- | ----------------- |
| Encryption            | ✅             | ✅                 |
| Integrity             | ✅             | ✅                 |
| Server authentication | ✅             | ✅                 |
| Client authentication | Usually ❌     | ✅                 |
| Common use            | Browser → API | Service → Service |
| Service Mesh          | Possible      | **Very common**   |

The key distinction:

> **TLS: "Prove who the server is."**
> **mTLS: "Both of us prove who we are."**
