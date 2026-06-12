package com.hospital.queue_management_system.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.hospital.queue_management_system.model.Patient;
import com.hospital.queue_management_system.repository.PatientRepository;

@Service

public class PatientService {

    private final PatientRepository repo;

    public PatientService(
            PatientRepository repo
    ){

        this.repo=repo;

    }

    public List<Patient> getAll(){

        return repo.findAll();

    }

    public String register(
            Patient patient
    ){

        if(
                repo.findByEmail(
                        patient.getEmail()
                ).isPresent()
        ){

            return "Email already exists";

        }

        repo.save(
                patient
        );

        return "Registration Success";

    }

    public String login(

            String email,

            String password

    ){

        Optional<Patient> patient=

                repo.findByEmail(
                        email
                );

        if(
                patient.isPresent()
                        &&
                        patient.get()
                                .getPassword()
                                .equals(
                                        password
                                )
        ){

            return "Login Success";

        }

        return "Invalid Credentials";

    }

    public Patient getProfile(
            String email
    ){

        return repo
                .findByEmail(email)
                .orElse(null);

    }

    public Patient updateProfile(
            Patient patient
    ){

        return repo.save(
                patient
        );

    }

}