package com.hospital.queue_management_system.controller;

import org.springframework.web.bind.annotation.*;

import com.hospital.queue_management_system.dto.DashboardStatsDTO;
import com.hospital.queue_management_system.service.DashboardService;

@RestController

@CrossOrigin(
        origins =
                "http://localhost:5173"
)

@RequestMapping(
        "/dashboard"
)

public class DashboardController {

    private final DashboardService service;

    public DashboardController(
            DashboardService service
    ){

        this.service=
                service;

    }

    @GetMapping

    public DashboardStatsDTO stats(){

        return service.getStats();

    }

}
