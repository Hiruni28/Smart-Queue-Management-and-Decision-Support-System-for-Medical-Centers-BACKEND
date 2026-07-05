package com.hospital.queue_management_system.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmergencyTransferDTO {

    private Long queueId;

    private Long newDoctorId;

    private String reason;

}