# Apply Progress: MinIO Image Storage

## Status
**completed** — All 16 tasks finished. Implementation verified manually and via unit/integration tests.

## Completed Tasks

### Phase 1: Backend — StorageService + Controllers
- [x] 1.1 StorageService: conditional init, uploadThumbnail() via ImageIO (200px), thumbnail helper
- [x] 1.2 MemberController: POST/GET photo endpoints (JPEG/PNG only, ≤10MB, pre-signed URL 302 redirect)
- [x] 1.3 VehicleController: POST/GET image endpoints (same pattern)
- [x] 1.4 AccessEventDTO: added memberPhotoUrl + vehicleImageUrl fields
- [x] 1.5 IngestionController: populates image URLs in SSE events (null-safe)
- [x] 1.6 Storage config in `application.yml` (Floci endpoint http://localhost:4566, creds test/test)

### Phase 2: Frontend — API + Upload UI + Display
- [x] 2.1 API client: uploadPhoto(), uploadImage(), getPhotoUrl(), getImageUrl()
- [x] 2.2 Types: imageKey on Vehicle, memberPhotoUrl/vehicleImageUrl on AccessEvent
- [x] 2.3 MemberManager: photo upload + 40x40 thumbnail in table rows
- [x] 2.4 VehicleManager: image upload + 40x40 thumbnail in table rows
- [x] 2.5 AccessFeed: 20x20 thumbnails in SSE events
- [x] 2.6 AccessLogView: 24x24 thumbnails in log entries

### Phase 3: Testing
- [x] 3.1 StorageServiceTest: null/blank key, thumbnail resize, no upscale, invalid image
- [x] 3.2 MemberControllerPhotoTest + VehicleControllerImageTest: upload validation
- [x] 3.3 Serve endpoint: 302 redirect / 404 / 503 tests
- [x] 3.4 SSE integration verified manually — images display correctly in AccessFeed + AccessLogView

## Key Decisions During Apply
- **Config location**: Storage config went into `application.yml` (default profile) instead of `application-demo.yml` — user chose to run with default profile + Floci directly
- **Vite proxy**: Changed from `10.10.31.118:8080` to `localhost:8080` to match local backend
- **PR split deferred**: All changes committed to main in a single session; PR split remains as documented strategy for when remote is configured
- **Floci bucket created manually**: User created `rfid` bucket on Floci (localhost:4566)

## Artifacts Produced
- `server/.../domain/service/StorageService.java` — modified
- `server/.../rest/controller/MemberController.java` — modified
- `server/.../rest/controller/VehicleController.java` — modified
- `server/.../domain/AccessEventDTO.java` — modified
- `server/.../rest/controller/IngestionController.java` — modified
- `server/.../resources/application.yml` — modified
- `ui/src/services/api.ts` — modified
- `ui/src/types/api.ts` — modified
- `ui/src/components/members/MemberManager.tsx` — modified
- `ui/src/components/vehicles/VehicleManager.tsx` — modified
- `ui/src/components/gate/AccessFeed.tsx` — modified
- `ui/src/components/gate/AccessLogView.tsx` — modified
- `ui/vite.config.ts` — modified
- `server/.../domain/service/StorageServiceTest.java` — new
- `server/.../rest/controller/MemberControllerPhotoTest.java` — new
- `server/.../rest/controller/VehicleControllerImageTest.java` — new
