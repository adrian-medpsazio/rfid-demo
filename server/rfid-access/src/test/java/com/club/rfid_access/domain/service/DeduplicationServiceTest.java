package com.club.rfid_access.domain.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DeduplicationServiceTest {

    private final DeduplicationService service = new DeduplicationService(500);

    @Test
    void firstRead_shouldNotBeDuplicate() {
        assertFalse(service.isDuplicate("GATE-01", "EPC-001"));
    }

    @Test
    void sameReadWithinWindow_shouldBeDuplicate() {
        service.isDuplicate("GATE-01", "EPC-001");
        assertTrue(service.isDuplicate("GATE-01", "EPC-001"));
    }

    @Test
    void sameEpcDifferentReader_shouldNotBeDuplicate() {
        service.isDuplicate("GATE-01", "EPC-001");
        assertFalse(service.isDuplicate("GATE-02", "EPC-001"));
    }

    @Test
    void differentEpcSameReader_shouldNotBeDuplicate() {
        service.isDuplicate("GATE-01", "EPC-001");
        assertFalse(service.isDuplicate("GATE-01", "EPC-002"));
    }
}
