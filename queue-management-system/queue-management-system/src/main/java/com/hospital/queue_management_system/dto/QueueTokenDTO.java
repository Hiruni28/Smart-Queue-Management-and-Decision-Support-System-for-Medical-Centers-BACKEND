package com.hospital.queue_management_system.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QueueTokenDTO {

    private Long queueId;

    private Long appointmentId;

    private Long doctorId;

    private String tokenNumber;

    private String patientName;

    private String doctorName;

    private String queueStatus;

    private String priorityType;

    private Integer emergencyLevel;

    private String emergencyReason;

    private Long transferredToDoctorId;

    private Integer estimatedWaitTime;

    private String doctorStatus;

    private Integer delayMinutes;
}