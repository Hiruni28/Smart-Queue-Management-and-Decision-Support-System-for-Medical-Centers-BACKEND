package com.hospital.queue_management_system.controller;

import java.util.List;

import org.springframework.web.bind.annotation.*;

import com.hospital.queue_management_system.model.DoctorSchedule;
import com.hospital.queue_management_system.service.DoctorScheduleService;

@RestController
@RequestMapping("/schedule")

@CrossOrigin(
        origins = "http://localhost:5173"
)

public class DoctorScheduleController {

    private final DoctorScheduleService service;

    public DoctorScheduleController(
            DoctorScheduleService service
    ) {
        this.service = service;
    }

    @GetMapping
    public List<DoctorSchedule> getAll() {

        return service.getAll();

    }

    @GetMapping("/{doctorId}")
    public List<DoctorSchedule> get(
            @PathVariable
            Long doctorId
    ) {

        return service.getByDoctor(
                doctorId
        );

    }

    @PostMapping
    public DoctorSchedule add(

            @RequestBody
            DoctorSchedule schedule

    ) {

        return service.add(
                schedule
        );

    }

    @PutMapping
    public DoctorSchedule update(

            @RequestBody
            DoctorSchedule schedule

    ) {

        return service.update(
                schedule
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