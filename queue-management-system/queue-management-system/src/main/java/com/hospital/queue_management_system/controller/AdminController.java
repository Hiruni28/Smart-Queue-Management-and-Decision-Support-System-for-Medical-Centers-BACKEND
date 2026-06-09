package com.hospital.queue_management_system.controller;

import org.springframework.web.bind.annotation.*;

import com.hospital.queue_management_system.dto.LoginRequest;
import com.hospital.queue_management_system.service.AdminService;

@RestController

@CrossOrigin(
        origins =
                "http://localhost:5173"
)

@RequestMapping("/admin")

public class AdminController {

    private final AdminService service;

    public AdminController(
            AdminService service
    ){

        this.service=
                service;

    }

    @PostMapping(
            "/login"
    )

    public String login(

            @RequestBody
            LoginRequest request

    ){

        boolean ok =
                service.login(
                        request
                );

        return ok
                ?
                "Login Success"
                :
                "Invalid Credentials";

    }

}