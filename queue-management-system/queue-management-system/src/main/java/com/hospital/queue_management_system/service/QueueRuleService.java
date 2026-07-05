package com.hospital.queue_management_system.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.hospital.queue_management_system.model.QueueRule;
import com.hospital.queue_management_system.repository.QueueRuleRepository;

@Service
public class QueueRuleService {

    private final QueueRuleRepository repo;

    public QueueRuleService(
            QueueRuleRepository repo
    ) {

        this.repo = repo;

    }

    public List<QueueRule> getAll() {

        return repo.findAll();

    }

    public QueueRule save(
            QueueRule rule
    ) {

        return repo.save(rule);

    }

    public void delete(
            Long id
    ) {

        repo.deleteById(id);

    }

}