package com.hospital.queue_management_system.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
@Table(name="staff")

public class Staff {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)

    private Long staffId;

    private String fullName;

    @Column(unique=true)

    private String email;

    private String password;

    public Staff(){}

}