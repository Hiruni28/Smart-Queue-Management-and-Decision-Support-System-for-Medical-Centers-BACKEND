package com.hospital.queue_management_system.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.hospital.queue_management_system.model.QueueToken;
import com.hospital.queue_management_system.repository.QueueTokenRepository;

@Service

public class QueueTokenService {

    private final QueueTokenRepository repo;

    public QueueTokenService(
            QueueTokenRepository repo
    ) {

        this.repo = repo;

    }


    // GET ALL QUEUES
    public List<QueueToken> getAll() {

        List<QueueToken> data =
                repo.findAll();

        // Debug output
        System.out.println("QUEUE DATA → " + data);

        return data;

    }


    // SAVE NEW QUEUE
    public QueueToken save(
            QueueToken queue
    ) {

        return repo.save(
                queue
        );

    }


    // DELETE QUEUE
    public void delete(
            Long id
    ) {

        repo.deleteById(
                id
        );

    }

}