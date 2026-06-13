package com.hospital.queue_management_system.controller;

import java.util.List;

import org.springframework.web.bind.annotation.*;

import com.hospital.queue_management_system.model.Appointment;
import com.hospital.queue_management_system.service.AppointmentService;

@RestController

@RequestMapping("/appointment")

@CrossOrigin(
        origins="http://localhost:5173"
)

public class AppointmentController {

    private final AppointmentService service;

    public AppointmentController(
            AppointmentService service
    ){

        this.service=service;

    }

    @PostMapping

    public Appointment book(

            @RequestBody Appointment appointment

    ){

        return service.book(
                appointment
        );

    }

    @GetMapping("/{patientId}")

    public List<Appointment> get(

            @PathVariable
            Long patientId

    ){

        return service.patientAppointments(
                patientId
        );

    }

    @DeleteMapping("/{id}")

    public void cancel(

            @PathVariable
            Long id

    ){

        service.cancel(
                id
        );

    }

}