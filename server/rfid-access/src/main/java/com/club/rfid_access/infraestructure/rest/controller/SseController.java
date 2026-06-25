package com.club.rfid_access.infraestructure.rest.controller;

import com.club.rfid_access.domain.service.SseBroadcaster;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/v1/events")
public class SseController {

    private final SseBroadcaster sseBroadcaster;

    public SseController(SseBroadcaster sseBroadcaster) {
        this.sseBroadcaster = sseBroadcaster;
    }

    @GetMapping
    public SseEmitter subscribe() {
        return sseBroadcaster.createEmitter();
    }
}
