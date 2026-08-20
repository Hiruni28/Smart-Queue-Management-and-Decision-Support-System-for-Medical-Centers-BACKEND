package com.hospital.queue_management_system.controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hospital.queue_management_system.dto.ReportsDashboardDTO;
import com.hospital.queue_management_system.service.ReportsService;

@RestController
@RequestMapping("/reports")
@CrossOrigin(origins = "http://localhost:5173")
public class ReportsController {

    private final ReportsService reportsService;

    public ReportsController(
            ReportsService reportsService
    ) {
        this.reportsService = reportsService;
    }

    // =========================================================
    // REPORT DASHBOARD
    // =========================================================

    @GetMapping("/dashboard")
    public ReportsDashboardDTO dashboard() {

        return reportsService.getDashboard();
    }
}