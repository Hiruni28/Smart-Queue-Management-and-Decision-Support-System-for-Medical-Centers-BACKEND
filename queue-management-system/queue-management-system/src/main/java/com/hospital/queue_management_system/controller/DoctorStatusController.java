package com.hospital.queue_management_system.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.hospital.queue_management_system.model.DoctorStatus;
import com.hospital.queue_management_system.service.DoctorStatusService;

@RestController
@RequestMapping("/doctor-status")
@CrossOrigin(origins = "http://localhost:5173")
public class DoctorStatusController {

    private final DoctorStatusService service;

    public DoctorStatusController(
            DoctorStatusService service
    ) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<DoctorStatus>> getAll() {

        return ResponseEntity.ok(
                service.getAll()
        );
    }

    @GetMapping("/{doctorId}")
    public ResponseEntity<DoctorStatus> getCurrentStatus(
            @PathVariable Long doctorId
    ) {

        DoctorStatus status =
                service.getCurrentStatus(doctorId);

        if (status == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(status);
    }

    @PostMapping
    public ResponseEntity<DoctorStatus> save(
            @RequestBody DoctorStatus status
    ) {

        return ResponseEntity.ok(
                service.save(status)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id
    ) {

        service.delete(id);

        return ResponseEntity.noContent().build();
    }
}