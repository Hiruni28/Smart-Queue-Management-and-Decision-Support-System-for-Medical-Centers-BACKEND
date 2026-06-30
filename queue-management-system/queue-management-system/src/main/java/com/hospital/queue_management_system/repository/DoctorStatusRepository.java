package com.hospital.queue_management_system.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hospital.queue_management_system.model.DoctorStatus;

public interface DoctorStatusRepository
        extends JpaRepository<DoctorStatus, Long> {

    Optional<DoctorStatus> findByDoctorId(
            Long doctorId
    );

}