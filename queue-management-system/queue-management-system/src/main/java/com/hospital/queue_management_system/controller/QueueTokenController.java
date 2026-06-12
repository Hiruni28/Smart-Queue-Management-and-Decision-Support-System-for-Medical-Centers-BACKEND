package com.hospital.queue_management_system.controller;

import java.util.*;

import org.springframework.web.bind.annotation.*;

import com.hospital.queue_management_system.model.QueueToken;
import com.hospital.queue_management_system.service.QueueTokenService;

@RestController

@RequestMapping("/queue")

@CrossOrigin(
        origins="http://localhost:5173"
)

public class QueueTokenController {

    private final QueueTokenService service;

    public QueueTokenController(
            QueueTokenService service
    ){

        this.service=service;

    }

    @GetMapping

    public List<QueueToken> getAll(){

        return service.getAll();

    }

    @PostMapping

    public QueueToken save(
            @RequestBody QueueToken queue
    ){

        return service.save(
                queue
        );

    }

    @PutMapping

    public QueueToken update(
            @RequestBody QueueToken queue
    ){

        return service.save(
                queue
        );

    }

    @DeleteMapping("/{id}")

    public void delete(
            @PathVariable Long id
    ){

        service.delete(
                id
        );

    }

}