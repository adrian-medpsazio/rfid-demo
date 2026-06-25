# Image Storage Specification

## Purpose

Define MinIO integration for storing, serving, and validating member/vehicle images with graceful degradation.

## Requirements

### Requirement: Conditional Availability

The system MUST degrade gracefully when MinIO is unavailable — log a warning and return 503.

- GIVEN MinIO is unreachable
- WHEN any upload or pre-signed URL request arrives
- THEN the system logs a warning with entity ID and size
- AND returns HTTP 503

### Requirement: Upload Validation

The system MUST accept only `image/jpeg` and `image/png`. Size MUST NOT exceed 10 MB.

- GIVEN a 5 MB JPEG with content-type `image/jpeg`
- WHEN uploaded
- THEN stored in MinIO
- AND a thumbnail (max 200px wide) generated

- GIVEN content-type `image/gif`
- WHEN uploaded → HTTP 400

- GIVEN file of 15 MB
- WHEN uploaded → HTTP 413

### Requirement: Pre-signed URLs

The system MUST serve images via pre-signed URLs with 1-hour expiry.

- GIVEN object `members/M-001/photo.jpg` exists
- WHEN the photo GET endpoint is called
- THEN 302 redirect to a pre-signed URL valid for 1 hour

### Requirement: Audit Logging

The system SHOULD log upload events at INFO level with entity ID, file size, and timestamp.

- GIVEN a successful upload for vehicle V-001
- THEN an INFO log records the event
