# ADR 01: Distributed In-Memory Lock (Redis) for Temporary Seat Reservations

* **Status:** Accepted
* **Date:** 2026-10-04
* **Deciders:** System Architecture Team
* **Context:** Enterprise Booking System — Ticketmaster Scenario

---

## Context and Problem Statement
When booking tickets for high-demand events, the platform must support a **two-phase reservation flow**:
1. A user selects a seat and holds it exclusively for 10 minutes while entering payment details.
2. The user confirms payment, or the hold expires and the seat becomes available to other buyers.

Using relational database row locks (`SELECT FOR UPDATE`) or setting a `status = 'reserved'` column with background Cron job cleanups introduces serious issues:
* **Cron Lag ($n$ Delta):** Cron jobs running on fixed intervals (e.g., every 10 minutes) leave seats held up to 19 minutes total.
* **Database Contention:** Thousands of concurrent requests attempting row updates on PostgreSQL during ticket drops cause lock escalation and high latency.

---

## Decision Driver
* Prevent double-booking (strong consistency requirement during final purchase).
* Low latency and high throughput under sudden traffic surges.
* Automatic, deterministic expiration of unconfirmed holds after exactly 600 seconds.

---

## Considered Options
1. **Option A: PostgreSQL Row Locks + Cron Job Cleanup** (Rejected: High DB lock contention, stale seat holds).
2. **Option B: Redis Distributed Lock with Key TTL** (**Chosen**).
3. **Option C: In-Memory Application State inside Booking Service** (Rejected: Fails when horizontally scaling across multiple service instances).

---

## Decision Outcome
**Chosen Option:** **Option B — Redis Distributed Lock with Key TTL**.

When a user initiates a reservation for `ticket_id`:
1. The Booking Service executes an atomic `SETNX ticket_lock:{ticket_id} {user_id} EX 600` in Redis.
2. If `SETNX` returns `1`, the lock is acquired, and a 10-minute timer begins.
3. When fetching available seats, the Event CRUD Service filters out ticket IDs present in the Redis lock cache.
4. Upon successful payment callback from Stripe, an ACID SQL transaction updates the PostgreSQL ticket state to `booked` and deletes the Redis lock key.
5. If payment is abandoned, Redis automatically evicts the key after 600 seconds, releasing the seat instantly.

---

## Positive Consequences
* **Zero DB Write Contention:** Reservations bypass disk writes entirely until final payment confirmation.
* **Deterministic 10-Minute Expiration:** Redis TTL guarantees key eviction down to the millisecond without background cron processes.
* **Horizontal Scalability:** Stateless Booking Service instances share a unified, consistent view of active holds in Redis Cluster.

## Negative Consequences & Mitigation
* **Risk of Cache Node Failure:** If Redis fails without replication, active hold keys could be lost.
* **Mitigation:** Redis Sentinel / Cluster setup with read replicas. Additionally, PostgreSQL ACID guarantees during final checkout serve as the ultimate source of truth (`UPDATE tickets SET status='booked' WHERE id=? AND status='available'`), preventing double bookings even if cache lock state is disrupted.
