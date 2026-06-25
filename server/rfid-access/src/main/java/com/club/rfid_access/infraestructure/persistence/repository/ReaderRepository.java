package com.club.rfid_access.infraestructure.persistence.repository;

import com.club.rfid_access.infraestructure.persistence.entity.ReaderEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReaderRepository extends JpaRepository<ReaderEntity, Long> {
    java.util.Optional<ReaderEntity> findByIpAddress(String ipAddress);
}
