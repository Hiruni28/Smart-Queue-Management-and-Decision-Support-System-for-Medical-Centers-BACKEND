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
            Date appointmentDate,
            Long doctorId
    );

    // Emergency patients first
    List<Appointment> findByAppointmentDateAndDoctorIdOrderByEmergencyLevelDescAppointmentTimeAscAppointmentIdAsc(
            Date appointmentDate,
            Long doctorId
    );

    List<Appointment> findAllByOrderByAppointmentDateDesc();

    boolean existsByPatientIdAndDoctorIdAndAppointmentDate(
            Long patientId,
            Long doctorId,
            Date appointmentDate
    );

    boolean existsByPatientIdAndDoctorIdAndAppointmentDateAndAppointmentIdNot(
            Long patientId,
            Long doctorId,
            Date appointmentDate,
            Long appointmentId
    );

    boolean existsByPatientIdAndDoctorIdAndAppointmentDateAndAppointmentTime(
            Long patientId,
            Long doctorId,
            Date appointmentDate,
            Time appointmentTime
    );

    boolean existsByPatientIdAndDoctorIdAndAppointmentDateAndAppointmentTimeAndAppointmentIdNot(
            Long patientId,
            Long doctorId,
            Date appointmentDate,
            Time appointmentTime,
            Long appointmentId
    );

    long countByDoctorIdAndAppointmentDateAndStatusNot(
            Long doctorId,
            Date appointmentDate,
            String status
    );

    long countByDoctorIdAndAppointmentDateAndStatusNotAndAppointmentIdNot(
            Long doctorId,
            Date appointmentDate,
            String status,
            Long appointmentId
    );
}