package com.hospital.queue_management_system.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

public class DoctorScheduleDTO {

    private Long scheduleId;

    private Long doctorId;

    private String doctorName;

    private String specialization;

    private String availableDate;

    private String startTime;

    private String endTime;

}