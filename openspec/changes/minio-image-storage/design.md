# Design: MinIO Image Storage

## Technical Approach

Extend existing `StorageService` with conditional MinIO init and thumbnail support. Add upload/serve endpoint pairs to `MemberController` and `VehicleController`. Generate thumbnails server-side with Java ImageIO (200px wide). Extend `AccessEventDTO` with image URL fields. Backend changes (PR1) and frontend changes (PR2) split for review budget compliance. Demo profile uses Floci emulator — same code path, different config.

## Architecture Decisions

| Decision | Choice | Alternatives | Rationale |
|----------|--------|-------------|-----------|
| Conditional MinIO | Catch init/upload errors, log warn, return null | `@ConditionalOnProperty` + no-op bean | Single code path, simpler config. Failures bubble to 503 for upload, 404 for serve — frontend handles gracefully |
| Thumbnail engine | Java ImageIO (javax.imageio) | Thumbnailator, imgscalr, Sharp (serverless) | Zero deps (JDK built-in). 200px width, preserve aspect ratio. Good enough for identity thumbnails |
| Upload response | 200 + entity body | 201, 302, no body | Frontend needs updated entity after upload (shows new state). Follows existing PUT/POST patterns |
| PR split | PR1: backend controllers + StorageService changes. PR2: frontend + DTO + SSE | All-in-one, 3-way split | Each PR ≤400 lines. PR1 is independently deployable (APIs exist, no UI regression). PR2 adds visual layer |
| Floci demo profile | Same MinioClient, different config values in `application-demo.yml` | Separate `@Profile("demo")` StorageService bean | MinioClient is endpoint-agnostic — just change URL/creds. No code duplication |

## Data Flow

```
Upload:
  Browser ──POST (multipart)──→ MemberController
      │                              │
      │                         validate JPEG/PNG, ≤10MB
      │                              │
      │                         StorageService.upload()
      │                              │
      │                    ┌── MinIO ── members/{id}/photo.jpg
      │                    │        
      │                    └── MinIO ── members/{id}/photo_thumb.jpg
      │                              │
      └── 200 + updated entity ←─────┘

Serve:
  Browser ──GET──→ MemberController
                       │
                  StorageService.getPresignedUrl()
                       │
                  302 Redirect ──→ MinIO pre-signed URL ──→ Browser loads directly

SSE:
  IngestionController ──→ AccessEventDTO(memberPhotoUrl, vehicleImageUrl)
                               │
                          SseBroadcaster ──→ React AccessFeed/AccessLogView
```

## File Changes

### PR1 — Backend Core

| File | Action | Description |
|------|--------|-------------|
| `StorageService.java` | Modify | Add `@ConditionalOnProperty` + `uploadThumbnail()`, wrap init in try-catch |
| `MemberController.java` | Modify | Add `POST /{id}/photo` + `GET /{id}/photo` |
| `VehicleController.java` | Modify | Inject `StorageService`, add `POST /{id}/image` + `GET /{id}/image` |
| `AccessEventDTO.java` | Modify | Add `String memberPhotoUrl, String vehicleImageUrl` fields |
| `IngestionController.java` | Modify | Inject `StorageService`, populate image URLs in SSE event |
| `application-demo.yml` | Modify | Add `rfid.storage.*` block for Floci endpoint/creds |

### PR2 — Frontend + DTO plumbing

| File | Action | Description |
|------|--------|-------------|
| `api.ts` | Modify | Add `uploadPhoto()`, `uploadImage()`, `getPhotoUrl()`, `getImageUrl()` methods |
| `api.ts` types (Member) | Modify | `photoUrl` already exists — used for pre-signed URL display URL |
| `api.ts` types (Vehicle) | Modify | Add `imageKey` field |
| `api.ts` types (AccessEvent) | Modify | Add `memberPhotoUrl`, `vehicleImageUrl` |
| `MemberManager.tsx` | Modify | Add file input + preview thumbnail in table |
| `VehicleManager.tsx` | Modify | Add file input + preview thumbnail in table |
| `AccessFeed.tsx` | Modify | Show 50px thumbnails next to member/vehicle info |
| `AccessLogView.tsx` | Modify | Show 50px thumbnails next to member name |

## Interfaces / Contracts

### Upload Endpoint — Member
```
POST /api/v1/members/{id}/photo
Content-Type: multipart/form-data
Body: file=<JPEG/PNG binary, ≤10MB>

Response 200: MemberEntity (with photoUrl = MinIO key)
Response 400: {"error": "Invalid image format. Only JPEG and PNG are allowed"}
Response 413: {"error": "File too large. Maximum size is 10MB"}
Response 404: {"error": "Member not found"}
Response 503: {"error": "Storage service unavailable"}
```

### Upload Endpoint — Vehicle
```
POST /api/v1/vehicles/{id}/image
Content-Type: multipart/form-data
Body: file=<JPEG/PNG binary, ≤10MB>

Response 200: VehicleEntity (with imageKey = MinIO key)
Response 400: {"error": "Invalid image format. Only JPEG and PNG are allowed"}
Response 413: {"error": "File too large. Maximum size is 10MB"}
Response 404: {"error": "Vehicle not found"}
Response 503: {"error": "Storage service unavailable"}
```

### Serve Endpoint — both Member and Vehicle
```
GET /api/v1/members/{id}/photo
Response 302: Location: <pre-signed MinIO URL, 1h expiry>
Response 404: (member not found or no photo)
Response 503: {"error": "Storage service unavailable"}
```

### SSE Event — extended
```json
{
  "eventId": "uuid",
  "timestamp": "...",
  "readerId": "GATE-01",
  "epc": "E20000123456789012345678",
  "decision": "GRANTED",
  "memberName": "Juan Pérez",
  "memberPhotoUrl": "http://minio:9000/rfid/members/42/photo_thumb.jpg?...",
  "vehiclePlate": "ABC-123",
  "vehicleColor": "Rojo",
  "vehicleBrand": "Toyota",
  "vehicleModel": "Corolla",
  "vehicleImageUrl": "http://minio:9000/rfid/vehicles/7/image_thumb.jpg?..."
}
```

## PR Split Detail

| PR | Contents | Est. Lines | Deployable |
|----|----------|-----------|------------|
| PR1 | StorageService changes, Member/Vehicle upload+serve endpoints, DTO extensions, IngestionController, demo config | ~350 | Yes — APIs work, frontend unaffected (no breaking changes) |
| PR2 | Frontend upload UI, thumbnail display in lists/feed/log, type updates | ~300 | Yes — pure UI layer |

## Testing Strategy

| Layer | What to Test | Approach |
|-------|-------------|----------|
| Unit | Thumbnail generation, format validation, file size check | JUnit 5, pure Java |
| Integration | Upload/serve endpoints, pre-signed URL redirect | `@SpringBootTest` + Testcontainers + MinIO testcontainer |
| Integration | SSE event with image URLs | Verify DTO fields populated |
| E2E | Full upload → display flow | Manual (no E2E framework yet) |

## Migration / Rollout

No data migration required. Existing `photoUrl` values (if any) remain unchanged — new uploads overwrite the field. Vehicle `imageKey` is null for existing records until first upload. MinIO bucket auto-created by `StorageService.initBucket()` on startup.

## Open Questions

None.
