package com.club.rfid_access.infraestructure.persistence.entity;

import jakarta.persistence.*;
import java.time.Instant;
@Entity
@Table(name = "vehicles")
public class VehicleEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 20)
    private String plate;
    @Column(length = 50)
    private String brand;
    @Column(length = 50)
    private String model;
    @Column(length = 30)
    private String color;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private MemberEntity member;
    @Column(name = "image_key", length = 255)
    private String imageKey;
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    public VehicleEntity() {}
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getPlate() { return plate; }
    public void setPlate(String plate) { this.plate = plate; }
    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
    public MemberEntity getMember() { return member; }
    public void setMember(MemberEntity member) { this.member = member; }
    public String getImageKey() { return imageKey; }
    public void setImageKey(String imageKey) { this.imageKey = imageKey; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
