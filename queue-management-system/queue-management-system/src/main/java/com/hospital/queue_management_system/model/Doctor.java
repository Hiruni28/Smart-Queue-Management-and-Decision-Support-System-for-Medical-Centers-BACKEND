package com.hospital.queue_management_system.model;

import jakarta.persistence.*;

import lombok.*;

@Entity

@Table(
        name="Doctors"
)

@Getter
@Setter

@NoArgsConstructor

@AllArgsConstructor

public class Doctor {

    @Id

    @GeneratedValue(
            strategy=
                    GenerationType.IDENTITY
    )

    @Column(
            name="doctor_id"
    )

    private Long doctorId;

    @Column(
            name="doctor_name"
    )

    private String doctorName;

    private String specialization;

    private String availability;

    @Column(
            name="room_number"
    )

    private String roomNumber;

}