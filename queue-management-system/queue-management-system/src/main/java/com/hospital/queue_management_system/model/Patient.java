package com.hospital.queue_management_system.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;
import java.sql.Date;

@Setter
@Getter
@Entity

@Table(name="patients")

public class Patient {

    @Id

    @GeneratedValue(
            strategy=GenerationType.IDENTITY
    )

    @Column(name="patient_id")
    private Long patientId;

    @Column(name="full_name")
    private String fullName;

    @Column(unique=true)
    private String email;

    private String password;

    private String phone;

    @Column(name="date_of_birth")
    private Date dateOfBirth;

    @Column(name="created_at")
    private Timestamp createdAt;


}