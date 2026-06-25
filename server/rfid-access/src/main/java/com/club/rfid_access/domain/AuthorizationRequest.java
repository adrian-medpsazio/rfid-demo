package com.club.rfid_access.domain;

import java.time.Instant;

public record AuthorizationRequest(
        String epc,
        String readerId,
        Instant timestamp
) {
}
