package com.hospital.queue_management_system.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.sql.Date;
import java.sql.Time;

@Setter
@Getter
@Entity

@Table(name="doctor_schedules")

public class DoctorSchedule {

    @Id

    @GeneratedValue(
            strategy=GenerationType.IDENTITY
    )

    @Column(name="schedule_id")

    private Long scheduleId;

    @Column(name="doctor_id")

    private Long doctorId;

    @Column(name="available_date")

    private Date availableDate;

    @Column(name="start_time")

    private Time startTime;

    @Column(name="end_time")

    private Time endTime;

}