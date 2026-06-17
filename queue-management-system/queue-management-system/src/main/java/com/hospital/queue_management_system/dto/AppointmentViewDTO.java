package com.hospital.queue_management_system.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

public class AppointmentViewDTO {

    private Long appointmentId;

    private String patientName;

    private String doctorName;

    private String specialization;

    private String roomNumber;

    private String appointmentDate;

    private String appointmentTime;

    private String status;

}