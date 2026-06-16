package com.hospital.queue_management_system.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hospital.queue_management_system.model.Staff;

public interface StaffRepository
        extends JpaRepository<Staff, Long>{

    Optional<Staff> findByEmail(
            String email
    );

}