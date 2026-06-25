package com.club.rfid_access.infraestructure.persistence.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "access_log")
public class AccessLogEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tag_epc", nullable = false, length = 64)
    private String tagEpc;

    @Column(name = "reader_id", nullable = false, length = 50)
    private String readerId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private MemberEntity member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id")
    private VehicleEntity vehicle;

    @Column(nullable = false)
    private boolean authorized;

    @Column(length = 255)
    private String reason;

    @Column(name = "read_count")
    private int readCount;

    @Column(name = "tag_timestamp", nullable = false)
    private Instant tagTimestamp;

    @Column(name = "server_timestamp")
    private Instant serverTimestamp;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public AccessLogEntity() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTagEpc() { return tagEpc; }
    public void setTagEpc(String tagEpc) { this.tagEpc = tagEpc; }

    public String getReaderId() { return readerId; }
    public void setReaderId(String readerId) { this.readerId = readerId; }

    public MemberEntity getMember() { return member; }
    public void setMember(MemberEntity member) { this.member = member; }

    public VehicleEntity getVehicle() { return vehicle; }
    public void setVehicle(VehicleEntity vehicle) { this.vehicle = vehicle; }

    public boolean isAuthorized() { return authorized; }
    public void setAuthorized(boolean authorized) { this.authorized = authorized; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public int getReadCount() { return readCount; }
    public void setReadCount(int readCount) { this.readCount = readCount; }

    public Instant getTagTimestamp() { return tagTimestamp; }
    public void setTagTimestamp(Instant tagTimestamp) { this.tagTimestamp = tagTimestamp; }

    public Instant getServerTimestamp() { return serverTimestamp; }
    public void setServerTimestamp(Instant serverTimestamp) { this.serverTimestamp = serverTimestamp; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
