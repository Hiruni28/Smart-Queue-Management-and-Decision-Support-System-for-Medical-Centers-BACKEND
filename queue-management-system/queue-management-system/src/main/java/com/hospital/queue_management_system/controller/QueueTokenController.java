package com.hospital.queue_management_system.controller;

import com.hospital.queue_management_system.dto.EmergencyTransferDTO;
import com.hospital.queue_management_system.dto.QueueTokenDTO;
import com.hospital.queue_management_system.model.QueueToken;
import com.hospital.queue_management_system.service.QueueTokenService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/queue-token")
@CrossOrigin(origins = "http://localhost:5173")
public class QueueTokenController {

    private final QueueTokenService service;

    public QueueTokenController(
            QueueTokenService service
    ) {
        this.service = service;
    }

    // =========================================================
    // GET QUEUE
    // =========================================================

    @GetMapping
    public ResponseEntity<List<QueueToken>> getQueue(
            @RequestParam(required = false) Long doctorId
    ) {

        if (doctorId != null) {

            return ResponseEntity.ok(
                    service.doctorQueue(doctorId)
            );
        }

        return ResponseEntity.ok(
                service.getAll()
        );
    }

    // =========================================================
    // GENERATE TOKEN
    // =========================================================

    @PostMapping
    public ResponseEntity<QueueToken> generateToken(
            @RequestBody QueueTokenDTO dto
    ) {

        return ResponseEntity.ok(
                service.generateToken(dto)
        );
    }

    // =========================================================
    // CALL NEXT PATIENT
    // =========================================================

    @PutMapping("/call-next")
    public ResponseEntity<QueueToken> callNext(
            @RequestParam Long doctorId
    ) {

        return ResponseEntity.ok(
                service.callNext(doctorId)
        );
    }

    // =========================================================
    // COMPLETE
    // =========================================================

    @PutMapping("/complete/{queueId}")
    public ResponseEntity<QueueToken> complete(
            @PathVariable Long queueId
    ) {

        return ResponseEntity.ok(
                service.complete(queueId)
        );
    }

    // =========================================================
    // SKIP
    // =========================================================

    @PutMapping("/skip/{queueId}")
    public ResponseEntity<QueueToken> skip(
            @PathVariable Long queueId
    ) {

        return ResponseEntity.ok(
                service.skip(queueId)
        );
    }

    // =========================================================
    // UPDATE STATUS
    // =========================================================

    @PutMapping("/status/{queueId}")
    public ResponseEntity<QueueToken> updateStatus(
            @PathVariable Long queueId,
            @RequestParam String status
    ) {

        return ResponseEntity.ok(
                service.updateStatus(
                        queueId,
                        status
                )
        );
    }

    // =========================================================
    // UPDATE PRIORITY
    // =========================================================

    @PutMapping("/priority/{queueId}")
    public ResponseEntity<QueueToken> updatePriority(
            @PathVariable Long queueId,
            @RequestParam String priority
    ) {

        return ResponseEntity.ok(
                service.updatePriority(
                        queueId,
                        priority
                )
        );
    }

    // =========================================================
    // EMERGENCY TRANSFER
    // =========================================================

    @PutMapping("/transfer-emergency")
    public ResponseEntity<QueueToken> transferEmergency(
            @RequestBody EmergencyTransferDTO dto
    ) {

        return ResponseEntity.ok(
                service.transferEmergency(dto)
        );
    }
}