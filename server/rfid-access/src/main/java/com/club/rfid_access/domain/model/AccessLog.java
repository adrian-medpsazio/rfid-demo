package com.club.rfid_access.domain.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccessLog {
    private Long id;
    private String epc;
    private String readerId;
    private int antenna;
    private int rssi;
    private Instant timestamp;
    private String decision;
    private String reason;
    private String memberName;
    private String vehiclePlate;
}
