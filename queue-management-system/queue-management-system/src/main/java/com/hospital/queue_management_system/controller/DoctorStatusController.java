package com.hospital.queue_management_system.controller;

import java.util.List;

import org.springframework.web.bind.annotation.*;

import com.hospital.queue_management_system.model.DoctorStatus;
import com.hospital.queue_management_system.service.DoctorStatusService;

@RestController

@RequestMapping(
        "/doctor-status"
)

@CrossOrigin(
        origins =
                "http://localhost:5173"
)

public class DoctorStatusController {

    private final DoctorStatusService service;

    public DoctorStatusController(
            DoctorStatusService service
    ) {

        this.service = service;

    }

    @GetMapping
    public List<DoctorStatus> getAll() {

        return service.getAll();

    }

    @PostMapping
    public DoctorStatus save(

            @RequestBody
            DoctorStatus status

    ) {

        return service.save(
                status
        );

    }

    @DeleteMapping("/{id}")
    public void delete(

            @PathVariable
            Long id

    ) {

        service.delete(
                id
        );

    }

}