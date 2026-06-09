package com.hospital.queue_management_system.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hospital.queue_management_system.model.Admin;

public interface AdminRepository
        extends JpaRepository<Admin,Long>{

    Optional<Admin>
    findByEmail(
            String email
    );

}