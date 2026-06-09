package com.hospital.queue_management_system.model;

import jakarta.persistence.*;
        import lombok.*;

@Entity
@Table(name="Admins")

@Getter
@Setter

@NoArgsConstructor

@AllArgsConstructor

public class Admin {

    @Id

    @GeneratedValue(
            strategy =
                    GenerationType.IDENTITY
    )

    private Long admin_id;

    private String name;

    @Column(
            unique=true
    )

    private String email;

    private String password;

}