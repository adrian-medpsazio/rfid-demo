# Realtime SSE — Image Storage Delta

## Purpose

Extend `AccessEventDTO` with image URLs for thumbnail display in SSE feed.

## Requirements

### Requirement: Extended AccessEventDTO

`AccessEventDTO` MUST include `memberPhotoUrl` and `vehicleImageUrl`. `IngestionController` SHALL populate with pre-signed URLs (1h expiry) when images exist.

- GIVEN member M-001 has a photo and vehicle V-001 has an image
- WHEN an SSE access-log event is dispatched
- THEN `memberPhotoUrl` and `vehicleImageUrl` contain valid pre-signed URLs

- GIVEN member M-002 has no photo
- WHEN an SSE access-log event is dispatched
- THEN `memberPhotoUrl` is null
