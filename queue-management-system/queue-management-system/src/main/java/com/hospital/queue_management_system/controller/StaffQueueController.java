package com.hospital.queue_management_system.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hospital.queue_management_system.dto.EmergencyTransferDTO;
import com.hospital.queue_management_system.dto.QueueTokenDTO;
import com.hospital.queue_management_system.service.StaffQueueService;

@RestController
@RequestMapping("/staff-queue")
@CrossOrigin(origins = "http://localhost:5173")
public class StaffQueueController {

    private final StaffQueueService service;

    public StaffQueueController(
            StaffQueueService service
    ) {
        this.service = service;
    }

    // =========================================================
    // GET TODAY'S QUEUE
    // =========================================================

    @GetMapping
    public ResponseEntity<List<QueueTokenDTO>> getQueue(
            @RequestParam(required = false) Long doctorId
    ) {

        return ResponseEntity.ok(
                service.getQueue(doctorId)
        );
    }

    // =========================================================
    // CALL NEXT PATIENT
    // =========================================================

    @PutMapping("/call-next")
    public ResponseEntity<String> callNext(
            @RequestParam Long doctorId
    ) {

        service.callNextPatient(doctorId);

        return ResponseEntity.ok(
                "Next patient called successfully."
        );
    }

    // =========================================================
    // SKIP PATIENT
    // =========================================================

    @PutMapping("/skip/{id}")
    public ResponseEntity<String> skip(
            @PathVariable Long id
    ) {

        service.skipPatient(id);

        return ResponseEntity.ok(
                "Patient skipped successfully."
        );
    }

    // =========================================================
    // COMPLETE PATIENT
    // =========================================================

    @PutMapping("/complete/{id}")
    public ResponseEntity<String> complete(
            @PathVariable Long id
    ) {

        service.completePatient(id);

        return ResponseEntity.ok(
                "Patient completed successfully."
        );
    }

    // =========================================================
    // UPDATE QUEUE STATUS
    // =========================================================

    @PutMapping("/status/{id}")
    public ResponseEntity<String> updateStatus(
            @PathVariable Long id,
            @RequestParam String status
    ) {

        service.updateStatus(
                id,
                status
        );

        return ResponseEntity.ok(
                "Queue status updated successfully."
        );
    }

    // =========================================================
    // EMERGENCY TRANSFER
    // =========================================================

    @PutMapping("/transfer-emergency")
    public ResponseEntity<QueueTokenDTO> emergencyTransfer(
            @RequestBody EmergencyTransferDTO dto
    ) {

        QueueTokenDTO result =
                service.transferEmergency(dto);

        return ResponseEntity.ok(result);
    }
}