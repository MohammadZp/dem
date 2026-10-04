# ADR 03: Virtual Waiting Queue (Redis Sorted Sets) for Flash Traffic Protection

* **Status:** Accepted
* **Date:** 2026-10-04
* **Deciders:** System Architecture Team
* **Context:** Enterprise Booking System — Ticketmaster Scenario

---

## Context and Problem Statement
For major event ticket drops (e.g., Taylor Swift concert, World Cup final), millions of concurrent users arrive within seconds. Allowing all users directly onto the seat map UI triggers hundreds of thousands of concurrent database and cache requests, overwhelming system capacity, causing cascading failures, and creating a poor user experience where all available seats instantly disappear.

---

## Decision Driver
* System stability and 24/7 availability during unpredictable 100x traffic surges.
* Fair, transparent queuing for ticket buyers.
* Controlled, rate-limited admission to seat selection microservices.

---

## Considered Options
1. **Option A: Direct Admission with Horizontal Auto-Scaling** (Rejected: Auto-scaling cannot spin up compute instances fast enough to absorb instantaneous 100x surges).
2. **Option B: Virtual Waiting Queue using Redis Sorted Sets & Server-Sent Events (SSE)** (**Chosen**).

---

## Decision Outcome
**Chosen Option:** **Option B — Virtual Waiting Queue**.

1. **Traffic Throttling:** When an event is flagged as a "High Demand Drop", the API Gateway redirects incoming users to a Virtual Waiting Room page.
2. **Queue Management:** The user's arrival timestamp and session ID are stored in a **Redis Sorted Set** (`ZADD queue:{event_id} timestamp user_id`).
3. **Rate-Limited Admission:** Background queue workers pop $N$ users per second off the sorted set and issue a signed admission JWT.
4. **Real-time Client Push:** The server pushes queue position updates to the client via a persistent **Server-Sent Events (SSE)** connection. Once admitted, the client presents the JWT to access the seat selection UI.

---

## Positive Consequences
* **Backend Protection:** Downstream microservices operate at calibrated, predictable load capacities without risk of crash.
* **Improved User Experience:** Users receive clear status feedback ("You are #1,420 in line, estimated wait 2 mins") rather than error pages or unresponsive UI.
* **Fairness:** Ensures first-come, first-served or randomized queue allocation.

## Negative Consequences & Mitigation
* **SSE Connection Volume:** Holding hundreds of thousands of concurrent SSE connections consumes gateway file descriptors.
* **Mitigation:** Lightweight NGINX / Envoy edge proxies handle idle SSE connection state efficiently.
