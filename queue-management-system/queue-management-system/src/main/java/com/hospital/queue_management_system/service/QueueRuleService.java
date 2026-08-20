package com.hospital.queue_management_system.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.hospital.queue_management_system.model.QueueRule;
import com.hospital.queue_management_system.repository.QueueRuleRepository;

@Service
public class QueueRuleService {

    private final QueueRuleRepository repo;

    public QueueRuleService(QueueRuleRepository repo) {
        this.repo = repo;
    }

    public List<QueueRule> getAll() {
        return repo.findAll();
    }

    public QueueRule save(QueueRule rule) {

        if (rule == null) {
            throw new RuntimeException(
                    "Queue rule data is required."
            );
        }

        if (rule.getRuleId() == null) {
            throw new RuntimeException(
                    "Queue rule ID is required."
            );
        }

        if (!repo.existsById(rule.getRuleId())) {
            throw new RuntimeException(
                    "Queue rule with ID "
                            + rule.getRuleId()
                            + " was not found."
            );
        }

        if (rule.getPriorityType() == null
                || rule.getPriorityType().trim().isEmpty()) {

            throw new RuntimeException(
                    "Priority type is required."
            );
        }

        if (rule.getPriorityOrder() == null
                || rule.getPriorityOrder() <= 0) {

            throw new RuntimeException(
                    "Priority order must be greater than zero."
            );
        }

        rule.setPriorityType(
                rule.getPriorityType().trim()
        );

        if (rule.getIsActive() == null) {
            rule.setIsActive(true);
        }

        Optional<QueueRule> existing =
                repo.findByPriorityTypeIgnoreCase(
                        rule.getPriorityType()
                );

        if (existing.isPresent()
                && !existing.get()
                .getRuleId()
                .equals(rule.getRuleId())) {

            throw new RuntimeException(
                    "A queue rule already exists for priority type: "
                            + rule.getPriorityType()
            );
        }

        return repo.save(rule);
    }
}