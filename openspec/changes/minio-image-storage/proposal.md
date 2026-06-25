# Proposal: MinIO Image Storage

## Intent
Add image upload/serve capabilities for member photos and vehicle images using MinIO (S3-compatible) object storage. Enable visual identification in access control dashboards while maintaining system resilience when storage is unavailable.

## Scope

### In Scope
- Member photo upload (`POST /api/v1/members/{id}/photo`) and serve (`GET /api/v1/members/{id}/photo`) with 1-hour pre-signed URLs
- Vehicle image upload (`POST /api/v1/vehicles/{id}/image`) and serve (`GET /api/v1/vehicles/{id}/image`)
- Conditional MinIO: StorageService logs warning and continues when MinIO/Floci unavailable
- Demo profile using Floci S3 emulator (same API, different config)
- Extend AccessEventDTO with `memberPhotoUrl` and `vehicleImageUrl` for SSE feed
- Frontend upload UI in MemberManager and VehicleManager
- Thumbnail display in AccessFeed and AccessLogView
- Chained stacked-to-main PRs (2 PRs, ~400 lines each)

### Out of Scope
- Image resizing/optimization (future enhancement)
- Bulk image import
- Image deletion on member/vehicle soft-delete
- Mobile app integration

## Capabilities

### New Capabilities
- `image-storage`: MinIO integration, conditional availability, Floci demo profile, pre-signed URL generation

### Modified Capabilities
- `member-management`: Add photo upload/serve endpoints, reuse existing `photoUrl` field as MinIO object key
- `vehicle-management`: Add image upload/serve endpoints, populate `imageKey` field
- `realtime-sse`: Extend AccessEventDTO with image URLs, populate in IngestionController

## Approach
Extend existing `StorageService` with conditional MinIO client initialization. Add endpoint pairs for photo/image upload and serve. Update `AccessEventDTO` to include pre-signed URLs for SSE feed. Frontend adds upload components and image display. Split into two stacked PRs: backend core (PR1) and frontend + DTO extensions (PR2).

## Affected Areas

| Area | Impact | Description |
|------|--------|-------------|
| `server/.../MemberController.java` | Modified | Add photo upload/serve endpoints |
| `server/.../VehicleController.java` | Modified | Add image upload/serve endpoints |
| `server/.../StorageService.java` | Modified | Conditional MinIO, better error handling |
| `server/.../MinioConfig.java` | Modified | Profile-specific configuration |
| `server/.../AccessEventDTO.java` | Modified | Add image URL fields |
| `server/.../IngestionController.java` | Modified | Populate image URLs in SSE events |
| `ui/src/components/members/MemberManager.tsx` | Modified | Add photo upload UI |
| `ui/src/components/vehicles/VehicleManager.tsx` | Modified | Add image upload UI |
| `ui/src/components/gate/AccessFeed.tsx` | Modified | Display member/vehicle thumbnails |
| `ui/src/components/gate/AccessLogView.tsx` | Modified | Display thumbnails in log view |

## Risks

| Risk | Likelihood | Mitigation |
|------|------------|------------|
| MinIO unavailability breaks core functionality | Medium | Conditional initialization, graceful degradation |
| Pre-signed URL security exposure | Low | 1-hour expiry, HTTPS only |
| Frontend bundle size increase from images | Low | Lazy loading, thumbnail optimization |
| Floci emulator differences from real MinIO | Low | Use standard S3 API only |

## Rollback Plan
1. Revert backend changes (controllers, DTO, StorageService modifications)
2. Revert frontend changes (upload UI, thumbnail display)
3. No database migrations required (existing fields reused)
4. Remove MinIO dependency if needed (optional cleanup)

## Dependencies
- MinIO server or Floci emulator running
- Existing `StorageService` and `MinioConfig` (already implemented)
- Frontend build pipeline (Vite + React)

## Success Criteria
- [ ] Member photos upload successfully to MinIO
- [ ] Vehicle images upload successfully to MinIO
- [ ] Pre-signed URLs generate correctly with 1-hour expiry
- [ ] SSE feed includes image URLs for real-time display
- [ ] Frontend displays upload forms and thumbnails
- [ ] System continues operating when MinIO is unavailable
- [ ] Floci demo profile works correctly
- [ ] Two stacked PRs created within 400-line budget each

## Proposal Question Round

**Assumptions needing user review:**
1. Reuse existing `photoUrl` field for member photos (already exists in MemberEntity)
2. Vehicle `imageKey` field already added via migration
3. No image deletion on soft-delete (per business rule)
4. Pre-signed URL expiry fixed at 1 hour, no refresh mechanism
5. Frontend will use standard HTML file input with preview
6. Split into 2 PRs: backend (PR1) + frontend/DTO (PR2)

**Proposed questions for clarification:**
1. Should we add image metadata (size, type, upload date) to member/vehicle entities?
2. Do we need image format validation (JPEG/PNG only) on backend?
3. Should thumbnails be generated server-side or client-side?
4. What's the maximum allowed image file size?
5. Should we add image upload audit logging?

Please confirm assumptions or provide answers to shape final proposal.