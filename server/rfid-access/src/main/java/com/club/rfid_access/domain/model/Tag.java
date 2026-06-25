package com.club.rfid_access.domain.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Tag {
    private Long id;
    private String epc;
    private TagStatus status;
    private AssignedEntityType assignedEntityType;
    private String assignedEntityId;
    private Instant assignmentDate;
    private Instant createdAt;
    private Instant updatedAt;
}
