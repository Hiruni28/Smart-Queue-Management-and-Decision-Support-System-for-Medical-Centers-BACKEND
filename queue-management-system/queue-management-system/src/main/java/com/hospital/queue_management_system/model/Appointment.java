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
@Table(name = "appointments")
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "appointment_id")
    private Long appointmentId;

    @Column(name = "patient_id", nullable = false)
    private Long patientId;

    @Column(name = "doctor_id", nullable = false)
    private Long doctorId;

    @Column(name = "appointment_date", nullable = false)
    private Date appointmentDate;

    @Column(name = "appointment_time", nullable = false)
    private Time appointmentTime;

    @Column(name = "status", nullable = false)
    private String status = "Booked";

    // =========================================================
    // EMERGENCY LEVEL
    // 0 = Normal
    // 1 = Low
    // 2 = Urgent
    // 3 = Emergency
    // =========================================================

    @Column(name = "emergency_level", nullable = false)
    private Integer emergencyLevel = 0;

    @Column(name = "created_at")
    private Timestamp createdAt;

    @PrePersist
    protected void onCreate() {

        if (createdAt == null) {
            createdAt = new Timestamp(System.currentTimeMillis());
        }

        if (status == null || status.trim().isEmpty()) {
            status = "Booked";
        }

        if (emergencyLevel == null) {
            emergencyLevel = 0;
        }
    }
}