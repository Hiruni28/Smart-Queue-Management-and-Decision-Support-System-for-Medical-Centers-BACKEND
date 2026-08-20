package com.hospital.queue_management_system.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AppointmentViewDTO {

    private Long appointmentId;

    private Long patientId;

    private Long doctorId;

    private String patientName;

    private String doctorName;

    private String specialization;

    private String roomNumber;

    private String appointmentDate;

    private String appointmentTime;

    private String status;

    private String queueToken;

    private Integer estimatedWaitTime;

    private Integer age;

    private Boolean hasSpecialNeeds;

    private String calculatedPriority;

    // =========================================================
    // EMERGENCY LEVEL
    // =========================================================

    private Integer emergencyLevel;
}