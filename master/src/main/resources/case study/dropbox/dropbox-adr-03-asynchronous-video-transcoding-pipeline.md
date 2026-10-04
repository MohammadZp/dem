# ADR 03: Asynchronous Video Transcoding Pipeline & Adaptive Bitrate Streaming (HLS)

## Status
**Accepted**

## Context & Problem Statement
The platform is expanding past 1,000,000 Daily Active Users (1M+ DAU) and transitioning from a monolithic backend to decoupled microservices. Users upload video files in diverse formats (.mov, .avi, .mp4, .mkv) with resolutions up to 4K.

Key engineering requirements:
1. **Non-Blocking Ingestion:** Video upload must not block waiting for video encoding/transcoding to complete.
2. **Adaptive Bitrate Streaming:** Users on mobile 4G networks or high-speed fiber must receive continuous, buffer-free playback matched to their available network bandwidth.
3. **Horizontal Compute Scaling:** Video transcoding is extremely CPU/GPU intensive and must scale independently without degrading core file storage microservices.

## Considered Options
1. **Synchronous Transcoding in API Handler:** Transcode video during the HTTP upload request.
2. **Monolithic Background Worker Thread Pool:** Run FFmpeg jobs on the monolithic backend application server.
3. **Event-Driven Asynchronous Pipeline via Apache Kafka, FFmpeg Worker Clusters, and HLS/DASH CDN Distribution (Chosen Solution):** Decouple ingestion from processing using Kafka message queues and distribute video via HLS playlists.

## Decision Outcome
**Chosen Option:** **Event-Driven Asynchronous Pipeline via Apache Kafka, FFmpeg Worker Clusters, and HLS/DASH CDN Distribution.**

### Architectural Mechanism:
1. **Event-Driven Decoupling:**
   * When a video file upload is finalized, the File Service publishes a `video_uploaded` event to an Apache Kafka topic (`video-processing-jobs`).
2. **Asynchronous Transcoding Worker Pool:**
   * Statescale auto-scaling Go/FFmpeg worker instances consume jobs from Kafka.
   * Workers download raw video blocks from S3, split video into 4-second chunk segments (`.ts` files), and encode video into multiple resolutions (1080p, 720p, 480p, 360p) using H264/AAC codecs.
3. **HLS Master Playlist Generation:**
   * Workers generate HTTP Live Streaming (HLS) master playlists (`master.m3u8`) referencing variant resolution playlists.
   * Transcoded segments and playlists are written back to Amazon S3.
4. **Edge CDN Distribution:**
   * Video streaming requests (`GET /videos/stream/{id}`) serve HLS playlists and `.ts` video chunks via CloudFront CDN edge locations, reducing origin load and eliminating playback buffering.

## Consequences

### Positive Impacts
* **Decoupled Architecture:** Heavy transcoding compute is isolated from core file storage, fulfilling requirement #2 of transitioning away from a monolithic backend.
* **Adaptive User Experience:** HLS automatically switches stream quality (e.g., 1080p to 480p) dynamically as client network conditions fluctuate.
* **Elastic Auto-Scaling:** Kafka consumer groups scale transcoding worker nodes up or down dynamically based on queue lag depth.

### Negative Impacts / Trade-offs
* **Processing Delay (Eventual Availability):** Uploaded videos are not instantly viewable in high resolution; users see a "Processing Video..." state for 10–60 seconds post-upload.
* **Storage Multiplier:** Storing original raw videos alongside 4 transcoded bitrate variants increases raw S3 storage footprint per video by ~1.8x.