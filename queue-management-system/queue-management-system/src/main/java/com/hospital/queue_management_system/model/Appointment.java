package com.hospital.queue_management_system.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;

@Getter
@Setter
@Entity

@Table(name="Appointments")

public class Appointment {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)

    @Column(name="appointment_id")
    private Long appointmentId;

    @Column(name="patient_id")
    private Long patientId;

    @Column(name="doctor_id")
    private Long doctorId;

    @Column(name="appointment_date")
    private Date appointmentDate;

    @Column(name="appointment_time")
    private Time appointmentTime;

    @Column(name="status")
    private String status;

    @Column(name="created_at")
    private Timestamp createdAt;

}