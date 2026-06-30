package com.hospital.queue_management_system.model;

import jakarta.persistence.*;

import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

@Entity
@Table(name = "DoctorStatus")

@Getter
@Setter
public class DoctorStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "status_id")
    private Long statusId;

    @Column(name = "doctor_id")
    private Long doctorId;

    @Column(name = "arrival_status")
    private String arrivalStatus;

    @Column(name = "delay_reason")
    private String delayReason;

    @Column(name = "updated_at")
    private Timestamp updatedAt;
}