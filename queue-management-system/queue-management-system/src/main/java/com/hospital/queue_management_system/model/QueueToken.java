package com.hospital.queue_management_system.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

@Getter
@Setter
@Entity
@Table(name = "queuetokens")
public class QueueToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "queue_id")
    private Long queueId;

    @Column(name = "appointment_id", nullable = false)
    private Long appointmentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "appointment_id",
            referencedColumnName = "appointment_id",
            insertable = false,
            updatable = false
    )
    private Appointment appointment;

    @Column(name = "doctor_id", nullable = false)
    private Long doctorId;

    @Column(name = "token_number", nullable = false)
    private String tokenNumber;

    @Column(name = "queue_status")
    private String queueStatus = "Waiting";

    @Column(name = "priority_type")
    private String priorityType = "Normal";

    @Column(name = "emergency_level")
    private Integer emergencyLevel = 0;

    @Column(name = "emergency_reason")
    private String emergencyReason;

    @Column(name = "transferred_to_doctor_id")
    private Long transferredToDoctorId;

    @Column(name = "estimated_wait_time", nullable = false)
    private Integer estimatedWaitTime = 0;

    @Column(name = "created_at")
    private Timestamp createdAt;

    @PrePersist
    protected void onCreate() {

        if (createdAt == null) {
            createdAt = new Timestamp(
                    System.currentTimeMillis()
            );
        }

        if (queueStatus == null
                || queueStatus.trim().isEmpty()) {

            queueStatus = "Waiting";
        }

        if (priorityType == null
                || priorityType.trim().isEmpty()) {

            priorityType = "Normal";
        }

        if (emergencyLevel == null) {
            emergencyLevel = 0;
        }

        if (estimatedWaitTime == null) {
            estimatedWaitTime = 0;
        }
    }
}