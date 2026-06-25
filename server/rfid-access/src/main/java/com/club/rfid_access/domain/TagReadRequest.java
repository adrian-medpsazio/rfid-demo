package com.club.rfid_access.domain;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;

public record TagReadRequest(
    @NotBlank String epc,
    @NotBlank String readerId,
    Integer antenna,
    Integer rssi,
    @NotNull Instant timestamp
) {}
