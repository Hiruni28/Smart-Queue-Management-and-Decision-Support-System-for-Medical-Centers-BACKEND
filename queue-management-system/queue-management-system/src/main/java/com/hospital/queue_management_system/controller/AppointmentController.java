package com.hospital.queue_management_system.controller;

import java.util.List;

import org.springframework.web.bind.annotation.*;

import com.hospital.queue_management_system.dto.AppointmentViewDTO;
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
            @PathVariable Long patientId
    ){

        return service.patientAppointments(
                patientId
        );

    }

    @PutMapping("/{id}")

    public Appointment update(
            @PathVariable Long id,
            @RequestBody Appointment appointment
    ){

        return service.updateAppointment(
                id,
                appointment
        );

    }

    @DeleteMapping("/{id}")

    public void cancel(
            @PathVariable Long id
    ){

        service.cancel(id);

    }

    @GetMapping("/staff/today/{doctorId}")

    public List<AppointmentViewDTO>
    today(
            @PathVariable Long doctorId
    ){

        return service.todayAppointments(
                doctorId
        );

    }

    @PutMapping("/status/{id}")

    public void updateStatus(
            @PathVariable Long id,
            @RequestParam String status
    ){

        service.updateStatus(
                id,
                status
        );

    }

}