package com.hospital.queue_management_system.repository;

import com.hospital.queue_management_system.model.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DoctorRepository
        extends JpaRepository<Doctor, Long> {
}