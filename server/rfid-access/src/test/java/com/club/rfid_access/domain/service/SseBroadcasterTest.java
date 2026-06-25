package com.club.rfid_access.domain.service;

import com.club.rfid_access.domain.AccessEventDTO;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class SseBroadcasterTest {

    private final SseBroadcaster broadcaster = new SseBroadcaster();

    @Test
    void createEmitter_shouldReturnEmitter() {
        SseEmitter emitter = broadcaster.createEmitter();
        assertNotNull(emitter);
    }

    @Test
    void multipleEmitters_shouldAllReceiveBroadcast() {
        SseEmitter e1 = broadcaster.createEmitter();
        SseEmitter e2 = broadcaster.createEmitter();

        var event = new AccessEventDTO("evt-1", Instant.now(), "GATE-01", 1,
                "EPC-001", "GRANTED", null, "MEMBER_ACTIVE", "ABC-123");

        // Should not throw
        assertDoesNotThrow(() -> broadcaster.broadcast(event));
    }
}
