package com.hospital.queue_management_system.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StaffAppointmentDTO {

    private Long appointmentId;

    private Long patientId;

    private Long doctorId;

    private String appointmentDate;

    private String appointmentTime;

    private String status;

}