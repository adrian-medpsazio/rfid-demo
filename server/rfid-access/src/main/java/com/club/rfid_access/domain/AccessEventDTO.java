package com.club.rfid_access.domain;

import java.time.Instant;

public record AccessEventDTO(
    String eventId,
    Instant timestamp,
    String readerId,
    int antenna,
    String antennaName,
    String epc,
    String decision,
    String reason,
    String memberName,
    String memberPhotoUrl,
    String vehiclePlate,
    String vehicleColor,
    String vehicleBrand,
    String vehicleModel,
    String vehicleImageUrl
) {}
