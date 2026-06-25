# Verification Report: MinIO Image Storage

**Change**: minio-image-storage  
**Date**: 2026-06-25  
**Mode**: Standard (Strict TDD disabled)  

## Summary

- **Tasks**: 16/16 complete
- **Specs**: 3/3 verified (Member Management, Vehicle Management, Realtime SSE)
- **Tests**: 3 test classes exist, user confirmed passing
- **Runtime evidence**: User uploaded images and confirmed thumbnails visible in UI
- **Verdict**: **PASS WITH WARNINGS**

---

## 1. Completeness

| Artifact | Status | Evidence |
|----------|--------|----------|
| Proposal | ✅ Done | `proposal.md` |
| Specs | ✅ Done | `specs/member-management/spec.md`, `specs/vehicle-management/spec.md`, `specs/realtime-sse/spec.md` |
| Design | ✅ Done | `design.md` |
| Tasks | ✅ Done (16/16) | `tasks.md` |
| Apply Progress | ✅ Done | `apply-progress.md` |

---

## 2. Spec Compliance Matrix

### Member Management (spec.md)

| Requirement | Scenario | Evidence | Status |
|-------------|----------|----------|--------|
| Upload member photo | JPEG/PNG accepted, stored at `members/{id}/photo.{ext}`, thumbnail at `members/{id}/photo_thumb.{ext}`, logged | `MemberController.java` lines 119-148 validate format, size, call `uploadWithThumbnail()`, log upload. `StorageService.java` generates 200px JPEG thumb | ✅ COMPLIANT |
| Upload — member not found | POST to `/api/v1/members/{id}/photo` where id doesn't exist → 404 | `MemberController.java` lines 114-117 return 404 | ✅ COMPLIANT |
| Upload — invalid format | Non-JPEG/PNG → 400 | `MemberController.java` lines 119-122 return 400 | ✅ COMPLIANT |
| Upload — oversize | >10MB → 413 | `MemberController.java` lines 124-126 return 413 | ✅ COMPLIANT |
| Serve member photo | GET `/api/v1/members/{id}/photo` → 302 redirect to pre-signed URL | `MemberController.java` lines 168-171 return 302 with Location header | ✅ COMPLIANT |
| Serve — no photo | GET where member has null/blank photoUrl → 404 | `MemberController.java` lines 159-161 return 404 | ✅ COMPLIANT |

### Vehicle Management (spec.md)

| Requirement | Scenario | Evidence | Status |
|-------------|----------|----------|--------|
| Upload vehicle image | JPEG/PNG accepted, stored at `vehicles/{id}/image.{ext}`, thumbnail at `vehicles/{id}/image_thumb.{ext}`, imageKey updated | `VehicleController.java` lines 80-108 validate, upload, set imageKey. `StorageService.uploadWithThumbnail()` | ✅ COMPLIANT |
| Upload — vehicle not found | POST where id doesn't exist → 404 | `VehicleController.java` lines 75-78 return 404 | ✅ COMPLIANT |
| Upload — invalid format | → 400 | `VehicleController.java` lines 80-83 return 400 | ✅ COMPLIANT |
| Upload — oversize | → 413 | `VehicleController.java` lines 85-87 return 413 | ✅ COMPLIANT |
| Serve vehicle image | GET `/api/v1/vehicles/{id}/image` → 302 redirect to pre-signed URL | `VehicleController.java` lines 129-131 return 302 | ✅ COMPLIANT |
| Serve — no image | GET where imageKey null/blank → 404 | `VehicleController.java` lines 119-121 return 404 | ✅ COMPLIANT |

### Realtime SSE (spec.md)

| Requirement | Scenario | Evidence | Status |
|-------------|----------|----------|--------|
| Extended DTO | `AccessEventDTO` includes `memberPhotoUrl` and `vehicleImageUrl` fields | `AccessEventDTO.java` records both String fields | ✅ COMPLIANT |
| Populate image URLs | When member has photo and vehicle has image, SSE event contains pre-signed URLs | `IngestionController.java` lines 146-150 populate via `storageService.getPresignedUrl()` | ✅ COMPLIANT |
| Null when no image | When member has no photo, `memberPhotoUrl` is null | `IngestionController.java` lines 144-148 — `memberPhotoUrl` stays null if member/photoUrl null | ✅ COMPLIANT |

---

## 3. Design Coherence

| Design Decision | Implementation | Status |
|-----------------|---------------|--------|
| Conditional MinIO: catch errors, log warn, return null | `StorageService.initBucket()` wrapped in try-catch, `getPresignedUrl()` returns null on failure | ✅ MATCH |
| Thumbnail: ImageIO, 200px, aspect ratio preserved | `StorageService.generateThumbnail()` uses `ImageIO`, `SCALE_SMOOTH`, preserves aspect ratio | ✅ MATCH |
| Upload response: 200 + entity body | Both controllers return `ResponseEntity.ok(member/vehicle)` | ✅ MATCH |
| Pre-signed URL 302 redirect | Both controllers return `302 FOUND` with `Location` header | ✅ MATCH |
| 503 on storage unavailable | Both controllers return `503 SERVICE_UNAVAILABLE` when `uploadWithThumbnail` or `getPresignedUrl` returns null | ✅ MATCH |
| Floci config | Design said `application-demo.yml`; user chose `application.yml` directly | ⚠️ WARNING — minor config location difference, user decision |
| Conditional MinIO approach | Design considered `@ConditionalOnProperty` but chose catch+return-null; code matches chosen approach | ✅ MATCH |

---

## 4. Test Coverage

| Test Class | Tests | Evidence | Status |
|-----------|-------|----------|--------|
| `StorageServiceTest.java` | Null/blank key → null, thumbnail 400→200px, no upscale for small images, invalid image → exception | File exists, user confirmed tests pass | ✅ PASS |
| `MemberControllerPhotoTest.java` | Invalid format → 400, oversize → 413, not found → 404, no photo → 404, storage unavailable → 503, valid → 302 | File exists, user confirmed tests pass | ✅ PASS |
| `VehicleControllerImageTest.java` | Invalid format → 400, not found → 404, valid upload → 200, no image → 404, valid → 302 | File exists, user confirmed tests pass | ✅ PASS |

**Note**: Tests could not be re-executed in this environment (no JDK). Last known execution was successful per user confirmation.

---

## 5. Runtime Evidence

| Check | Result | Status |
|-------|--------|--------|
| Floci running on :4566 | HTTP 200 on root | ✅ OK |
| Bucket `rfid` exists | Created (was lost on Floci restart, re-created) | ✅ OK |
| Stored objects | Lost on Floci restart (in-memory storage) | ⚠️ WARNING — images need re-upload after Floci restart |
| User upload test | User confirmed images visible in UI, thumbnails working | ✅ OK |
| SSE images | User confirmed thumbnails in AccessFeed and AccessLogView | ✅ OK |

---

## 6. Issues

### CRITICAL
*(none)*

### WARNING
1. **Floci in-memory storage**: Floci stores data in-memory. On restart, all uploaded images are lost and bucket must be re-created. Mitigation: `StorageService.initBucket()` auto-creates bucket on startup (logged as info), but images themselves are not recovered. Consider persisting Floci data or switching to Dockerized MinIO with volume mount for demo stability.
2. **Config location deviation**: Design specified `application-demo.yml` for Floci config; user applied config directly to `application.yml`. Functional, but deviates from documented design. No impact on functionality.

### SUGGESTION
1. **Floci persistence**: If using Floci long-term, configure `FLOCI_DATA_DIR` env var for disk persistence, or mount a volume.
2. **E2E test**: Consider adding a Playwright/Cypress test for the full upload→display flow.

---

## 7. Final Verdict

```
PASS WITH WARNINGS
```

- **All 16 tasks**: ✅ Complete
- **All spec requirements**: ✅ Compliant (14/14 scenarios mapped and verified)
- **Design coherence**: ✅ 6/7 decisions match, 1 minor deviation (config location)
- **Test coverage**: ✅ 3 test classes with unit + integration coverage
- **Runtime evidence**: ✅ User-verified upload and display working
- **Warnings**: Non-critical (Floci in-memory volatility, minor config location)
