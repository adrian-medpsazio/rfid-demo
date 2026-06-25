package com.club.rfid_access.infraestructure.persistence.repository;

import com.club.rfid_access.infraestructure.persistence.entity.VehicleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VehicleRepository extends JpaRepository<VehicleEntity, Long> {
    List<VehicleEntity> findByMemberId(Long memberId);
}
