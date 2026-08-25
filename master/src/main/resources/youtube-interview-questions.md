# YouTube System Design Interview Questions & Answers

Based on the system design breakdown by Evan (former Meta Staff Engineer), this document compiles key system design interview questions, detailed answers, and architectural insights for designing a large-scale video sharing platform like YouTube.

---

### Q1: What are the primary scale estimates and requirements to establish when designing YouTube?
**Answer:**
*   **Functional Requirements:**
    *   Users must be able to upload videos.
    *   Users must be able to watch/stream videos.
*   **Non-Functional Requirements:**
    *   **High Availability over Consistency:** Video uploads do not need to propagate globally immediately. Eventual consistency is acceptable; it is more important that users can always access the platform to view existing content.
    *   **Support for Large File Uploads:** The system must handle video files up to **256 GB** (or 12 hours in length).
    *   **Low Latency Streaming:** Real-time playback startup latency should be under **500 milliseconds** (getting pixels on screen ASAP), even in low bandwidth environments.
    *   **Scalability:** The system must scale to handle **1 million uploads per day** and **100 million daily active users (DAUs)**.

---

### Q2: Why is a standard POST request inappropriate for uploading videos to YouTube, and how do you resolve this bottleneck?
**Answer:**
*   **The Problem:** Standard HTTP POST requests send the entire file payload through the API Gateway and Web/Video Services. Most API Gateways and web servers have strict maximum payload limits. For instance, an AWS API Gateway has a hard limit of **10 megabytes**. A 256 GB video far exceeds this. Passing such large files through intermediate application servers also causes immense network costs, memory consumption, and CPU overhead.
*   **The Solution:** Use **Direct-to-Object-Storage Upload with Multipart Upload**.
    1.  The client sends a metadata-only POST request to the Video Service (including the file size).
    2.  The Video Service requests **pre-signed URLs** from the object store (e.g., AWS S3 or Google Cloud Storage) and returns them to the client.
    3.  The client chunks the video file and uploads the segments directly to the storage bucket in parallel using the object store's multi-part/resumable upload API. S3/GCS then stitches the chunks back together once all parts are received. This bypasses the API Gateway and application servers entirely.

---

### Q3: When uploading directly to S3 via pre-signed URLs, how should the system update the video metadata status to "Upload Complete"? Why shouldn't the client trigger this update?
**Answer:**
*   **Why not trust the client:** The client application could easily fail, disconnect, or maliciously lie about the upload status. Relying on the client to send a "completed" callback to the Video Service can result in inconsistent database states (e.g., status marked as complete when the file is corrupted or incomplete in S3).
*   **The Solution (S3 Notifications + Worker):** Use an asynchronous event-driven pattern. 
    1.  Once S3 finishes stitching the multi-part upload, it triggers an **S3 Notification** event.
    2.  This notification is sent to an asynchronous worker (e.g., a serverless Lambda function or message queue).
    3.  The worker securely extracts the verified S3 URL from the event payload and updates the video's status to `uploaded` in the Metadata Database.

---

### Q4: Why does the system chunk the video twice—once on the client side during upload, and once on the server side after upload?
**Answer:**
The two chunking processes are optimized for entirely different objectives:
1.  **Client-Side Upload Chunking:** Optimized to minimize HTTP request overhead and handle network interruptions. These chunks are relatively large (**5 to 10 MB**), have arbitrary byte offsets, and do not consider visual boundaries or playback performance.
2.  **Server-Side Streaming Chunking:** Optimized for the playback experience. A server-side chunker splits the completed video file into very small segments (**2 to 10 seconds** of video). These chunks are precisely cut at video **key frames** to ensure clean playback and enable immediate startup latency (the client only needs to download a tiny 2-second chunk to start playing instead of waiting for a 10 GB file).

---

### Q5: Explain the components of a video container file and the role of transcoding in video streaming systems.
**Answer:**
*   **Video Container (e.g., .mp4, .mov):** A wrapper that packages several components together:
    *   **Video Codec:** A compression standard (like H.264, HEVC, AV1) that compresses massive raw video data by identifying patterns and redundancies (only storing full "key frames" occasionally and only saving the differences/motion data between frames).
    *   **Audio Codec:** Compression standard for audio streams.
    *   **Metadata:** Playback parameters like resolution, bit rate, frame rate, aspect ratio, and duration.
*   **Role of Transcoding:** If a user uploads a 4K video, a client on a bad 3G connection cannot play it smoothly. Transcoding is the asynchronous process of converting the uploaded raw video into **multiple resolutions (from 240p up to 4K)**, codecs, and bit rates. This allows the system to serve the optimal resolution based on the user's real-time network environment.

---

### Q6: What is Adaptive Bitrate Streaming, and how does the client utilize a "Manifest File" to achieve it?
**Answer:**
*   **Adaptive Bitrate Streaming:** The client dynamically adjusts the quality of the video stream in real-time as network conditions change. For example, if a user starts watching on high-speed home Wi-Fi, they stream in 4K. If they walk out of range onto cellular data, the player automatically downgrades to 720p or 480p without stalling playback.
*   **The Manifest File:** A manifest file (such as a JSON, XML, or YAML file) maps each available resolution to its ordered list of chunk URLs. 
*   **How it works:** 
    1.  The client first downloads the manifest file.
    2.  The player checks the current network throughput.
    3.  The player fetches the first few chunks at the highest supported quality (e.g., 4K).
    4.  The player continually measures network performance. If bandwidth drops, the client adaptively requests the subsequent chunks from the lower resolution lists (e.g., 720p) specified in the manifest file.

---

### Q7: How do you design the system for global low-latency video streaming? Explain the role of CDNs and how they cache files.
**Answer:**
*   To minimize latency, video data must be located close to the end user. If a user in Germany requests a video stored in a US-West object storage bucket, the round-trip time is too high.
*   **Using a CDN (Content Delivery Network):**
    *   The system uses CDNs acting as geographical caches located at Edge Points of Presence (PoPs) close to users.
    *   **Popular Videos:** Popular video chunks and their corresponding **manifest files** are cached directly on edge servers.
    *   **Streaming Flow:** When a user clicks play, they query the metadata DB for basic text details, but the video player immediately requests the manifest file and video chunks from the nearest CDN. If it is a cache hit, the video starts instantly (well under 500ms). If it is a cache miss, the CDN fetches the chunks from S3, caches them locally for future viewers, and streams them to the user.

---

### Q8: What database type and sharding strategy would you select for the Video Metadata Database at YouTube's scale?
**Answer:**
*   **Data Volume Estimation:** With 1 million uploads per day, if each metadata entry is ~1 KB, we accumulate roughly **1 GB of metadata per day**, which is only **~365 GB per year** (or 36.5 TB over 10 years). The scale of the core video metadata (ID, title, description, user_id, S3 URL, upload_status) is actually small enough to fit on a single relational database instance (like PostgreSQL) or a NoSQL store (like DynamoDB).
*   **Database Choice:** **NoSQL (e.g., DynamoDB or Cassandra)** is preferred for its high write throughput, predictable low latency, and horizontal scalability.
*   **Sharding Strategy:**
    *   **Partition/Shard Key:** Shard by `video_id` to distribute write and read traffic evenly across the cluster.
    *   **Sort Key:** Use the creation timestamp (`created_at`) as the sort key.
    *   **Global Secondary Index (GSI):** Create a GSI on `user_id` to quickly query all videos uploaded by a specific user (which is a common query pattern).
