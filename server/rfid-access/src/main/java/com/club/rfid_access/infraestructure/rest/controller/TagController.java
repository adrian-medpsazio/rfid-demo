package com.club.rfid_access.infraestructure.rest.controller;

import com.club.rfid_access.infraestructure.persistence.entity.RfidTagEntity;
import com.club.rfid_access.infraestructure.persistence.repository.MemberRepository;
import com.club.rfid_access.infraestructure.persistence.repository.RfidTagRepository;
import com.club.rfid_access.infraestructure.persistence.repository.VehicleRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/tags")
public class TagController {

    private final RfidTagRepository tagRepository;
    private final MemberRepository memberRepository;
    private final VehicleRepository vehicleRepository;

    public TagController(RfidTagRepository tagRepository,
                         MemberRepository memberRepository,
                         VehicleRepository vehicleRepository) {
        this.tagRepository = tagRepository;
        this.memberRepository = memberRepository;
        this.vehicleRepository = vehicleRepository;
    }

    @PostMapping("/assign")
    public ResponseEntity<?> assign(@RequestBody Map<String, String> body) {
        String epc = body.get("epc");
        String entityType = body.get("entityType");
        String entityId = body.get("entityId");

        var existing = tagRepository.findByEpc(epc);
        if (existing.isPresent() && existing.get().isActive()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "Tag already assigned"));
        }

        RfidTagEntity tag;
        if (existing.isPresent()) {
            // reactivate previously unassigned tag
            tag = existing.get();
            tag.setActive(true);
            tag.setAssignedAt(Instant.now());
            tag.setRevokedAt(null);
            tag.setMember(null);
            tag.setVehicle(null);
        } else {
            tag = new RfidTagEntity();
            tag.setEpc(epc);
            tag.setActive(true);
            tag.setAssignedAt(Instant.now());
        }

        if ("MEMBER".equals(entityType)) {
            var member = memberRepository.findById(Long.parseLong(entityId))
                    .orElseThrow(() -> new IllegalArgumentException("Member not found: " + entityId));
            tag.setMember(member);
        } else if ("VEHICLE".equals(entityType)) {
            var vehicle = vehicleRepository.findById(Long.parseLong(entityId))
                    .orElseThrow(() -> new IllegalArgumentException("Vehicle not found: " + entityId));
            tag.setVehicle(vehicle);
        }

        tagRepository.save(tag);
        return ResponseEntity.status(HttpStatus.CREATED).body(tag);
    }

    @PostMapping("/unassign")
    public ResponseEntity<?> unassign(@RequestBody Map<String, String> body) {
        String epc = body.get("epc");
        var tagOpt = tagRepository.findByEpc(epc);

        if (tagOpt.isEmpty()) {
            return ResponseEntity.ok(Map.of("status", "not_found"));
        }

        var tag = tagOpt.get();
        tag.setActive(false);
        tag.setRevokedAt(Instant.now());
        tagRepository.save(tag);

        return ResponseEntity.ok(Map.of("status", "unassigned"));
    }

    @GetMapping("/{epc}")
    public ResponseEntity<?> getByEpc(@PathVariable String epc) {
        return tagRepository.findByEpc(epc)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/by-member/{memberId}")
    public ResponseEntity<?> getByMember(@PathVariable Long memberId) {
        return tagRepository.findByMember_IdAndActiveTrue(memberId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/by-vehicle/{vehicleId}")
    public ResponseEntity<?> getByVehicle(@PathVariable Long vehicleId) {
        return tagRepository.findByVehicle_IdAndActiveTrue(vehicleId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
