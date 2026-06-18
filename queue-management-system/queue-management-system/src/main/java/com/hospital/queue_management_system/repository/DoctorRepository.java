package com.hospital.queue_management_system.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hospital.queue_management_system.model.Doctor;

public interface DoctorRepository
        extends JpaRepository<Doctor,Long>{

    List<Doctor> findAll();

}