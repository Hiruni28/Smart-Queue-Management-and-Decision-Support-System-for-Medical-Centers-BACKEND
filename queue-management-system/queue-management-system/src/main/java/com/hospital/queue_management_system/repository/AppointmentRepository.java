package com.hospital.queue_management_system.repository;

import java.sql.Date;
import java.sql.Time;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hospital.queue_management_system.model.Appointment;

public interface AppointmentRepository
        extends JpaRepository<Appointment, Long> {

    List<Appointment> findByPatientId(
            Long patientId
    );

    List<Appointment> findByAppointmentDateAndDoctorId(
            Date date,
            Long doctorId
    );

    List<Appointment> findAllByOrderByAppointmentDateDesc();

    boolean existsByPatientIdAndDoctorIdAndAppointmentDateAndAppointmentTime(
            Long patientId,
            Long doctorId,
            Date appointmentDate,
            Time appointmentTime
    );

    long countByDoctorIdAndAppointmentDate(
            Long doctorId,
            Date appointmentDate
    );
}