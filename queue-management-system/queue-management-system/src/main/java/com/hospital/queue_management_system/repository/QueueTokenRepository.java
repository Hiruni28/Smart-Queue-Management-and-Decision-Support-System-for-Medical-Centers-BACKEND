package com.hospital.queue_management_system.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hospital.queue_management_system.model.QueueToken;

public interface QueueTokenRepository
        extends JpaRepository<QueueToken, Long> {

}