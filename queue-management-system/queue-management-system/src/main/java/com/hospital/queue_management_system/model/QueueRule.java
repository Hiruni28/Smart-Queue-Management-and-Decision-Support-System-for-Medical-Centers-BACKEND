package com.hospital.queue_management_system.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "queuerules")
public class QueueRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "rule_id")
    private Long ruleId;

    @Column(name = "description")
    private String description;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "priority_order")
    private Integer priorityOrder;

    @Column(name = "priority_type")
    private String priorityType;
}