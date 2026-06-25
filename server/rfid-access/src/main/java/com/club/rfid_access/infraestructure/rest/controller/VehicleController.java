package com.club.rfid_access.infraestructure.rest.controller;

import com.club.rfid_access.domain.service.StorageService;
import com.club.rfid_access.infraestructure.persistence.entity.VehicleEntity;
import com.club.rfid_access.infraestructure.persistence.repository.MemberRepository;
import com.club.rfid_access.infraestructure.persistence.repository.VehicleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/vehicles")
public class VehicleController {

    private static final Logger log = LoggerFactory.getLogger(VehicleController.class);
    private static final long MAX_FILE_SIZE = 10L * 1024 * 1024; // 10 MB

    private final VehicleRepository vehicleRepository;
    private final MemberRepository memberRepository;
    private final StorageService storageService;

    public VehicleController(VehicleRepository vehicleRepository, MemberRepository memberRepository,
                             StorageService storageService) {
        this.vehicleRepository = vehicleRepository;
        this.memberRepository = memberRepository;
        this.storageService = storageService;
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody VehicleEntity vehicle) {
        vehicle.setCreatedAt(Instant.now());
        VehicleEntity saved = vehicleRepository.save(vehicle);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @GetMapping
    public List<VehicleEntity> listAll() {
        return vehicleRepository.findAll();
    }

    @PutMapping("/{id}/link/{memberId}")
    public ResponseEntity<?> linkToMember(@PathVariable Long id, @PathVariable Long memberId) {
        var vehicleOpt = vehicleRepository.findById(id);
        if (vehicleOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        var memberOpt = memberRepository.findById(memberId);
        if (memberOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        var vehicle = vehicleOpt.get();
        vehicle.setMember(memberOpt.get());
        vehicleRepository.save(vehicle);

        return ResponseEntity.ok(vehicle);
    }

    @GetMapping("/by-member/{memberId}")
    public List<VehicleEntity> getByMember(@PathVariable Long memberId) {
        return vehicleRepository.findByMemberId(memberId);
    }

    @PostMapping("/{id}/image")
    public ResponseEntity<?> uploadImage(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
        var vehicleOpt = vehicleRepository.findById(id);
        if (vehicleOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        String contentType = file.getContentType();
        if (contentType == null || !(contentType.equals("image/jpeg") || contentType.equals("image/png"))) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid image format. Only JPEG and PNG are allowed"));
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            return ResponseEntity.status(413).body(Map.of("error", "File too large. Maximum size is 10MB"));
        }

        String ext = "image/png".equals(contentType) ? "png" : "jpg";
        String key = "vehicles/" + id + "/image." + ext;

        try {
            String uploadedKey = storageService.uploadWithThumbnail(key, file);
            if (uploadedKey == null) {
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                        .body(Map.of("error", "Storage service unavailable"));
            }
            var vehicle = vehicleOpt.get();
            vehicle.setImageKey(uploadedKey);
            vehicleRepository.save(vehicle);

            log.info("Uploaded image for vehicle {}: {}", id, uploadedKey);
            return ResponseEntity.ok(vehicle);
        } catch (Exception e) {
            log.error("Failed to upload image for vehicle {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to upload image"));
        }
    }

    @GetMapping("/{id}/image")
    public ResponseEntity<?> getImage(@PathVariable Long id) {
        var vehicleOpt = vehicleRepository.findById(id);
        if (vehicleOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        var vehicle = vehicleOpt.get();
        if (vehicle.getImageKey() == null || vehicle.getImageKey().isBlank()) {
            return ResponseEntity.notFound().build();
        }

        String presignedUrl = storageService.getPresignedUrl(vehicle.getImageKey());
        if (presignedUrl == null) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("error", "Storage service unavailable"));
        }

        return ResponseEntity.status(HttpStatus.FOUND)
                .header(HttpHeaders.LOCATION, presignedUrl)
                .build();
    }
}
