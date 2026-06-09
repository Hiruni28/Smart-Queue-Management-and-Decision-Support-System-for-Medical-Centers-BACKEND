package com.hospital.queue_management_system.controller;

import java.util.*;

import org.springframework.web.bind.annotation.*;

import com.hospital.queue_management_system.model.Doctor;
import com.hospital.queue_management_system.service.DoctorService;

@RestController

@RequestMapping(
        "/doctors"
)

@CrossOrigin(
        origins =
                "http://localhost:5173"
)

public class DoctorController {

    private final DoctorService service;

    public DoctorController(
            DoctorService service
    ){

        this.service=
                service;

    }

    @GetMapping

    public List<Doctor> all(){

        return service.getAll();

    }

    @PostMapping

    public Doctor add(

            @RequestBody
            Doctor doctor

    ){

        return service.add(
                doctor
        );

    }

    @PutMapping

    public Doctor update(

            @RequestBody
            Doctor doctor

    ){

        return service.update(
                doctor
        );

    }

    @DeleteMapping(
            "/{id}"
    )

    public void delete(

            @PathVariable
            Long id

    ){

        service.delete(
                id
        );

    }

}
