package com.club.rfid_access.infraestructure.persistence.repository;

import com.club.rfid_access.infraestructure.persistence.entity.AccessLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccessLogRepository extends JpaRepository<AccessLogEntity, Long> {
}
