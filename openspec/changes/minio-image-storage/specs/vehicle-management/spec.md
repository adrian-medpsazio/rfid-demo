# Vehicle Management — Image Storage Delta

## Purpose

Extend vehicle management with image upload/serve, populating `imageKey` field.

## Requirements

### Requirement: Upload Vehicle Image

The system MUST accept JPEG/PNG images for existing vehicles. Stores at `vehicles/{id}/image.{ext}`, thumbnail at `vehicles/{id}/image_thumb.{ext}`, and updates `imageKey`.

- GIVEN vehicle V-001 exists
- WHEN a 1 MB PNG is POSTed to `/api/v1/vehicles/V-001/image`
- THEN stored at `vehicles/V-001/image.png`
- AND `imageKey` set to `vehicles/V-001/image.png`
- AND thumbnail at `vehicles/V-001/image_thumb.png`

- GIVEN vehicle V-999 does not exist
- WHEN POST `/api/v1/vehicles/V-999/image` → HTTP 404

### Requirement: Serve Vehicle Image

The system MUST serve images via pre-signed URL, 1-hour expiry.

- GIVEN vehicle V-001 has `imageKey` populated
- WHEN `GET /api/v1/vehicles/V-001/image` → 302 redirect to pre-signed URL

- GIVEN vehicle V-001 has null `imageKey`
- WHEN `GET /api/v1/vehicles/V-001/image` → HTTP 404
