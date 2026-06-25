# Member Management — Image Storage Delta

## Purpose

Extend member management with photo upload/serve, reusing `photoUrl` as MinIO key.

## Requirements

### Requirement: Upload Member Photo

The system MUST accept JPEG/PNG photos for existing members. Stores at `members/{id}/photo.{ext}` and thumbnail at `members/{id}/photo_thumb.{ext}`.

- GIVEN member M-001 exists
- WHEN a 2 MB JPEG is POSTed to `/api/v1/members/M-001/photo`
- THEN stored at `members/M-001/photo.jpg`
- AND thumbnail (max 200px wide) at `members/M-001/photo_thumb.jpg`
- AND upload is logged

- GIVEN member M-999 does not exist
- WHEN POST `/api/v1/members/M-999/photo` → HTTP 404

### Requirement: Serve Member Photo

The system MUST serve photos via pre-signed URL, 1-hour expiry.

- GIVEN member M-001 has a stored photo
- WHEN `GET /api/v1/members/M-001/photo` → 302 redirect to pre-signed URL

- GIVEN member M-001 has no photo
- WHEN `GET /api/v1/members/M-001/photo` → HTTP 404
