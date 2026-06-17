package com.hospital.queue_management_system.service;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import com.hospital.queue_management_system.dto.AppointmentViewDTO;
import com.hospital.queue_management_system.model.Appointment;
import com.hospital.queue_management_system.model.Doctor;
import com.hospital.queue_management_system.model.Patient;
import com.hospital.queue_management_system.repository.AppointmentRepository;
import com.hospital.queue_management_system.repository.DoctorRepository;
import com.hospital.queue_management_system.repository.PatientRepository;

@Service
public class AppointmentService {

    private final AppointmentRepository repo;

    private final PatientRepository patientRepo;

    private final DoctorRepository doctorRepo;

    public AppointmentService(
            AppointmentRepository repo,
            PatientRepository patientRepo,
            DoctorRepository doctorRepo
    ){

        this.repo=repo;
        this.patientRepo=patientRepo;
        this.doctorRepo=doctorRepo;

    }

    public Appointment book(Appointment appointment){

        appointment.setStatus("Booked");

        return repo.save(appointment);

    }

    public List<Appointment> patientAppointments(
            Long id
    ){

        return repo.findByPatientId(id);

    }

    public Appointment updateAppointment(
            Long id,
            Appointment updated
    ){

        Appointment existing=
                repo.findById(id)
                        .orElseThrow();

        existing.setDoctorId(updated.getDoctorId());

        existing.setAppointmentDate(
                updated.getAppointmentDate()
        );

        existing.setAppointmentTime(
                updated.getAppointmentTime()
        );

        return repo.save(existing);

    }

    public void cancel(Long id){

        repo.deleteById(id);

    }

    public void updateStatus(
            Long id,
            String status
    ){

        Appointment app=
                repo.findById(id)
                        .orElseThrow();

        app.setStatus(status);

        repo.save(app);

    }

    public List<AppointmentViewDTO> todayAppointments(
            Long doctorId
    ){

        Date today=
                Date.valueOf(
                        LocalDate.now()
                );

        return repo
                .findByAppointmentDateAndDoctorId(
                        today,
                        doctorId
                )

                .stream()

                .map(a->{

                    Patient patient=
                            patientRepo
                                    .findById(
                                            a.getPatientId()
                                    )
                                    .orElse(null);

                    Doctor doctor=
                            doctorRepo
                                    .findById(
                                            a.getDoctorId()
                                    )
                                    .orElse(null);

                    AppointmentViewDTO dto=
                            new AppointmentViewDTO();

                    dto.setAppointmentId(
                            a.getAppointmentId()
                    );

                    dto.setPatientName(
                            patient!=null
                                    ?
                                    patient.getFullName()
                                    :
                                    "-"
                    );

                    dto.setDoctorName(
                            doctor!=null
                                    ?
                                    doctor.getDoctorName()
                                    :
                                    "-"
                    );

                    dto.setSpecialization(
                            doctor!=null
                                    ?
                                    doctor.getSpecialization()
                                    :
                                    "-"
                    );

                    dto.setRoomNumber(
                            doctor!=null
                                    ?
                                    doctor.getRoomNumber()
                                    :
                                    "-"
                    );

                    dto.setAppointmentDate(
                            a.getAppointmentDate().toString()
                    );

                    dto.setAppointmentTime(
                            a.getAppointmentTime().toString()
                    );

                    dto.setStatus(
                            a.getStatus()
                    );

                    return dto;

                })

                .toList();

    }

}