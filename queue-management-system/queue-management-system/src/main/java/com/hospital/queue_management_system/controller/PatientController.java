package com.hospital.queue_management_system.controller;

import java.util.*;

import org.springframework.web.bind.annotation.*;

import com.hospital.queue_management_system.model.Patient;
import com.hospital.queue_management_system.service.PatientService;

@RestController

@RequestMapping("/patient")

@CrossOrigin(
        origins="http://localhost:5173"
)

public class PatientController {

    private final PatientService service;

    public PatientController(
            PatientService service
    ){

        this.service=service;

    }

    @GetMapping

    public List<Patient> getAll(){

        return service.getAll();

    }

    @PostMapping("/register")

    public String register(

            @RequestBody Patient patient

    ){

        return service.register(
                patient
        );

    }

    @PostMapping("/login")

    public String login(

            @RequestBody Map<String,String> body

    ){

        return service.login(

                body.get(
                        "email"
                ),

                body.get(
                        "password"
                )

        );

    }

    @GetMapping("/profile/{email}")

    public Patient profile(

            @PathVariable
            String email

    ){

        return service.getProfile(
                email
        );

    }


    @PutMapping("/profile")

    public Patient update(

            @RequestBody
            Patient patient

    ){

        return service.updateProfile(
                patient
        );

    }

}