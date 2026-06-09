package com.hospital.queue_management_system.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.hospital.queue_management_system.model.Doctor;
import com.hospital.queue_management_system.repository.DoctorRepository;

@Service

public class DoctorService {

    private final DoctorRepository repo;

    public DoctorService(
            DoctorRepository repo
    ){

        this.repo=
                repo;

    }

    public List<Doctor> getAll(){

        return repo.findAll();

    }

    public Doctor add(
            Doctor doctor
    ){

        return repo.save(
                doctor
        );

    }

    public Doctor update(
            Doctor doctor
    ){

        return repo.save(
                doctor
        );

    }

    public void delete(
            Long id
    ){

        repo.deleteById(
                id
        );

    }

}