package com.club.rfid_access.infraestructure.rest.controller;

import com.club.rfid_access.infraestructure.persistence.entity.AccessLogEntity;
import com.club.rfid_access.infraestructure.persistence.repository.AccessLogRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("/api/v1/access-logs")
public class AccessLogController {

    private final AccessLogRepository accessLogRepository;

    public AccessLogController(AccessLogRepository accessLogRepository) {
        this.accessLogRepository = accessLogRepository;
    }

    @GetMapping
    public Page<AccessLogEntity> list(
            @RequestParam(required = false) Instant from,
            @RequestParam(required = false) Instant to,
            @RequestParam(required = false) Long readerId,
            @RequestParam(required = false) String epc,
            @RequestParam(required = false) Long memberId,
            Pageable pageable) {
        // default sort: most recent first
        if (!pageable.getSort().isSorted()) {
            pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),
                    Sort.by(Sort.Direction.DESC, "tagTimestamp"));
        }
        return accessLogRepository.findAll(pageable);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public void deleteNotAllowed() {
        // Append-only log — deletes not permitted
    }
}
