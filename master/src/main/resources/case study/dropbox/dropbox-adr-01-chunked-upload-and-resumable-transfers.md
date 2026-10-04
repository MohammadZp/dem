# ADR 01: Client-Side Block Chunking, SHA-256 Fingerprinting, and Resumable Uploads

## Status
**Accepted**

## Context & Problem Statement
The Cloud Storage and Video Sharing Platform must support large file uploads (up to 50 GB) while serving over 1,000,000 daily active users (1M+ DAU). 

Uploading 50 GB files as single HTTP POST request payloads introduces severe operational vulnerabilities:
1. **API Gateway & Proxy Payload Limits:** AWS API Gateway enforces a strict 10 MB payload limit, while NGINX defaults to 1 MB – 100 MB max body sizes.
2. **Bandwidth & CPU Inefficiency:** Routing multi-gigabyte byte streams through application microservices creates unnecessary network hops and saturates application server CPU/RAM.
3. **Network Instability & Retry Waste:** If an HTTP connection drops after uploading 45 GB (90% complete), forcing the user to restart the upload from 0% leads to terrible user experience and extreme bandwidth waste.

## Considered Options
1. **Single Monolithic Multipart HTTP POST to File Service:** Streams entire raw file through API Gateway to backend storage.
2. **Server-Side Chunking Stream:** Streams continuous file bytes to backend, where an application worker splits bytes into S3 objects.
3. **Client-Side Block Chunking with Pre-Signed S3 URLs and SHA-256 Fingerprinting (Chosen Solution):** Desktop/Web client splits files into 5 MB chunks, calculates SHA-256 hashes, requests pre-signed S3 URLs, and uploads chunks directly to S3.

## Decision Outcome
**Chosen Option:** **Client-Side Block Chunking with Pre-Signed S3 URLs and SHA-256 Fingerprinting.**

### Architectural Mechanism:
1. **Client Block Chunking:** The client splits any file exceeding 5 MB into fixed 5 MB block chunks.
2. **Content-Addressed Fingerprinting:** The client computes a SHA-256 hash for each block. The hash serves as the immutable block identifier (`block_id`).
3. **Initiation & Pre-Signed URLs:** The client calls `POST /files/initiate` passing the list of block hashes. The File Service checks which blocks already exist in the system and returns AWS S3 Pre-Signed `PUT` URLs (15-minute TTL) *only for missing blocks*.
4. **Direct Parallel Uploads:** The client uploads block chunks directly to Amazon S3 in parallel (up to 4 concurrency threads).
5. **Resumable State Management:** If network connectivity breaks, the client re-initiates the upload. The File Service compares local block hashes with stored metadata and returns pre-signed URLs only for the remaining un-uploaded blocks.
6. **Trust-But-Verify Validation:** Upon client upload completion (`POST /files/complete`), the File Service executes asynchronous `HEAD` requests to S3 to verify object presence and ETag checksums before committing the file manifest to DynamoDB.

## Consequences

### Positive Impacts
* **Resiliency & Zero Loss:** Network drops only lose the active 5 MB block in flight rather than multi-gigabyte file transfers.
* **Cost & Server Efficiency:** Bypasses backend app servers for raw byte ingestion, reducing application compute costs by over 80%.
* **Scalability:** Amazon S3 natively handles petabyte-scale throughput directly from edge client uploads.

### Negative Impacts / Trade-offs
* **Client Complexity:** Requires logic in desktop/web clients for file partitioning, hashing, retry loops, and parallel thread management.
* **Orphaned Block Cleanup:** Aborted uploads leave uncommitted 5 MB blocks in S3. Requires an automated S3 Lifecycle rule to purge uncommitted blocks after 24 hours.