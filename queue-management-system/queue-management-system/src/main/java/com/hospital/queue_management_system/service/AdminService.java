package com.hospital.queue_management_system.service;

import org.springframework.stereotype.Service;

import com.hospital.queue_management_system.dto.LoginRequest;
import com.hospital.queue_management_system.repository.AdminRepository;

@Service

public class AdminService {

    private final AdminRepository repo;

    public AdminService(
            AdminRepository repo
    ){

        this.repo=repo;

    }

    public boolean login(
            LoginRequest request
    ){

        return repo
                .findByEmail(
                        request.getEmail()
                )

                .map(

                        admin ->

                                admin
                                        .getPassword()
                                        .equals(
                                                request.getPassword()
                                        )

                )

                .orElse(false);

    }

}