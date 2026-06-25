package com.club.rfid_access.infraestructure.persistence.repository;

import com.club.rfid_access.infraestructure.persistence.entity.RfidTagEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RfidTagRepository extends JpaRepository<RfidTagEntity, Long> {
    Optional<RfidTagEntity> findByEpc(String epc);
    java.util.Optional<RfidTagEntity> findByMember_IdAndActiveTrue(Long memberId);
    java.util.Optional<RfidTagEntity> findByVehicle_IdAndActiveTrue(Long vehicleId);
}
