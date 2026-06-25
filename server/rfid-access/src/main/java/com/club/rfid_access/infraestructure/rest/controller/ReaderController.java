package com.club.rfid_access.infraestructure.rest.controller;

import com.club.rfid_access.infraestructure.persistence.entity.ReaderEntity;
import com.club.rfid_access.infraestructure.persistence.repository.ReaderRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/readers")
public class ReaderController {

    private final ReaderRepository readerRepository;

    public ReaderController(ReaderRepository readerRepository) {
        this.readerRepository = readerRepository;
    }

    @PostMapping
    public ResponseEntity<ReaderEntity> create(@RequestBody ReaderEntity reader) {
        reader.setCreatedAt(Instant.now());
        reader.setActive(true);
        return ResponseEntity.status(HttpStatus.CREATED).body(readerRepository.save(reader));
    }

    @GetMapping
    public Iterable<ReaderEntity> list() {
        return readerRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReaderEntity> getById(@PathVariable Long id) {
        return readerRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ReaderEntity> update(@PathVariable Long id, @RequestBody ReaderEntity updated) {
        return readerRepository.findById(id)
                .map(reader -> {
                    if (updated.getName() != null) reader.setName(updated.getName());
                    if (updated.getSerial() != null) reader.setSerial(updated.getSerial());
                    if (updated.getIpAddress() != null) reader.setIpAddress(updated.getIpAddress());
                    if (updated.getLocation() != null) reader.setLocation(updated.getLocation());
                    return ResponseEntity.ok(readerRepository.save(reader));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<?> setStatus(@PathVariable Long id, @RequestBody Map<String, Boolean> body) {
        return readerRepository.findById(id)
                .map(reader -> {
                    reader.setActive(body.getOrDefault("active", reader.isActive()));
                    return ResponseEntity.ok(readerRepository.save(reader));
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
