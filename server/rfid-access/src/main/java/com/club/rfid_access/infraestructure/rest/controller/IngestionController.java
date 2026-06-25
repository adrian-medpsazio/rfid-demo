package com.club.rfid_access.infraestructure.rest.controller;

import com.club.rfid_access.domain.AccessEventDTO;
import com.club.rfid_access.domain.AuthorizationRequest;
import com.club.rfid_access.domain.AuthorizationResult;
import com.club.rfid_access.domain.TagDataEvent;
import com.club.rfid_access.domain.service.DeduplicationService;
import com.club.rfid_access.domain.service.DefaultAuthorizationCheck;
import com.club.rfid_access.domain.service.SseBroadcaster;
import com.club.rfid_access.infraestructure.persistence.entity.AccessLogEntity;
import com.club.rfid_access.infraestructure.persistence.entity.ReaderEntity;
import com.club.rfid_access.infraestructure.persistence.repository.AccessLogRepository;
import com.club.rfid_access.infraestructure.persistence.repository.ReaderRepository;
import com.club.rfid_access.infraestructure.persistence.repository.RfidTagRepository;
import com.club.rfid_access.infraestructure.persistence.repository.VehicleRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/ingestion")
public class IngestionController {

    private static final Logger log = LoggerFactory.getLogger(IngestionController.class);

    private final DeduplicationService dedupService;
    private final DefaultAuthorizationCheck authCheck;
    private final SseBroadcaster sseBroadcaster;
    private final AccessLogRepository accessLogRepository;
    private final RfidTagRepository tagRepository;
    private final ReaderRepository readerRepository;
    private final VehicleRepository vehicleRepository;

    public IngestionController(DeduplicationService dedupService,
                               DefaultAuthorizationCheck authCheck,
                               SseBroadcaster sseBroadcaster,
                               AccessLogRepository accessLogRepository,
                               RfidTagRepository tagRepository,
                               ReaderRepository readerRepository,
                               VehicleRepository vehicleRepository) {
        this.dedupService = dedupService;
        this.authCheck = authCheck;
        this.sseBroadcaster = sseBroadcaster;
        this.accessLogRepository = accessLogRepository;
        this.tagRepository = tagRepository;
        this.readerRepository = readerRepository;
        this.vehicleRepository = vehicleRepository;
    }

    @PostMapping("/tag-read")
    public ResponseEntity<Void> ingestTagRead(HttpServletRequest request, @RequestBody List<TagDataEvent> events) {
        var readerOpt = resolveReader(request.getRemoteAddr());
        String readerId = readerOpt.map(ReaderEntity::getName).orElse(request.getRemoteAddr());
        String readerLocation = readerOpt.map(ReaderEntity::getLocation).orElse(null);
        for (var event : events) {
            processTagEvent(event, readerId, readerLocation);
        }
        return ResponseEntity.ok().build();
    }

    private Optional<ReaderEntity> resolveReader(String remoteAddr) {
        Optional<ReaderEntity> reader = readerRepository.findByIpAddress(remoteAddr);
        if (reader.isEmpty()) {
            log.warn("Reader not found for IP: {}, using remote IP as reader ID", remoteAddr);
        }
        return reader;
    }

    private void processTagEvent(TagDataEvent tagEvent, String readerId, String readerLocation) {
        String epc = tagEvent.data().idHex().toUpperCase();
        log.info("Tag read: reader={}, epc={}", readerId, epc);

        if (dedupService.isDuplicate(readerId, epc)) {
            log.debug("Duplicate read ignored: reader={}, epc={}", readerId, epc);
            return;
        }

        AuthorizationResult result = authCheck.check(
                new AuthorizationRequest(epc, readerId, tagEvent.timestamp())
        );

        // look up tag for member/vehicle details
        String memberName = null;
        String vehiclePlate = null;
        String vehicleColor = null;
        String vehicleBrand = null;
        String vehicleModel = null;
        var tagOpt = tagRepository.findByEpc(epc);
        if (tagOpt.isPresent() && tagOpt.get().isActive()) {
            var tag = tagOpt.get();
            if (tag.getMember() != null) {
                memberName = tag.getMember().getFirstName() + " " + tag.getMember().getLastName();
            }
            if (tag.getVehicle() != null) {
                vehiclePlate = tag.getVehicle().getPlate();
                vehicleColor = tag.getVehicle().getColor();
                vehicleBrand = tag.getVehicle().getBrand();
                vehicleModel = tag.getVehicle().getModel();
            } else if (tag.getMember() != null) {
                // no direct vehicle on tag — look up member's first vehicle
                var vehicles = vehicleRepository.findByMemberId(tag.getMember().getId());
                if (!vehicles.isEmpty()) {
                    var v = vehicles.get(0);
                    vehiclePlate = v.getPlate();
                    vehicleColor = v.getColor();
                    vehicleBrand = v.getBrand();
                    vehicleModel = v.getModel();
                }
            }
        }

        var logEntry = new AccessLogEntity();
        logEntry.setTagEpc(epc);
        logEntry.setReaderId(readerId);
        if (tagOpt.isPresent() && tagOpt.get().isActive()) {
            logEntry.setMember(tagOpt.get().getMember());
            logEntry.setVehicle(tagOpt.get().getVehicle());
        }
        logEntry.setAuthorized(result.decision() == com.club.rfid_access.domain.AuthorizationDecision.GRANTED);
        logEntry.setReason(result.reason());
        logEntry.setReadCount(tagEvent.data().eventNum() + 1);
        logEntry.setTagTimestamp(tagEvent.timestamp());
        logEntry.setServerTimestamp(Instant.now());
        logEntry.setCreatedAt(Instant.now());
        accessLogRepository.save(logEntry);

        int antennaPort = tagEvent.data().antenna() != null ? tagEvent.data().antenna() : 0;
        String antennaName = readerLocation != null ? readerLocation : ("Antena " + antennaPort);

        var sseEvent = new AccessEventDTO(
                UUID.randomUUID().toString(),
                Instant.now(),
                readerId,
                antennaPort,
                antennaName,
                epc,
                result.decision().name(),
                result.reason(),
                memberName,
                vehiclePlate,
                vehicleColor,
                vehicleBrand,
                vehicleModel
        );

        sseBroadcaster.broadcast(sseEvent);
    }
}
