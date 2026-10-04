# System Design — Enterprise Ticket Booking Platform (Ticketmaster)

This repository contains the complete architectural specification, C4 models, Architectural Decision Records (ADRs), and system design documentation for Question 01 (**Enterprise Booking System Design**), grounded in the **Hello Interview** system design breakdown by an ex-Meta Staff Engineer.

---

## 1. Executive Summary & Business Scenario

The objective is to design a global, enterprise-grade ticket booking platform capable of supporting millions of concurrent users searching for events and reserving seats during peak flash sales.

### Primary Functional Requirements
1. **Search Events:** Users can search events by keyword, performer, venue, date range, category, and location.
2. **View Event Details & Seat Map:** Users can view detailed event info, venue layouts, and real-time seat availability maps.
3. **Two-Phase Booking Flow:**
   * **Phase 1 (Reserve):** Hold a selected seat exclusively for 10 minutes.
   * **Phase 2 (Confirm):** Complete credit card payment via Stripe within the 10-minute window.

### Key Non-Functional Requirements
* **Strong Consistency for Ticketing:** Zero double-booking tolerance.
* **High Availability & Low Latency for Search/View:** Sub-50ms search response; highly available event page views.
* **Surge Scalability:** Ability to absorb 100x traffic spikes during popular ticket drops without cascading failures.
* **High Read-to-Write Ratio:** Estimated at 100:1 read-to-write traffic ratio.

---

## 2. High-Level System Architecture & Component Overview

```
[ Client (Web/Mobile) ]
          │ (HTTPS / SSE)
          ▼
   [ API Gateway ] ───────► [ Virtual Waiting Queue (Redis Sorted Set) ]
          │
  ┌───────┼────────────────────────┐
  ▼       ▼                        ▼
[Event] [Search]              [Booking Service]
[CRUD ] [Service]                     │
  │       │                ┌──────────┴──────────┐
  │       ▼                ▼                     ▼
  │  [ElasticSearch]  [Redis Lock]      [Stripe Payment API]
  │                        │
  └───────┬────────────────┘
          ▼
    [PostgreSQL] ──(CDC)──► [Kafka] ──► [ElasticSearch]
```

---

## 3. Core Architectural Highlights

1. **Two-Phase Reservation with Distributed Locks (ADR 01):**
   * Eliminates database row locks and background Cron job lag.
   * Uses Redis `SETNX` with a 600-second TTL (`ticket_lock:{id}`).
   * Automatically evicts expired holds if checkout is abandoned.

2. **Search Engine with Change Data Capture (ADR 02):**
   * Uses ElasticSearch inverted indices for full-text and geospatial search.
   * Synchronized from PostgreSQL via Debezium CDC and Kafka event streams.

3. **Virtual Waiting Queue (ADR 03):**
   * Absorbs instant 100x traffic surges using Redis Sorted Sets.
   * Admits users at a steady rate, pushing real-time queue position via Server-Sent Events (SSE).

---

## 4. Deliverables in this Package

* **`ticketmaster-c4-diagrams.xml`:** Importable Draw.io XML file containing 4 complete pages:
  * **Page 1:** C1 — System Context Diagram
  * **Page 2:** C2 — Container Diagram
  * **Page 3:** C3 — Component Diagram (Booking Service Deep Dive)
  * **Page 4:** C4 — Detailed Execution Flow (2-Phase Ticket Reservation & Payment Sequence)
* **`adr-01-distributed-lock-for-seat-reservations.md`:** Architectural Decision Record for Redis Distributed Locks.
* **`adr-02-search-architecture-elastic-search-cdc.md`:** Architectural Decision Record for ElasticSearch + CDC synchronization.
* **`adr-03-virtual-waiting-queue-for-peak-traffic-surges.md`:** Architectural Decision Record for Virtual Waiting Rooms.
* **`readme.md`:** Comprehensive system design overview document.

---

## 5. How to Import the Diagrams into Draw.io

1. Open [draw.io](https://app.diagrams.net/).
2. Click **File** -> **Open From** -> **Device...** (or Drag & Drop).
3. Select `ticketmaster-c4-diagrams.xml`.
4. Switch between the 4 tabs at the bottom to view C1, C2, C3, and C4 diagrams.
