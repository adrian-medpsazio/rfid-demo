package com.club.rfid_access.domain;

import java.time.Instant;

public record TagDataEvent(
    Data data,
    Instant timestamp,
    String type
) {
    public record Data(
        int eventNum,
        String format,
        String idHex,
        Integer antenna,
        Integer rssi
    ) {}
}
