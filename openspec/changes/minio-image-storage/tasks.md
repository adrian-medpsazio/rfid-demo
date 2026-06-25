# Tasks: MinIO Image Storage

## Review Workload Forecast

| Field | Value |
|-------|-------|
| Estimated changed lines | ~650 (PR1: ~350, PR2: ~300) |
| 400-line budget risk | High |
| Chained PRs recommended | Yes |
| Delivery strategy | force-chained |
| Suggested split | PR1 backend → PR2 frontend (stacked-to-main) |
| Chain strategy | stacked-to-main |

Decision needed before apply: No
Chained PRs recommended: Yes
Chain strategy: stacked-to-main
400-line budget risk: High

### Suggested Work Units

| Unit | Goal | Likely PR | Notes |
|------|------|-----------|-------|
| 1 | Backend: StorageService conditional + thumbnail + upload/serve endpoints | PR 1 | Base = main. Independently deployable (no UI regression) |
| 2 | Backend: DTO + IngestionController + demo config | PR 1 | Combined with unit 1 in the same PR |
| 3 | Frontend: API client + upload UI + thumbnail display | PR 2 | Depends on PR1 endpoints. Adds visual layer |

## Phase 1: Backend — StorageService + Controllers (PR1)

- [x] 1.1 Modify `StorageService.java`: wrap initBucket() in try-catch, add uploadThumbnail() using ImageIO with 200px width, add thumbnail generation helper
- [x] 1.2 Add `POST /api/v1/members/{id}/photo` + `GET /api/v1/members/{id}/photo` to `MemberController.java` (validate content-type JPEG/PNG, size ≤10MB, upload to MinIO, 302 redirect with pre-signed URL)
- [x] 1.3 Inject StorageService into `VehicleController.java`, add `POST /api/v1/vehicles/{id}/image` + `GET /api/v1/vehicles/{id}/image` (same upload/serve pattern)
- [x] 1.4 Add `memberPhotoUrl` and `vehicleImageUrl` String fields to `AccessEventDTO.java`
- [x] 1.5 Inject StorageService into `IngestionController.java`, populate image URL fields in SSE AccessEventDTO
- [x] 1.6 Add `rfid.storage.*` config block to `application-demo.yml` for Floci endpoint/credentials (user handles this) — **hecho en `application.yml` directamente**

## Phase 2: Frontend — API + Upload UI + Display

- [x] 2.1 Add `uploadPhoto(id, file)` and `uploadImage(id, file)` methods to `api.ts`
- [x] 2.2 Add `imageKey` to Vehicle type, add `memberPhotoUrl`/`vehicleImageUrl` to AccessEvent type
- [x] 2.3 Add file upload input + preview thumbnail to `MemberManager.tsx` table rows
- [x] 2.4 Add file upload input + preview thumbnail to `VehicleManager.tsx` table rows
- [x] 2.5 Show member/vehicle thumbnails in `AccessFeed.tsx` next to event info
- [x] 2.6 Show member/vehicle thumbnails in `AccessLogView.tsx` next to entries

## Phase 3: Testing

- [x] 3.1 Unit: thumbnail generation (ImageIO), null/blank key handling — `StorageServiceTest.java`
- [x] 3.2 Integration: upload endpoint validation (format, oversize, not found) — `MemberControllerPhotoTest.java`, `VehicleControllerImageTest.java`
- [x] 3.3 Integration: serve endpoint 302 redirect / 404 / 503 — `MemberControllerPhotoTest.java`, `VehicleControllerImageTest.java`
- [x] 3.4 Integration: SSE event contains populated image URL fields — **verificado manualmente** (imágenes visibles en AccessFeed + AccessLogView, Floci responde en :4566)
