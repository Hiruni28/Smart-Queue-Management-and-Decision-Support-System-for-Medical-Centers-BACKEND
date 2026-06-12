package com.hospital.queue_management_system.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

@Setter
@Getter
@Entity
@Table(name="queuetokens")

public class QueueToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    @Column(name="queue_id")
    private Long queueId;

    @Column(name="appointment_id")
    private Long appointmentId;

    @Column(name="token_number")
    private String tokenNumber;

    @Column(name="queue_status")
    private String queueStatus;

    @Column(name="priority_type")
    private String priorityType;

    @Column(name="estimated_wait_time")
    private Integer estimatedWaitTime;

    @Column(name="created_at")
    private Timestamp createdAt;


}