package com.hospital.queue_management_system.model;

import jakarta.persistence.*;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

import com.fasterxml.jackson.annotation.JsonFormat;

@Setter
@Getter

@Entity
@Table(name="doctor_schedules")

public class DoctorSchedule {

    @Id
    @GeneratedValue(
            strategy = GenerationType.IDENTITY
    )

    @Column(name="schedule_id")
    private Long scheduleId;

    @Column(name="doctor_id")
    private Long doctorId;

    @JsonFormat(
            pattern="yyyy-MM-dd"
    )

    @Column(name="available_date")

    private LocalDate availableDate;

    @JsonFormat(
            pattern="HH:mm:ss"
    )

    @Column(name="start_time")

    private LocalTime startTime;

    @JsonFormat(
            pattern="HH:mm:ss"
    )

    @Column(name="end_time")

    private LocalTime endTime;

}