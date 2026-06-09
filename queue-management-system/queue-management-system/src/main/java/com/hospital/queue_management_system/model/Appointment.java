package com.hospital.queue_management_system.model;

import jakarta.persistence.*;

import java.time.LocalDate;

import lombok.*;

@Entity

@Table(
        name="Appointments"
)

@Getter
@Setter

@NoArgsConstructor

@AllArgsConstructor

public class Appointment {

    @Id

    @GeneratedValue(
            strategy=
                    GenerationType.IDENTITY
    )

    private Long appointmentId;

    private Long patientId;

    private Long doctorId;

    private LocalDate appointmentDate;

    private String status;

}
