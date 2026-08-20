package com.hospital.queue_management_system.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

@Getter
@Setter
@Entity
@Table(name = "notifications")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_id")
    private Long notificationId;

    @Column(name = "patient_id", nullable = false)
    private Long patientId;

    @Column(name = "appointment_id")
    private Long appointmentId;

    @Column(name = "queue_id")
    private Long queueId;

    @Column(name = "doctor_id")
    private Long doctorId;

    @Column(name = "notification_type", nullable = false)
    private String notificationType;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "message", nullable = false)
    private String message;

    @Column(name = "is_read", nullable = false)
    private Boolean isRead = false;

    @Column(name = "created_at", nullable = false)
    private Timestamp createdAt;

    @PrePersist
    protected void onCreate() {

        if (createdAt == null) {
            createdAt = new Timestamp(
                    System.currentTimeMillis()
            );
        }

        if (isRead == null) {
            isRead = false;
        }
    }
}