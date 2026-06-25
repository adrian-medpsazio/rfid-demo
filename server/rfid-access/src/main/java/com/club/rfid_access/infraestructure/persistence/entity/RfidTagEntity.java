package com.club.rfid_access.infraestructure.persistence.entity;

import jakarta.persistence.*;
import java.time.Instant;
@Entity
@Table(name = "rfid_tags")
public class RfidTagEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true, length = 64)
    private String epc;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private MemberEntity member;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id")
    private VehicleEntity vehicle;
    @Column(name = "assigned_at")
    private Instant assignedAt;
    @Column(name = "revoked_at")
    private Instant revokedAt;
    @Column(nullable = false)
    private boolean active;
    public RfidTagEntity() {}
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getEpc() { return epc; }
    public void setEpc(String epc) { this.epc = epc; }
    public MemberEntity getMember() { return member; }
    public void setMember(MemberEntity member) { this.member = member; }
    public VehicleEntity getVehicle() { return vehicle; }
    public void setVehicle(VehicleEntity vehicle) { this.vehicle = vehicle; }
    public Instant getAssignedAt() { return assignedAt; }
    public void setAssignedAt(Instant assignedAt) { this.assignedAt = assignedAt; }
    public Instant getRevokedAt() { return revokedAt; }
    public void setRevokedAt(Instant revokedAt) { this.revokedAt = revokedAt; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
