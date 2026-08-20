package com.hospital.queue_management_system.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hospital.queue_management_system.model.DoctorStatus;

@Repository
public interface DoctorStatusRepository
        extends JpaRepository<DoctorStatus, Long> {

    List<DoctorStatus> findAllByOrderByUpdatedAtDesc();

    Optional<DoctorStatus>
    findTopByDoctorIdOrderByUpdatedAtDesc(
            Long doctorId
    );
}