package com.hospital.queue_management_system.service;

import java.sql.Date;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.hospital.queue_management_system.dto.AppointmentViewDTO;
import com.hospital.queue_management_system.dto.StaffAppointmentDTO;
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
    ) {

        this.repo = repo;
        this.patientRepo = patientRepo;
        this.doctorRepo = doctorRepo;

    }

    // =========================
    // Patient Functions
    // =========================

    public Appointment book(
            Appointment appointment
    ) {

        if (
                appointment.getAppointmentDate()
                        .before(Date.valueOf(LocalDate.now()))
        ) {

            throw new RuntimeException(
                    "Cannot book appointments for past dates."
            );

        }

        appointment.setStatus("Booked");

        return repo.save(appointment);

    }

    public List<Appointment> patientAppointments(
            Long id
    ) {

        return repo.findByPatientId(
                id
        );

    }

    public Appointment updateAppointment(
            Long id,
            Appointment updated
    ) {

        Appointment existing =
                repo.findById(id)
                        .orElseThrow();

        existing.setDoctorId(
                updated.getDoctorId()
        );

        existing.setAppointmentDate(
                updated.getAppointmentDate()
        );

        existing.setAppointmentTime(
                updated.getAppointmentTime()
        );

        if (
                updated.getAppointmentDate()
                        .before(Date.valueOf(LocalDate.now()))
        ) {

            throw new RuntimeException(
                    "Cannot select a past date."
            );

        }

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

    // =========================
    // Staff Functions
    // =========================

    public List<AppointmentViewDTO> getAllAppointments() {

        return repo.findAllByOrderByAppointmentDateDesc()

                .stream()

                .map(a -> {

                    Patient patient =
                            patientRepo
                                    .findById(
                                            a.getPatientId()
                                    )
                                    .orElse(null);

                    Doctor doctor =
                            doctorRepo
                                    .findById(
                                            a.getDoctorId()
                                    )
                                    .orElse(null);

                    AppointmentViewDTO dto =
                            new AppointmentViewDTO();

                    dto.setAppointmentId(
                            a.getAppointmentId()
                    );

                    dto.setPatientId(
                            a.getPatientId()
                    );

                    dto.setDoctorId(
                            a.getDoctorId()
                    );

                    dto.setPatientName(
                            patient != null
                                    ? patient.getFullName()
                                    : "-"
                    );

                    dto.setDoctorName(
                            doctor != null
                                    ? doctor.getDoctorName()
                                    : "-"
                    );

                    dto.setSpecialization(
                            doctor != null
                                    ? doctor.getSpecialization()
                                    : "-"
                    );

                    dto.setRoomNumber(
                            doctor != null
                                    ? doctor.getRoomNumber()
                                    : "-"
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

    public Appointment staffCreateAppointment(
            StaffAppointmentDTO dto
    ) {

        Date appointmentDate =
                Date.valueOf(
                        dto.getAppointmentDate()
                );

        if (
                appointmentDate.toLocalDate()
                        .isBefore(LocalDate.now())
        ) {

            throw new RuntimeException(
                    "Cannot create appointments for past dates."
            );

        }

        Time appointmentTime =
                Time.valueOf(
                        LocalTime.parse(
                                dto.getAppointmentTime()
                        )
                );

        boolean alreadyBooked =
                repo.existsByPatientIdAndDoctorIdAndAppointmentDateAndAppointmentTime(
                        dto.getPatientId(),
                        dto.getDoctorId(),
                        appointmentDate,
                        appointmentTime
                );

        if (alreadyBooked) {

            throw new RuntimeException(
                    "This patient already has an appointment with this doctor at this time."
            );

        }

        Doctor doctor =
                doctorRepo.findById(
                        dto.getDoctorId()
                ).orElseThrow();

        long totalAppointments =
                repo.countByDoctorIdAndAppointmentDate(
                        dto.getDoctorId(),
                        appointmentDate
                );

        if (
                doctor.getMaxPatientsPerDay() != null
                        &&
                        totalAppointments >= doctor.getMaxPatientsPerDay()
        ) {

            throw new RuntimeException(
                    "Doctor has reached the maximum number of patients for this day."
            );

        }

        Appointment appointment =
                new Appointment();

        appointment.setPatientId(
                dto.getPatientId()
        );

        appointment.setDoctorId(
                dto.getDoctorId()
        );

        appointment.setAppointmentDate(
                appointmentDate
        );

        appointment.setAppointmentTime(
                appointmentTime
        );

        appointment.setStatus(
                dto.getStatus()
        );

        return repo.save(
                appointment
        );

    }

    public Appointment staffUpdateAppointment(
            Long id,
            StaffAppointmentDTO dto
    ) {

        Appointment appointment =
                repo.findById(id)
                        .orElseThrow();

        Date appointmentDate =
                Date.valueOf(
                        dto.getAppointmentDate()
                );

        if (
                appointmentDate.toLocalDate()
                        .isBefore(LocalDate.now())
        ) {

            throw new RuntimeException(
                    "Cannot create appointments for past dates."
            );

        }

        Time appointmentTime =
                Time.valueOf(
                        LocalTime.parse(
                                dto.getAppointmentTime()
                        )
                );

        boolean duplicate =
                repo.findAll().stream()

                        .anyMatch(a ->

                                !a.getAppointmentId().equals(id)

                                        && a.getPatientId().equals(dto.getPatientId())

                                        && a.getDoctorId().equals(dto.getDoctorId())

                                        && a.getAppointmentDate().equals(appointmentDate)

                                        && a.getAppointmentTime().equals(appointmentTime)
                        );

        if (duplicate) {

            throw new RuntimeException(
                    "This patient already has an appointment with this doctor at this time."
            );

        }

        Doctor doctor =
                doctorRepo.findById(
                        dto.getDoctorId()
                ).orElseThrow();

        long totalAppointments =
                repo.countByDoctorIdAndAppointmentDate(
                        dto.getDoctorId(),
                        appointmentDate
                );

        if (
                doctor.getMaxPatientsPerDay() != null
                        &&
                        totalAppointments > doctor.getMaxPatientsPerDay()
        ) {

            throw new RuntimeException(
                    "Doctor has reached the maximum number of patients for this day."
            );

        }

        appointment.setPatientId(
                dto.getPatientId()
        );

        appointment.setDoctorId(
                dto.getDoctorId()
        );

        appointment.setAppointmentDate(
                appointmentDate
        );

        appointment.setAppointmentTime(
                appointmentTime
        );

        appointment.setStatus(
                dto.getStatus()
        );

        return repo.save(
                appointment
        );

    }

    public void staffDeleteAppointment(
            Long id
    ) {

        repo.deleteById(
                id
        );

    }

    public void updateStatus(
            Long id,
            String status
    ) {

        Appointment app =
                repo.findById(id)
                        .orElseThrow();

        app.setStatus(
                status
        );

        repo.save(
                app
        );

    }

    // =========================
    // Today's Appointments
    // =========================

    public List<AppointmentViewDTO> todayAppointments(
            Long doctorId
    ) {

        Date today =
                Date.valueOf(
                        LocalDate.now()
                );

        return repo
                .findByAppointmentDateAndDoctorId(
                        today,
                        doctorId
                )

                .stream()

                .map(a -> {

                    Patient patient =
                            patientRepo
                                    .findById(
                                            a.getPatientId()
                                    )
                                    .orElse(null);

                    Doctor doctor =
                            doctorRepo
                                    .findById(
                                            a.getDoctorId()
                                    )
                                    .orElse(null);

                    AppointmentViewDTO dto =
                            new AppointmentViewDTO();

                    dto.setAppointmentId(
                            a.getAppointmentId()
                    );

                    dto.setPatientId(
                            a.getPatientId()
                    );

                    dto.setDoctorId(
                            a.getDoctorId()
                    );

                    dto.setPatientName(
                            patient != null
                                    ? patient.getFullName()
                                    : "-"
                    );

                    dto.setDoctorName(
                            doctor != null
                                    ? doctor.getDoctorName()
                                    : "-"
                    );

                    dto.setSpecialization(
                            doctor != null
                                    ? doctor.getSpecialization()
                                    : "-"
                    );

                    dto.setRoomNumber(
                            doctor != null
                                    ? doctor.getRoomNumber()
                                    : "-"
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