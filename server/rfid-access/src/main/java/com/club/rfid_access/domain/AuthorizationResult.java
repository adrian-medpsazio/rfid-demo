package com.club.rfid_access.domain;

import java.time.Instant;

public record AuthorizationResult(
        AuthorizationDecision decision,
        String reason,
        Instant checkedAt
) {
}
