# ADR 02: Metadata Synchronization, Block-Level Deduplication, and Delta Sync

## Status
**Accepted**

## Context & Problem Statement
A core requirement of cloud file synchronization across multiple devices (Mac, Windows, iOS, Web) is ensuring that local file changes are synced instantly and accurately to remote storage and peer devices.

Key challenges include:
1. **High Storage Costs:** Storing full copies of duplicate files uploaded by different users wastes petabytes of storage.
2. **Network Saturation on Small Edits:** If a user edits a 2 GB file by changing 10 lines of text, re-transferring the entire 2 GB file burns user bandwidth and introduces sync lag.
3. **Cross-Device State Consistency:** Multiple devices syncing simultaneously must detect conflicts and receive real-time delta updates without flooding servers with database queries.

## Considered Options
1. **File-Level Storage with Full Re-Uploads:** Upload full file on every change and store complete file copies per user.
2. **File-Level Storage with Differential Sync (Rsync-like):** Compute byte diffs between file versions server-side.
3. **Block-Level Content-Addressed Storage (CAS) with Delta Sync & Cursor-Based Polling (Chosen Solution):** Index blocks by SHA-256 hash, share identical blocks across storage, and sync only changed block hashes.

## Decision Outcome
**Chosen Option:** **Block-Level Content-Addressed Storage (CAS) with Delta Sync & Cursor-Based Polling.**

### Architectural Mechanism:
1. **Global Block-Level Deduplication:**
   * Every 5 MB block is indexed in DynamoDB by its SHA-256 hash.
   * If User B uploads a block identical to one already uploaded by User A, the File Service detects the SHA-256 match, increments the block reference counter, and skips the S3 upload entirely (instant upload).
2. **Delta Synchronization (Delta Sync):**
   * When a local file is modified, native operating system file watchers (`FSEvents` on macOS, `FileSystemWatcher` on Windows) alert the client app.
   * The client re-chunks the file and recalculates SHA-256 block hashes. Only blocks with modified SHA-256 hashes are uploaded.
3. **Cursor-Based Delta Sync:**
   * The Metadata Sync Service maintains an incremental sequence number (`sync_cursor`) for every folder tree in DynamoDB.
   * Peer clients periodically long-poll `GET /sync/changes?cursor=10492`. The service returns only the specific block manifest mutations occurring after that cursor, reducing sync latency from minutes to milliseconds.

## Consequences

### Positive Impacts
* **Bandwidth Savings:** Editing a large video or document transfers only the modified 5 MB block, reducing network load by up to 99%.
* **Massive Storage Cost Reduction:** Content-addressed deduplication eliminates redundant block storage in Amazon S3 across all system users.
* **Sub-Second Multi-Device Sync:** Cursor-based delta responses keep metadata payloads lightweight (< 2 KB), allowing instant peer device updates.

### Negative Impacts / Trade-offs
* **Metadata Overhead:** Managing billions of block index mappings requires a high-throughput, horizontally scalable NoSQL database (Amazon DynamoDB).
* **Reference Count Complexity:** Deleting a file requires atomic reference decrementing to ensure a block in S3 is only deleted when zero users reference its SHA-256 hash.