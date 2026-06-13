package com.hospital.queue_management_system.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hospital.queue_management_system.model.Appointment;

public interface AppointmentRepository
        extends JpaRepository<Appointment,Long>{

    List<Appointment> findByPatientId(
            Long patientId
    );

}