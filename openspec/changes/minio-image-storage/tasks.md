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

- [ ] 1.1 Modify `StorageService.java`: wrap initBucket() in try-catch, add uploadThumbnail() using ImageIO with 200px width, add thumbnail generation helper
- [ ] 1.2 Add `POST /api/v1/members/{id}/photo` + `GET /api/v1/members/{id}/photo` to `MemberController.java` (validate content-type JPEG/PNG, size ≤10MB, upload to MinIO, 302 redirect with pre-signed URL)
- [ ] 1.3 Inject StorageService into `VehicleController.java`, add `POST /api/v1/vehicles/{id}/image` + `GET /api/v1/vehicles/{id}/image` (same upload/serve pattern)
- [ ] 1.4 Add `memberPhotoUrl` and `vehicleImageUrl` String fields to `AccessEventDTO.java`
- [ ] 1.5 Inject StorageService into `IngestionController.java`, populate image URL fields in SSE AccessEventDTO
- [ ] 1.6 Add `rfid.storage.*` config block to `application-demo.yml` for Floci endpoint/credentials

## Phase 2: Frontend — API + Upload UI + Display (PR2)

- [ ] 2.1 Add `uploadPhoto(id, file)` and `uploadImage(id, file)` methods to `api.ts`
- [ ] 2.2 Add `imageKey` to Vehicle type, add `memberPhotoUrl`/`vehicleImageUrl` to AccessEvent type in `api.ts`
- [ ] 2.3 Add file upload input + preview thumbnail to `MemberManager.tsx` table rows
- [ ] 2.4 Add file upload input + preview thumbnail to `VehicleManager.tsx` table rows
- [ ] 2.5 Show 50px member/vehicle thumbnails in `AccessFeed.tsx` next to event info
- [ ] 2.6 Show 50px member/vehicle thumbnails in `AccessLogView.tsx` next to entries

## Phase 3: Testing

- [ ] 3.1 Unit: thumbnail generation (ImageIO), format validation (JPEG/PNG only), file size check (≤10MB)
- [ ] 3.2 Integration: upload endpoint happy path + error cases (invalid format, oversize, entity not found) with Testcontainers + MinIO
- [ ] 3.3 Integration: serve endpoint returns 302 redirect to pre-signed URL, 404 when no photo/image
- [ ] 3.4 Integration: SSE event contains populated image URL fields when images exist, null when absent
