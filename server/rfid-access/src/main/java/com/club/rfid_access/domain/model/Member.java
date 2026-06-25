package com.club.rfid_access.domain.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Member {
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String photoUrl;
    private String memberCode;
    private boolean active;
    private Instant createdAt;
    private Instant updatedAt;
}
