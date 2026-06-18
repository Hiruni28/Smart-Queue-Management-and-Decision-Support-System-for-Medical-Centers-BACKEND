package com.hospital.queue_management_system.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hospital.queue_management_system.model.Patient;

public interface PatientRepository
        extends JpaRepository<Patient,Long>{

    Optional<Patient> findByEmail(
            String email
    );

    List<Patient> findByFullNameContainingIgnoreCase(
            String fullName
    );

    List<Patient> findByPhoneContaining(
            String phone
    );

}