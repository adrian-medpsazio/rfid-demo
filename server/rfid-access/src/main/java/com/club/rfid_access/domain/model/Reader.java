package com.club.rfid_access.domain.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class Reader {
  private Long id;
  private String name;
  private String serial;
  private String ipAddress;
  private String location;
  private boolean active;
  private Instant createdAt;
}
