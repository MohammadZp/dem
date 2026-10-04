# System Design Specification: Cloud Storage & Video Sharing Platform (Dropbox / YouTube Hybrid)

## Executive Summary
This document defines the production system design for a high-scalability **Cloud File Storage and Video Sharing System** capable of serving **1,000,000+ Daily Active Users (1M+ DAU)**. The platform transitions from a legacy monolithic architecture to an event-driven microservices topology, supporting multi-gigabyte file storage (up to 50 GB), sub-second cross-device folder synchronization, content-addressed block deduplication, and asynchronous multi-bitrate video transcoding (HLS).

---

## 1. Requirements & System Boundaries

### 1.1 Functional Requirements
* **Large File Uploads & Resumability:** Support uploading files up to 50 GB with automatic resumability on network failures.
* **Cross-Device Delta Synchronization:** Automatically sync changes in local folders (Mac `FSEvents`, Windows `FileSystemWatcher`) to remote storage and connected peer devices with sub-second latency.
* **Video Upload, Processing & Delivery:** Ingest user-submitted videos, process them asynchronously into multi-bitrate formats (1080p, 720p, 480p), and stream via adaptive video playback.
* **Block-Level Deduplication:** Index and deduplicate 5 MB blocks globally to optimize cloud storage costs.

### 1.2 Non-Functional Requirements
* **High Scalability (1M+ DAU):** Horizontally scalable stateless microservices capable of handling peak traffic surges.
* **CAP Theorem Trade-Off (Availability over Consistency for Viewing; High Data Integrity for Sync):**
  * *File/Video Playback:* Prioritize High Availability (AP) over immediate consistency — short delays in propagating new files to distant regions are acceptable.
  * *Folder Sync & File Integrity:* Strong data integrity and consistency (CP) for folder states to prevent file corruption.
* **Low Latency Performance:**
  * Sub-200ms API response time for metadata operations.
  * Low-latency video playback start times (< 1.5s) using Edge Content Delivery Networks (CDN).

---

## 2. Architecture Models (C4 Model Overview)

The architecture is formally modeled across 4 C4 levels in `dropbox-c4-diagrams.xml`:

1. **C1 — System Context:** Illustrates interaction between Cloud Users, Desktop/Mobile Clients, the core Dropbox/Video Platform, AWS S3 Object Storage, Global CDN, and External Auth Gateways.
2. **C2 — Container Diagram:** Breaks the system into decoupled microservices:
   * **API Gateway & Load Balancer (NGINX/Envoy)**
   * **File & Block Microservice (Go)**
   * **Metadata Sync Microservice (Java / Spring Boot)**
   * **Video Transcoding Worker Cluster (Go / FFmpeg)**
   * **Apache Kafka Event Bus**
   * **Amazon DynamoDB Metadata Store & Redis In-Memory Cache**
   * **Amazon S3 Object Storage & CloudFront CDN**
3. **C3 — Component Diagram:** Details the inner modules of the **File & Block Microservice**: Upload REST Controller, Block Deduplication Engine, Pre-Signed URL Generator, Trust-But-Verify Worker, and File Metadata Manager.
4. **C4 — Execution Sequence:** Details the 12-step sequence execution for client block chunking, pre-signed URL acquisition, direct S3 upload, trust-but-verify confirmation, and Kafka-triggered video transcoding.

---

## 3. Core Data Schema (Amazon DynamoDB)

### 3.1 `FileMetadata` Table
* **`file_id`** (Partition Key — UUID)
* **`owner_id`** (String / Foreign Key)
* **`folder_id`** (String / Index Key)
* **`file_name`** (String)
* **`file_size_bytes`** (Number)
* **`mime_type`** (String — e.g., `video/mp4`, `application/pdf`)
* **`block_manifest`** (List of SHA-256 block hash strings)
* **`sync_version`** (Number / Monotonic increment)
* **`created_at`** (Timestamp)
* **`updated_at`** (Timestamp)

### 3.2 `BlockIndex` Table
* **`block_hash`** (Partition Key — SHA-256 String)
* **`s3_object_key`** (String — e.g., `s3://blocks/e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855`)
* **`size_bytes`** (Number — 5,242,880)
* **`reference_count`** (Atomic Integer)
* **`status`** (Enum: `PENDING`, `COMMITTED`)

### 3.3 `UserSyncCursor` Table
* **`user_id`** (Partition Key)
* **`folder_id`** (Sort Key)
* **`last_sync_cursor`** (BigInt / Incremental Event ID)
* **`updated_at`** (Timestamp)

---

## 4. Primary API Specifications

### 4.1 Initiate File Upload
`POST /api/v1/files/initiate`
* **Request Header:** `Authorization: Bearer <JWT>`
* **Request Body:**
```json
{
  "folder_id": "f_98231",
  "file_name": "presentation_4k.mp4",
  "file_size_bytes": 104857600,
  "block_hashes": [
    "8f434346648f6b96df89dda901c5176b10a6d83961dd3c1ac88b59b2dc327aa4",
    "ecccbc75c3712d185a6d12973938a1622fec3d7748bedf07338e32c71324835a"
  ]
}
```
* **Response (200 OK):**
```json
{
  "file_id": "doc_77192",
  "missing_blocks": [
    {
      "block_hash": "8f434346648f6b96df89dda901c5176b10a6d83961dd3c1ac88b59b2dc327aa4",
      "presigned_url": "https://s3.us-east-1.amazonaws.com/dropbox-blocks/8f434346648f?X-Amz-Algorithm=AWS4-HMAC-SHA256..."
    }
  ],
  "existing_blocks": [
    "ecccbc75c3712d185a6d12973938a1622fec3d7748bedf07338e32c71324835a"
  ]
}
```

### 4.2 Complete Upload & Commit Manifest
`POST /api/v1/files/complete`
* **Request Body:**
```json
{
  "file_id": "doc_77192",
  "uploaded_block_hashes": [
    "8f434346648f6b96df89dda901c5176b10a6d83961dd3c1ac88b59b2dc327aa4",
    "ecccbc75c3712d185a6d12973938a1622fec3d7748bedf07338e32c71324835a"
  ]
}
```
* **Response (200 OK):** `{"status": "COMMITTED", "sync_cursor": 10493}`

### 4.3 Fetch Folder Changes (Delta Sync)
`GET /api/v1/sync/changes?folder_id=f_98231&cursor=10492`
* **Response (200 OK):**
```json
{
  "current_cursor": 10493,
  "changes": [
    {
      "event_type": "FILE_MUTATED",
      "file_id": "doc_77192",
      "file_name": "presentation_4k.mp4",
      "updated_blocks": ["8f434346648f6b96df89dda901c5176b10a6d83961dd3c1ac88b59b2dc327aa4"]
    }
  ]
}
```

### 4.4 Stream Transcoded Video
`GET /api/v1/videos/stream/{file_id}/master.m3u8`
* **Response:** Returns HLS Master Playlist referencing 1080p, 720p, and 480p variant streams.

---

## 5. Architectural Decision Records (ADRs) Attached
* **`dropbox-adr-01-chunked-upload-and-resumable-transfers.md`**: Client-side block chunking, SHA-256 fingerprinting, pre-signed URLs, and resumable upload protocol.
* **`dropbox-adr-02-metadata-sync-and-block-level-deduplication.md`**: Content-Addressed Block Storage (CAS), global deduplication, and cursor-based delta sync.
* **`dropbox-adr-03-asynchronous-video-transcoding-pipeline.md`**: Event-driven Kafka transcoding pipeline, FFmpeg segmenting, HLS streaming, and CDN edge delivery.