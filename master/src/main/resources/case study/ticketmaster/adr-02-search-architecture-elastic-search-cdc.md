# ADR 02: Search Engine Architecture via ElasticSearch and Change Data Capture (CDC)

* **Status:** Accepted
* **Date:** 2026-10-04
* **Deciders:** System Architecture Team
* **Context:** Enterprise Booking System — Ticketmaster Scenario

---

## Context and Problem Statement
Users search for events using combinations of text terms (performer name, event title), categories, dates, and geographic location proximity. Executing relational database queries with wildcard string matching (`LIKE '%term%'`) forces full table scans across millions of event records, driving DB CPU utilization to 100% and causing unacceptable search latencies during peak traffic.

---

## Decision Driver
* Sub-100ms response time for complex search queries across global user bases.
* Support multi-byte characters and full-text tokenized search.
* Offload search traffic entirely from the primary relational database.

---

## Considered Options
1. **Option A: Direct PostgreSQL SQL Queries with B-Tree/GIN Indexes** (Rejected: Slow for multi-field wildcards and geospatial joins).
2. **Option B: ElasticSearch Cluster populated via Change Data Capture (CDC)** (**Chosen**).
3. **Option C: ElasticSearch as Primary Datastore** (Rejected: ElasticSearch lacks robust multi-document ACID transaction guarantees needed for ticketing).

---

## Decision Outcome
**Chosen Option:** **Option B — ElasticSearch with Debezium CDC over Apache Kafka**.

1. **Primary Store:** PostgreSQL remains the immutable source of truth for events, venues, performers, and tickets.
2. **Asynchronous Indexing:** Database write logs (`wal`) are streamed by **Debezium CDC** to **Apache Kafka**. A worker service consumes Kafka topics and updates document indexes in **ElasticSearch**.
3. **Query Execution:** The dedicated **Search Service** executes search queries against ElasticSearch using inverted text indexes and quadtree/geohash spatial indexes.

---

## Positive Consequences
* **Sub-20ms Search Latency:** Inverted indexes deliver rapid full-text and location matching.
* **Decoupled Primary Database:** PostgreSQL is completely shielded from high-volume search traffic (reads > 100:1 ratio).
* **Geospatial & Multi-Language Support:** Native ElasticSearch tokenizers handle multi-byte characters and lat/long radius filters seamlessly.

## Negative Consequences & Mitigation
* **Eventual Consistency:** A small propagation lag (<1 second) exists between database writes and search index updates.
* **Mitigation:** Acceptable per non-functional requirements — immediate consistency is required for seat booking, while search discovery tolerates slight eventual consistency.
