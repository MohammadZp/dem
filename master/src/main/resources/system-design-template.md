# Comprehensive System Design Interview Template & Checklist

This template is structured around the systematic 6-step flow of thinking recommended by former Meta Staff Engineers [3, 4]. You can use this blueprint to tackle any complex system design problem (e.g., YouTube, Netflix, Spotify, or Twitter) in technical interviews [1].

---

## Step 1: Requirements & Scale Estimation
*Suggested Time: 5–10 Minutes*

The goal of this phase is to define the boundaries of the system and clarify any ambiguities [3]. Never start designing without explicit confirmation of these requirements from your interviewer [3].

### A) Functional Requirements
List the exact features the system must support from the user's perspective ("Users should be able to...") [3]:
- [ ] **Core Feature 1:** (e.g., "Users should be able to upload videos") [5]
- [ ] **Core Feature 2:** (e.g., "Users should be able to watch/stream videos") [6]
- [ ] **Secondary Features:** (e.g., commenting, liking, or subscribing—align with the interviewer if these are in scope or deferred) [39]

### B) Non-Functional Requirements
Define the system qualities that will shape your architecture [3]:
- [ ] **CAP Theorem Trade-off (Availability vs. Consistency):** Which one takes priority? (e.g., for YouTube, availability is prioritized over immediate consistency -> Eventual Consistency is acceptable) [7, 8].
- [ ] **Latency Targets:** What is the maximum acceptable response or start-up time? (e.g., video streaming playback startup under 500 milliseconds) [8].
- [ ] **Reliability & Fault Tolerance:** How should the system handle node/component failures? [3]
- [ ] **Scalability & Specific Constraints:** (e.g., supporting extremely large file payloads up to 256 GB, or handling low-bandwidth environments) [6, 8, 9].

### C) Scale Estimation (Back-of-the-Envelope Math)
Ask key metrics from the interviewer or make reasonable assumptions to guide downstream engineering decisions [6]:
- [ ] **Daily Active Users (DAU):** (e.g., 100 Million) [6]
- [ ] **Write Volume / QPS:** (e.g., 1 Million uploads per day = ~12 uploads per second average, with a peak of 3x–5x) [6, 9]
- [ ] **Read Volume / QPS:** (e.g., 100 Million views per day = ~1150 views per second average) [9]
- [ ] **Storage Requirements:** Compute storage for 1 year and 5 years based on maximum/average file sizes (e.g., 1M uploads/day * average size) [39].
- [ ] **Bandwidth Estimation:** Ingress (upload) and Egress (download) bandwidth based on QPS and payload sizes.

---

## Step 2: Core Data Entities
*Suggested Time: 3–5 Minutes*

Identify the primary nouns of the system [3]. Do not draft complete database schemas yet; focus purely on the main objects that will be persisted in your databases and exchanged via your APIs [10].

- [ ] **Entity 1:** (e.g., `Video` - representing raw bytes of media) [11]
- [ ] **Entity 2:** (e.g., `VideoMetadata` - containing title, description, creator, status, and references) [11, 19]
- [ ] **Entity 3:** (e.g., `User` - representing creators and viewers) [10]

---

## Step 3: API Design (The Contract)
*Suggested Time: 5 Minutes*

Define the public API endpoints that clients will call to interface with your system [4]. Go **one-by-one through your functional requirements** to derive these API contracts [11].

### Upload API (Example) [11, 12, 18]
* **Endpoint:** `POST /v1/videos`
* **Request Body:**
  ```json
  {
    "title": "My Video",
    "description": "Video description",
    "video_size_bytes": 1073741824
  }
  ```
* **Response:**
  ```json
  {
    "video_id": "vid123",
    "upload_urls": ["https://s3.amazonaws.com/bucket/vid123-part1?...", "https://s3.amazonaws.com/bucket/vid123-part2?..."],
    "status": "pending"
  }
  ```

### Read/Stream API (Example) [13, 33]
* **Endpoint:** `GET /v1/videos/{video_id}`
* **Response:**
  ```json
  {
    "video_id": "vid123",
    "title": "My Video",
    "manifest_url": "https://cdn.youtube.com/vid123/manifest.m3u8",
    "creator_id": "user789"
  }
  ```

---

## Step 4: High-Level Design (HLD)
*Suggested Time: 10 Minutes*

Draw a simple, working architecture that satisfies the core functional requirements [4]. Connect clients, gateways, microservices, databases, and object stores [14]. **Do not worry about scale, latency, or optimizations yet** [4].

### Core Components checklist:
- [ ] **Client Applications:** Web, Mobile, Smart TV [14].
- [ ] **API Gateway:** Routing requests, rate limiting, authentication, SSL termination [14].
- [ ] **Microservices:** Stateless, decoupled services (e.g., `VideoService`, `UserService`) [14].
- [ ] **Relational or NoSQL Databases:** To store lightweight, queryable structured data (metadata) [15].
- [ ] **Object/Blob Storage:** (e.g., Amazon S3 or Google GCS) to cost-effectively store large binary assets like raw video files [15].

---

## Step 5: Deep Dives & Optimization
*Suggested Time: 15–20 Minutes*

Go **one-by-one through your non-functional requirements** to scale and optimize your high-level design [4]. Identify bottlenecks and introduce advanced system design patterns [4].

### A) Payload and Network Bottlenecks
* **Problem:** API Gateways have payload size limits (e.g., AWS API Gateway limit is 10 MB), but files can be up to 256 GB [16].
- [ ] **Solution:** Implement client-side chunking and bypass the API Gateway by uploading directly to S3 via pre-signed URLs using Multipart Upload (S3) or Resumable Uploads (GCS) [17, 18, 19].

### B) Asynchronous Orchestration
* **Problem:** Client-side updates are untrustworthy and prone to failures, leading to inconsistent application states [20].
- [ ] **Solution:** Rely on server-side triggers (e.g., S3 Event Notifications) to launch worker queues (Lambda, Celery) to update state databases and trigger video processing [20].

### C) Processing & Transcoding Pipelines
* **Problem:** Large files cannot be streamed efficiently as a single piece, and network speeds vary dynamically [22, 26].
- [ ] **Solution:** Introduce an asynchronous pipeline (Chunker + Transcoder) that [23, 29]:
  1. Splices the video into 2–10 second segments precisely at keyframes [23, 25].
  2. Transcodes segments in parallel into multiple codecs, resolutions (240p to 4K), and bitrates [27, 29, 30].
  3. Generates a playlist/manifest file (HLS or MPEG-DASH) [33, 35].

### D) Global Content Delivery (Low Latency)
* **Problem:** Fetching multi-gigabyte files from centralized object stores results in massive latency for international users [32].
- [ ] **Solution:** Place a Content Delivery Network (CDN) with edge servers geographically closer to users [32, 33]. Cache manifest files and popular video chunks on Edge POPs (Points of Presence) to lower startup times under 500ms [33].

### E) Database Scaling & Sharding
* **Problem:** As metadata rows grow into billions, a single database instance hits read/write and storage limits [38, 39].
- [ ] **Solution:** Shard the database horizontally (e.g., sharding a NoSQL database on `video_id` as the partition key and utilizing a Global Secondary Index on `user_id` to query a creator's upload library) [39].

---

## Step 6: Review & Wrap-up
*Suggested Time: 3–5 Minutes*

Sanity check your design by walking the interviewer through the complete lifecycle of both write and read paths, comparing it against your initial constraints [9, 41].

- [ ] **Write Path Verification:** Trace how a 256 GB file goes from client chunking -> pre-signed S3 upload -> S3 notification -> chunking/transcoding worker queue -> DB metadata update [36, 41].
- [ ] **Read Path Verification:** Trace how a user requests the video -> gets metadata and manifest -> pulls cached chunks directly from nearest CDN POP -> adaptively switches resolutions based on client network conditions [31, 36, 37].
- [ ] **Requirements Match:** Verify that availability, scalability, low-latency under bad connections, and storage estimations are fully resolved [37, 41].
