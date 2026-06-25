package com.club.rfid_access.infraestructure.rest.controller;

import com.club.rfid_access.domain.service.StorageService;
import com.club.rfid_access.infraestructure.persistence.entity.MemberEntity;
import com.club.rfid_access.infraestructure.persistence.repository.MemberRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/members")
public class MemberController {

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
}
