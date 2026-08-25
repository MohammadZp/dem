# YouTube App System Architecture & Design Guide

This document outlines the architectural components ("parts") of the YouTube application and details how they are designed to support massive scale, low latency, and high availability, based on Hello Interview's system design breakdown.

---

## 1. Key System Requirements & Scale
To design YouTube's infrastructure effectively, the architecture must support the following parameters:
* **Scale**: **100 million daily active users** and **1 million video uploads per day** [6].
* **Maximum Video Size**: **256 GB** (or 12 hours) [6].
* **Performance Quality**: **Low latency streaming** (aiming for less than 500ms to get the first pixels on screen) even in low bandwidth environments [8, 26].
* **Availability vs. Consistency (CAP Theorem)**: Highly prioritizing **availability over consistency**. It is completely acceptable if a video uploaded in Germany takes minutes or hours to propagate and become visible to a user in the US [7, 8].

---

## 2. Core Architectural Components ("The Parts")
The system is divided into several primary stateless microservices, storage layers, and caching networks:

```
[ Client App ] ---> [ API Gateway ] ---> [ Video Service ] ---> [ Video Metadata DB ]
     |                                                                ^
     v (Direct Upload / Stream)                                       |
 [ S3 / GCS ] <--- [ S3 Notifications ] ---> [ Workers ] -------------+
     |                                   (Chunker / Transcoders)
     v
 [  CDN  ] <--- Caches popular video chunks & manifest files
```

### A. The Client Application
The client (mobile app or browser) is responsible for:
1. **Multipart Uploading**: Splitting large video files into chunk payloads (typically 5–10 MB) and uploading them directly to object storage via **pre-signed URLs** to bypass server limits [17, 18, 25].
2. **Adaptive Bitrate Streaming (ABR)**: Using client-side logic to periodically assess current network bandwidth and adaptively request the appropriate resolution chunks (e.g., dynamically switching from 4K down to 720p if the network degrades) [31, 32].

### B. API Gateway
* Serves as the single entry point for API requests, managing **authentication, rate limiting, and request routing** to downstream services [14].
* **Why we bypass it for video files**: Standard API gateways (like AWS API Gateway) have strict maximum payload limits (e.g., **10 MB**) [16]. Thus, we cannot route large 256 GB video payloads through it [12, 16].

### C. Video Service (Stateless)
* Processes metadata requests, such as saving titles and descriptions, and generating the necessary **pre-signed URLs** from S3 for client uploads [14, 18].
* Designed to be stateless so it can **scale horizontally** [38].

### D. Data & Blob Storage (S3 / GCS & Metadata DB)
1. **Blob Storage (Amazon S3 / Google Cloud Storage)**:
   * Highly, almost infinitely, scalable storage used to keep both the raw uploaded video bytes, processed streaming chunks, and the video manifest files [15, 33, 38].
2. **Video Metadata Database**:
   * Stores high-level metadata (e.g., video title, description, creator, upload status, and ordered lists of S3 paths) [11, 15, 24].
   * This DB has relatively low write traffic (1 million writes/day is ~12 writes per second), meaning it can comfortably live in relational systems like PostgreSQL or a NoSQL system like DynamoDB [15, 38, 39].
   * To scale, this database is **sharded by Video ID**, with a **Global Secondary Index (GSI) on User ID** to fetch all videos uploaded by a specific user [39].

### E. Asynchronous Processing Pipeline (Workers)
* **S3 Notifications**: Triggered automatically when S3 finishes stitching a multi-part upload together. This prevents the system from having to trust client-side success callbacks [20].
* **Chunker Worker**: Downloads the completed video from S3 and splits it into small **2 to 10-second playback chunks** [23].
* **Transcoding Workers**: Processes these chunks in parallel, encoding them into various container formats, bitrates, and resolutions (from 240p up to 4K) [29, 30].
* Both Chunker and Transcoding workers are stateless and scale horizontally based on CPU/Memory consumption [40].

### F. Content Delivery Network (CDN)
* Geographically distributed servers that act as a cache close to the end-users [33].
* The CDN caches **popular video chunks** and **manifest files** [33]. This allows users (e.g., in Europe fetching a video stored in a US-West bucket) to download content with sub-500ms latency [32, 33].

---

## 3. Deep Dive: Dynamic Workflows

### The Double-Chunking Design Strategy
An elegant element of the YouTube design is **chunking the video twice**, as both stages require optimization for entirely different goals:
1. **Upload Chunks (Client-side)**: Optimized to minimize HTTP request overhead and handle network interrupts. These are large (5–10 MB) raw byte chunks without any regard for video frame boundaries [25].
2. **Download/Streaming Chunks (Server-side)**: Optimized for smooth playback. These are small (2–10 seconds) segments sliced precisely at **video keyframes** to allow instant playback initialization and immediate adaptive resolution switches [25].

### Manifest Files & Streaming Protocols
To execute playback, streaming protocols like **HLS (Apple)** or **DASH (Open Source)** are used [35]. The core mechanic revolves around:
1. **The Manifest File**: A simple index file (JSON, YAML, or XML) stored in S3/CDN that maps out the list of all video chunks in chronological order across all supported resolutions [33].
2. **Adaptive Playback**: The client first requests the lightweight manifest file [34]. As the video plays, client-side code adaptively fetches subsequent chunks based on real-time network checks [31]. If bandwidth drops, the player seamlessly fetches the next 2-second chunk from a lower resolution (e.g., 240p) mapped in the manifest [26, 31].

---

## 4. Key Design Trade-Offs

| Component / Goal | Design Approach Chosen | Trade-off / Benefit |
| :--- | :--- | :--- |
| **Video Upload** | Direct S3 multi-part upload via Pre-signed URLs [17, 18] | Bypasses the 10 MB API Gateway limit [16, 17] and saves massive network bandwidth on intermediate servers. |
| **Consistency** | Eventual Consistency (AP System) [7, 8] | High availability for playback and uploading [7, 8]. Videos take a few minutes to propagate worldwide but the app never crashes due to lag. |
| **Latency** | CDN Caching of Manifest & Popular Chunks [33] | Sub-500ms video startup times [8, 33]. It is financially expensive but essential for the core user experience [40]. |
| **Worker Coordination** | S3 Event Notifications -> Queue -> Workers [20, 23] | High reliability. Avoids trusting client-side callbacks to initiate transcoding [20]. |
