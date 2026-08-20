package com.hospital.queue_management_system.repository;

import com.hospital.queue_management_system.model.QueueRule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface QueueRuleRepository
        extends JpaRepository<QueueRule, Long> {

    Optional<QueueRule> findByPriorityTypeIgnoreCase(
            String priorityType
    );
}