package com.hospital.queue_management_system.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor

public class DashboardStatsDTO {

    private Long doctors;

    private Long staff;

    private Long appointments;

    private Long waitingPatients;

    private Long servingPatients;

}