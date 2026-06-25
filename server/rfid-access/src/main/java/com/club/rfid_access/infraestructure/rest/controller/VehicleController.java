package com.club.rfid_access.infraestructure.rest.controller;

import com.club.rfid_access.infraestructure.persistence.entity.VehicleEntity;
import com.club.rfid_access.infraestructure.persistence.repository.MemberRepository;
import com.club.rfid_access.infraestructure.persistence.repository.VehicleRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/vehicles")
public class VehicleController {

    private final VehicleRepository vehicleRepository;
    private final MemberRepository memberRepository;

    public VehicleController(VehicleRepository vehicleRepository, MemberRepository memberRepository) {
        this.vehicleRepository = vehicleRepository;
        this.memberRepository = memberRepository;
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
}
