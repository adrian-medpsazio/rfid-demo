package com.club.rfid_access.infraestructure.rest.controller;

import com.club.rfid_access.domain.service.StorageService;
import com.club.rfid_access.infraestructure.persistence.entity.MemberEntity;
import com.club.rfid_access.infraestructure.persistence.repository.MemberRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/members")
public class MemberController {

    private static final Logger log = LoggerFactory.getLogger(MemberController.class);
    private static final long MAX_FILE_SIZE = 10L * 1024 * 1024; // 10 MB

    private final MemberRepository memberRepository;
    private final StorageService storageService;

    public MemberController(MemberRepository memberRepository, StorageService storageService) {
        this.memberRepository = memberRepository;
        this.storageService = storageService;
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody MemberEntity member) {
        if (member.getEmail() != null && memberRepository.findByEmail(member.getEmail()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "Email already registered"));
        }

        member.setActive(true);
        member.setCreatedAt(Instant.now());
        member.setUpdatedAt(Instant.now());

        MemberEntity saved = memberRepository.save(member);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody MemberEntity updated) {
        var memberOpt = memberRepository.findById(id);
        if (memberOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        var member = memberOpt.get();

        if (updated.getEmail() != null && !updated.getEmail().equals(member.getEmail())
                && memberRepository.findByEmail(updated.getEmail()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "Email already taken"));
        }

        if (updated.getFirstName() != null) member.setFirstName(updated.getFirstName());
        if (updated.getLastName() != null) member.setLastName(updated.getLastName());
        if (updated.getEmail() != null) member.setEmail(updated.getEmail());
        if (updated.getPhone() != null) member.setPhone(updated.getPhone());
        if (updated.getPhotoUrl() != null) member.setPhotoUrl(updated.getPhotoUrl());
        member.setUpdatedAt(Instant.now());

        return ResponseEntity.ok(memberRepository.save(member));
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<?> deactivate(@PathVariable Long id) {
        var memberOpt = memberRepository.findById(id);
        if (memberOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        var member = memberOpt.get();
        member.setActive(false);
        member.setUpdatedAt(Instant.now());
        memberRepository.save(member);

        return ResponseEntity.ok(Map.of("status", "deactivated"));
    }

    @PatchMapping("/{id}/activate")
    public ResponseEntity<?> activate(@PathVariable Long id) {
        var memberOpt = memberRepository.findById(id);
        if (memberOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        var member = memberOpt.get();
        member.setActive(true);
        member.setUpdatedAt(Instant.now());
        memberRepository.save(member);

        return ResponseEntity.ok(Map.of("status", "activated"));
    }

    @GetMapping
    public Page<MemberEntity> list(@RequestParam(required = false) String status, Pageable pageable) {
        if ("ACTIVE".equalsIgnoreCase(status)) {
            return memberRepository.findAll(pageable); // filter would need a custom query
        }
        return memberRepository.findAll(pageable);
    }

    @PostMapping("/{id}/photo")
    public ResponseEntity<?> uploadPhoto(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
        var memberOpt = memberRepository.findById(id);
        if (memberOpt.isEmpty()) {
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
        String key = "members/" + id + "/photo." + ext;

        try {
            String uploadedKey = storageService.uploadWithThumbnail(key, file);
            if (uploadedKey == null) {
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                        .body(Map.of("error", "Storage service unavailable"));
            }
            var member = memberOpt.get();
            member.setPhotoUrl(uploadedKey);
            member.setUpdatedAt(Instant.now());
            memberRepository.save(member);

            log.info("Uploaded photo for member {}: {}", id, uploadedKey);
            return ResponseEntity.ok(member);
        } catch (Exception e) {
            log.error("Failed to upload photo for member {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to upload photo"));
        }
    }

    @GetMapping("/{id}/photo")
    public ResponseEntity<?> getPhoto(@PathVariable Long id) {
        var memberOpt = memberRepository.findById(id);
        if (memberOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        var member = memberOpt.get();
        if (member.getPhotoUrl() == null || member.getPhotoUrl().isBlank()) {
            return ResponseEntity.notFound().build();
        }

        String presignedUrl = storageService.getPresignedUrl(member.getPhotoUrl());
        if (presignedUrl == null) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("error", "Storage service unavailable"));
        }

        return ResponseEntity.status(HttpStatus.FOUND)
                .header(HttpHeaders.LOCATION, presignedUrl)
                .build();
    }
}
