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

    private Long doctorId;

    private String doctorName;

    private String specialization;

    private String availability;

    private String roomNumber;

}
