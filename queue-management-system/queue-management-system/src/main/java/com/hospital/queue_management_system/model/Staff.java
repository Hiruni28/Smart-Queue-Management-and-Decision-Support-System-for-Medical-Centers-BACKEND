package com.hospital.queue_management_system.model;

import jakarta.persistence.*;

import lombok.*;

@Entity

@Table(
        name="Staff"
)

@Getter
@Setter

@NoArgsConstructor

@AllArgsConstructor

public class Staff {

    @Id

    @GeneratedValue(
            strategy=
                    GenerationType.IDENTITY
    )

    private Long staffId;

    private String fullName;

    private String email;

    private String password;

}