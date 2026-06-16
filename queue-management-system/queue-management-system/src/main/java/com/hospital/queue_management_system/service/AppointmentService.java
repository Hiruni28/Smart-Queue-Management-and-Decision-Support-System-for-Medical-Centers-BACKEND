package com.hospital.queue_management_system.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.hospital.queue_management_system.model.Appointment;
import com.hospital.queue_management_system.repository.AppointmentRepository;

@Service

public class AppointmentService {

    private final AppointmentRepository repo;

    public AppointmentService(
            AppointmentRepository repo
    ) {

        this.repo = repo;

    }

    public Appointment book(
            Appointment appointment
    ) {

        appointment.setStatus(
                "Booked"
        );

        return repo.save(
                appointment
        );

    }

    public List<Appointment> patientAppointments(
            Long patientId
    ) {

        return repo.findByPatientId(
                patientId
        );

    }

    public Appointment updateAppointment(

            Long id,

            Appointment updated

    ) {

        Appointment existing =
                repo.findById(id)
                        .orElseThrow(
                                () ->
                                        new RuntimeException(
                                                "Appointment not found"
                                        )
                        );

        existing.setDoctorId(
                updated.getDoctorId()
        );

        existing.setAppointmentDate(
                updated.getAppointmentDate()
        );

        existing.setAppointmentTime(
                updated.getAppointmentTime()
        );

        existing.setStatus(
                "Booked"
        );

        return repo.save(
                existing
        );

    }

    public void cancel(
            Long id
    ) {

        repo.deleteById(
                id
        );

    }

}

