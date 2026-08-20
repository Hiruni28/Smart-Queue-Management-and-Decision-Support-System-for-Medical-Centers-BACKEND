package com.hospital.queue_management_system.service;

import java.sql.Date;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hospital.queue_management_system.dto.AppointmentViewDTO;
import com.hospital.queue_management_system.dto.QueueTokenDTO;
import com.hospital.queue_management_system.dto.StaffAppointmentDTO;

import com.hospital.queue_management_system.model.Appointment;
import com.hospital.queue_management_system.model.Doctor;
import com.hospital.queue_management_system.model.DoctorSchedule;
import com.hospital.queue_management_system.model.Patient;
import com.hospital.queue_management_system.model.QueueToken;

import com.hospital.queue_management_system.repository.AppointmentRepository;
import com.hospital.queue_management_system.repository.DoctorRepository;
import com.hospital.queue_management_system.repository.DoctorScheduleRepository;
import com.hospital.queue_management_system.repository.PatientRepository;
import com.hospital.queue_management_system.repository.QueueTokenRepository;

@Service
public class AppointmentService {

    private final AppointmentRepository repo;
    private final PatientRepository patientRepo;
    private final DoctorRepository doctorRepo;
    private final QueueTokenRepository queueTokenRepo;
    private final DoctorScheduleRepository doctorScheduleRepo;
    private final QueueTokenService queueTokenService;

    public AppointmentService(
            AppointmentRepository repo,
            PatientRepository patientRepo,
            DoctorRepository doctorRepo,
            QueueTokenRepository queueTokenRepo,
            DoctorScheduleRepository doctorScheduleRepo,
            QueueTokenService queueTokenService
    ) {
        this.repo = repo;
        this.patientRepo = patientRepo;
        this.doctorRepo = doctorRepo;
        this.queueTokenRepo = queueTokenRepo;
        this.doctorScheduleRepo = doctorScheduleRepo;
        this.queueTokenService = queueTokenService;
    }

    // =========================================================
    // PATIENT BOOK
    // =========================================================

    @Transactional
    public Appointment book(
            Appointment appointment
    ) {

        validateAppointmentInput(appointment);

        validateEmergencyLevel(
                appointment.getEmergencyLevel()
        );

        LocalDate appointmentDate =
                appointment.getAppointmentDate().toLocalDate();

        LocalTime appointmentTime =
                appointment.getAppointmentTime().toLocalTime();

        validateFutureDateTime(
                appointmentDate,
                appointmentTime
        );

        // Check doctor exists
        doctorRepo.findById(
                appointment.getDoctorId()
        ).orElseThrow(
                () -> new RuntimeException(
                        "Doctor not found."
                )
        );

        // Check patient exists
        patientRepo.findById(
                appointment.getPatientId()
        ).orElseThrow(
                () -> new RuntimeException(
                        "Patient not found."
                )
        );

        // Find the schedule for this doctor/date/time
        DoctorSchedule selectedSchedule =
                findSchedule(
                        appointment.getDoctorId(),
                        appointmentDate,
                        appointmentTime
                );

        // Use schedule maxPatients
        checkScheduleCapacity(
                appointment.getDoctorId(),
                appointment.getAppointmentDate(),
                selectedSchedule.getMaxPatients()
        );

        boolean alreadyBooked =
                repo.existsByPatientIdAndDoctorIdAndAppointmentDate(
                        appointment.getPatientId(),
                        appointment.getDoctorId(),
                        appointment.getAppointmentDate()
                );

        if (alreadyBooked) {
            throw new RuntimeException(
                    "You already have an appointment with this doctor for this date."
            );
        }

        appointment.setStatus("Booked");

        return repo.save(appointment);
    }

    // =========================================================
    // PATIENT APPOINTMENT VIEWS
    // =========================================================

    public List<AppointmentViewDTO> patientAppointmentViews(
            Long patientId
    ) {

        if (patientId == null) {
            throw new RuntimeException(
                    "Patient ID is required."
            );
        }

        return repo.findByPatientId(patientId)
                .stream()
                .map(this::toAppointmentView)
                .toList();
    }

    // =========================================================
    // PATIENT UPDATE
    // =========================================================

    @Transactional
    public Appointment updateAppointment(
            Long id,
            Appointment updated
    ) {

        if (id == null) {
            throw new RuntimeException(
                    "Appointment ID is required."
            );
        }

        if (updated == null) {
            throw new RuntimeException(
                    "Appointment data is required."
            );
        }

        validateEmergencyLevel(
                updated.getEmergencyLevel()
        );



        Appointment existing =
                repo.findById(id)
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "Appointment not found."
                                )
                        );

        if (updated.getPatientId() != null
                && !existing.getPatientId()
                .equals(updated.getPatientId())) {

            throw new RuntimeException(
                    "You are not allowed to update this appointment."
            );
        }

        if (!"Booked".equalsIgnoreCase(
                existing.getStatus()
        )) {

            throw new RuntimeException(
                    "Only booked appointments can be updated."
            );
        }

        if (updated.getDoctorId() == null) {
            throw new RuntimeException(
                    "Doctor is required."
            );
        }

        if (updated.getAppointmentDate() == null) {
            throw new RuntimeException(
                    "Appointment date is required."
            );
        }

        if (updated.getAppointmentTime() == null) {
            throw new RuntimeException(
                    "Appointment time is required."
            );
        }

        LocalDate date =
                updated.getAppointmentDate().toLocalDate();

        LocalTime time =
                updated.getAppointmentTime().toLocalTime();

        validateFutureDateTime(
                date,
                time
        );

        // Check doctor exists
        doctorRepo.findById(
                updated.getDoctorId()
        ).orElseThrow(
                () -> new RuntimeException(
                        "Doctor not found."
                )
        );

        // Find schedule
        DoctorSchedule selectedSchedule =
                findSchedule(
                        updated.getDoctorId(),
                        date,
                        time
                );

        // Check capacity using schedule maxPatients
        checkScheduleCapacityForUpdate(
                updated.getDoctorId(),
                updated.getAppointmentDate(),
                selectedSchedule.getMaxPatients(),
                id
        );

        boolean duplicate =
                repo.existsByPatientIdAndDoctorIdAndAppointmentDateAndAppointmentIdNot(
                        existing.getPatientId(),
                        updated.getDoctorId(),
                        updated.getAppointmentDate(),
                        id
                );

        if (duplicate) {
            throw new RuntimeException(
                    "You already have an appointment with this doctor for this date."
            );
        }

        existing.setDoctorId(
                updated.getDoctorId()
        );

        existing.setAppointmentDate(
                updated.getAppointmentDate()
        );

        existing.setAppointmentTime(
                updated.getAppointmentTime()
        );

        if (updated.getEmergencyLevel() != null) {

            existing.setEmergencyLevel(
                    updated.getEmergencyLevel()
            );
        }

        existing.setStatus("Booked");

        return repo.save(existing);
    }

    // =========================================================
    // PATIENT CANCEL
    // =========================================================

    @Transactional
    public void cancel(Long id) {

        if (id == null) {
            throw new RuntimeException(
                    "Appointment ID is required."
            );
        }

        Appointment app =
                repo.findById(id)
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "Appointment not found."
                                )
                        );

        String status =
                app.getStatus() == null
                        ? ""
                        : app.getStatus().trim();

        if ("Completed".equalsIgnoreCase(status)) {
            throw new RuntimeException(
                    "Completed appointments cannot be cancelled."
            );
        }

        if ("Cancelled".equalsIgnoreCase(status)) {
            return;
        }

        app.setStatus("Cancelled");

        repo.save(app);

        queueTokenRepo
                .findByAppointmentId(
                        app.getAppointmentId()
                )
                .ifPresent(token -> {

                    token.setQueueStatus("Cancelled");

                    queueTokenRepo.save(token);
                });
    }

    // =========================================================
    // STAFF - GET ALL
    // =========================================================

    public List<AppointmentViewDTO> getAllAppointments() {

        return repo
                .findAllByOrderByAppointmentDateDesc()
                .stream()
                .map(this::toAppointmentView)
                .toList();
    }

    // =========================================================
    // STAFF CREATE
    // =========================================================

    @Transactional
    public Appointment staffCreateAppointment(
            StaffAppointmentDTO dto
    ) {

        validateStaffDTO(dto);

        validateEmergencyLevel(
                dto.getEmergencyLevel()
        );

        Date appointmentDate;
        Time appointmentTime;

        try {

            appointmentDate =
                    Date.valueOf(
                            dto.getAppointmentDate().trim()
                    );

            appointmentTime =
                    Time.valueOf(
                            LocalTime.parse(
                                    dto.getAppointmentTime().trim()
                            )
                    );

        } catch (Exception e) {

            throw new RuntimeException(
                    "Invalid appointment date or time."
            );
        }

        LocalDate date =
                appointmentDate.toLocalDate();

        LocalTime time =
                appointmentTime.toLocalTime();

        validateFutureDateTime(
                date,
                time
        );

        patientRepo.findById(
                dto.getPatientId()
        ).orElseThrow(
                () -> new RuntimeException(
                        "Patient not found."
                )
        );

        // Check doctor exists
        doctorRepo.findById(
                dto.getDoctorId()
        ).orElseThrow(
                () -> new RuntimeException(
                        "Doctor not found."
                )
        );

        // Find schedule
        DoctorSchedule schedule =
                findSchedule(
                        dto.getDoctorId(),
                        date,
                        time
                );

        // Use schedule maxPatients
        checkScheduleCapacity(
                dto.getDoctorId(),
                appointmentDate,
                schedule.getMaxPatients()
        );

        boolean duplicate =
                repo.existsByPatientIdAndDoctorIdAndAppointmentDateAndAppointmentTime(
                        dto.getPatientId(),
                        dto.getDoctorId(),
                        appointmentDate,
                        appointmentTime
                );

        if (duplicate) {
            throw new RuntimeException(
                    "This patient already has an appointment with this doctor at this time."
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

        appointment.setStatus("Booked");

        appointment.setEmergencyLevel(
                dto.getEmergencyLevel() != null
                        ? dto.getEmergencyLevel()
                        : 0
        );

        return repo.save(appointment);
    }

    // =========================================================
    // STAFF UPDATE
    // =========================================================

    @Transactional
    public Appointment staffUpdateAppointment(
            Long id,
            StaffAppointmentDTO dto
    ) {

        if (id == null) {
            throw new RuntimeException(
                    "Appointment ID is required."
            );
        }

        Appointment appointment =
                repo.findById(id)
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "Appointment not found."
                                )
                        );

        validateStaffDTO(dto);

        validateEmergencyLevel(
                dto.getEmergencyLevel()
        );

        String currentStatus =
                appointment.getStatus() == null
                        ? "Booked"
                        : appointment.getStatus().trim();

        if (!"Booked".equalsIgnoreCase(
                currentStatus
        )) {

            throw new RuntimeException(
                    "Only booked appointments can be edited."
            );
        }

        Date appointmentDate;
        Time appointmentTime;

        try {

            appointmentDate =
                    Date.valueOf(
                            dto.getAppointmentDate().trim()
                    );

            appointmentTime =
                    Time.valueOf(
                            LocalTime.parse(
                                    dto.getAppointmentTime().trim()
                            )
                    );

        } catch (Exception e) {

            throw new RuntimeException(
                    "Invalid appointment date or time."
            );
        }

        LocalDate date =
                appointmentDate.toLocalDate();

        LocalTime time =
                appointmentTime.toLocalTime();

        validateFutureDateTime(
                date,
                time
        );

        patientRepo.findById(
                dto.getPatientId()
        ).orElseThrow(
                () -> new RuntimeException(
                        "Patient not found."
                )
        );

        // Check doctor exists
        doctorRepo.findById(
                dto.getDoctorId()
        ).orElseThrow(
                () -> new RuntimeException(
                        "Doctor not found."
                )
        );

        // Find schedule
        DoctorSchedule schedule =
                findSchedule(
                        dto.getDoctorId(),
                        date,
                        time
                );

        // Check capacity using schedule maxPatients
        checkScheduleCapacityForUpdate(
                dto.getDoctorId(),
                appointmentDate,
                schedule.getMaxPatients(),
                id
        );

        boolean duplicate =
                repo.existsByPatientIdAndDoctorIdAndAppointmentDateAndAppointmentTimeAndAppointmentIdNot(
                        dto.getPatientId(),
                        dto.getDoctorId(),
                        appointmentDate,
                        appointmentTime,
                        id
                );

        if (duplicate) {
            throw new RuntimeException(
                    "This patient already has an appointment with this doctor at this time."
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

        appointment.setStatus("Booked");

        appointment.setEmergencyLevel(
                dto.getEmergencyLevel() != null
                        ? dto.getEmergencyLevel()
                        : appointment.getEmergencyLevel()
        );

        return repo.save(appointment);
    }

    // =========================================================
    // STAFF DELETE
    // =========================================================

    @Transactional
    public void staffDeleteAppointment(
            Long id
    ) {

        if (id == null) {
            throw new RuntimeException(
                    "Appointment ID is required."
            );
        }

        Appointment appointment =
                repo.findById(id)
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "Appointment not found."
                                )
                        );

        queueTokenRepo.deleteByAppointmentId(id);

        repo.delete(appointment);
    }

    // =========================================================
    // STAFF UPDATE STATUS
    // =========================================================

    @Transactional
    public void updateStatus(
            Long id,
            String status
    ) {

        Appointment app =
                repo.findById(id)
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "Appointment not found."
                                )
                        );

        String normalizedStatus =
                normalizeAppointmentStatus(status);

        String currentStatus =
                app.getStatus() == null
                        ? "Booked"
                        : app.getStatus().trim();

        if ("Completed".equalsIgnoreCase(currentStatus)
                && !"Completed".equalsIgnoreCase(normalizedStatus)) {

            throw new RuntimeException(
                    "Completed appointments cannot be changed."
            );
        }

        if ("Cancelled".equalsIgnoreCase(currentStatus)
                && !"Cancelled".equalsIgnoreCase(normalizedStatus)) {

            throw new RuntimeException(
                    "Cancelled appointments cannot be changed."
            );
        }

        app.setStatus(normalizedStatus);

        repo.save(app);

        // =====================================================
        // CONFIRMED -> CREATE QUEUE TOKEN
        // =====================================================

        if ("Confirmed".equalsIgnoreCase(normalizedStatus)) {

            boolean exists =
                    queueTokenRepo
                            .findByAppointmentId(
                                    app.getAppointmentId()
                            )
                            .isPresent();

            if (!exists) {

                QueueTokenDTO tokenDTO =
                        new QueueTokenDTO();

                tokenDTO.setAppointmentId(
                        app.getAppointmentId()
                );

                tokenDTO.setDoctorId(
                        app.getDoctorId()
                );

                /*
                 * Determine queue priority.
                 *
                 * Emergency level 1-3 always means Emergency.
                 * Otherwise, calculate priority from patient details.
                 */
                Patient patient =
                        patientRepo.findById(
                                app.getPatientId()
                        ).orElse(null);

                String priority;

                Integer emergencyLevel =
                        app.getEmergencyLevel() != null
                                ? app.getEmergencyLevel()
                                : 0;

                if (emergencyLevel > 0) {

                    priority = "Emergency";

                } else {

                    priority =
                            queueTokenService
                                    .calculatePatientPriority(
                                            patient
                                    );
                }

                tokenDTO.setPriorityType(
                        priority
                );

                /*
                 * Emergency reason is optional here.
                 * It can be supplied later during emergency transfer
                 * or through the emergency workflow.
                 */
                tokenDTO.setEmergencyReason(
                        null
                );

                /*
                 * QueueTokenService is responsible for creating
                 * the token and validating the emergency level.
                 *
                 * Do not calculate queue position here using
                 * appointment date/time.
                 */
                queueTokenService.generateToken(
                        tokenDTO
                );
            }
        }

        // =====================================================
        // CANCELLED
        // =====================================================

        if ("Cancelled".equalsIgnoreCase(normalizedStatus)) {

            queueTokenRepo
                    .findByAppointmentId(
                            app.getAppointmentId()
                    )
                    .ifPresent(token -> {

                        token.setQueueStatus(
                                "Cancelled"
                        );

                        queueTokenRepo.save(token);
                    });
        }

        // =====================================================
        // COMPLETED
        // =====================================================

        if ("Completed".equalsIgnoreCase(normalizedStatus)) {

            queueTokenRepo
                    .findByAppointmentId(
                            app.getAppointmentId()
                    )
                    .ifPresent(token -> {

                        token.setQueueStatus(
                                "Completed"
                        );

                        queueTokenRepo.save(token);
                    });
        }
    }

    // =========================================================
    // TODAY'S APPOINTMENTS
    // =========================================================

    public List<AppointmentViewDTO> todayAppointments(
            Long doctorId
    ) {

        if (doctorId == null) {
            throw new RuntimeException(
                    "Doctor ID is required."
            );
        }

        Date today =
                Date.valueOf(LocalDate.now());

        return repo
                .findByAppointmentDateAndDoctorIdOrderByEmergencyLevelDescAppointmentTimeAscAppointmentIdAsc(
                        today,
                        doctorId
                )
                .stream()
                .map(this::toAppointmentView)
                .toList();
    }

    // =========================================================
    // ESTIMATED WAIT
    // =========================================================

    // =========================================================
    // ESTIMATED WAIT
    // =========================================================
    //
    // Estimated waiting time is now calculated by QueueTokenService
    // when the queue token is generated.
    //
    // QueueTokenService is responsible for queue priority and token
    // ordering, so AppointmentService should not calculate queue
    // position separately.

    // =========================================================
    // FIND SCHEDULE
    // =========================================================

    private DoctorSchedule findSchedule(
            Long doctorId,
            LocalDate date,
            LocalTime time
    ) {

        List<DoctorSchedule> schedules =
                doctorScheduleRepo
                        .findByDoctorIdAndAvailableDate(
                                doctorId,
                                date
                        );

        if (schedules.isEmpty()) {

            throw new RuntimeException(
                    "The selected date is not available for this doctor."
            );
        }

        return schedules
                .stream()
                .filter(schedule -> {

                    if (schedule.getStartTime() == null
                            || schedule.getEndTime() == null) {

                        return false;
                    }

                    return !time.isBefore(
                            schedule.getStartTime()
                    )
                            && !time.isAfter(
                            schedule.getEndTime()
                    );
                })
                .findFirst()
                .orElseThrow(
                        () -> new RuntimeException(
                                "The selected appointment time is not available for this doctor."
                        )
                );
    }

    // =========================================================
    // CAPACITY
    // =========================================================

    private void checkScheduleCapacity(
            Long doctorId,
            Date date,
            Integer maxPatients
    ) {

        if (maxPatients == null
                || maxPatients <= 0) {

            throw new RuntimeException(
                    "Maximum patient limit is not configured for this schedule."
            );
        }

        long current =
                repo.countByDoctorIdAndAppointmentDateAndStatusNot(
                        doctorId,
                        date,
                        "Cancelled"
                );

        if (current >= maxPatients) {

            throw new RuntimeException(
                    "This doctor's schedule is full. Maximum patients allowed: "
                            + maxPatients
            );
        }
    }

    private void checkScheduleCapacityForUpdate(
            Long doctorId,
            Date date,
            Integer maxPatients,
            Long appointmentId
    ) {

        if (maxPatients == null
                || maxPatients <= 0) {

            throw new RuntimeException(
                    "Maximum patient limit is not configured for this schedule."
            );
        }

        long current =
                repo.countByDoctorIdAndAppointmentDateAndStatusNotAndAppointmentIdNot(
                        doctorId,
                        date,
                        "Cancelled",
                        appointmentId
                );

        if (current >= maxPatients) {

            throw new RuntimeException(
                    "This doctor's schedule is full. Maximum patients allowed: "
                            + maxPatients
            );
        }
    }

    // =========================================================
    // DATE / TIME VALIDATION
    // =========================================================

    private void validateFutureDateTime(
            LocalDate date,
            LocalTime time
    ) {

        LocalDate today =
                LocalDate.now();

        LocalTime now =
                LocalTime.now();

        if (date.isBefore(today)) {

            throw new RuntimeException(
                    "Cannot select a past date."
            );
        }

        if (date.isEqual(today)
                && time.isBefore(now)) {

            throw new RuntimeException(
                    "Cannot select a time that has already passed."
            );
        }
    }

    // =========================================================
    // APPOINTMENT INPUT VALIDATION
    // =========================================================

    private void validateAppointmentInput(
            Appointment appointment
    ) {

        if (appointment == null) {
            throw new RuntimeException(
                    "Appointment data is required."
            );
        }

        if (appointment.getPatientId() == null) {
            throw new RuntimeException(
                    "Patient ID is required."
            );
        }

        if (appointment.getDoctorId() == null) {
            throw new RuntimeException(
                    "Doctor ID is required."
            );
        }

        if (appointment.getAppointmentDate() == null) {
            throw new RuntimeException(
                    "Appointment date is required."
            );
        }

        if (appointment.getAppointmentTime() == null) {
            throw new RuntimeException(
                    "Appointment time is required."
            );
        }

        validateEmergencyLevel(
                appointment.getEmergencyLevel()
        );
    }

    // =========================================================
    // STAFF DTO VALIDATION
    // =========================================================

    private void validateStaffDTO(
            StaffAppointmentDTO dto
    ) {

        if (dto == null) {
            throw new RuntimeException(
                    "Appointment data is required."
            );
        }

        if (dto.getPatientId() == null) {
            throw new RuntimeException(
                    "Patient ID is required."
            );
        }

        if (dto.getDoctorId() == null) {
            throw new RuntimeException(
                    "Doctor ID is required."
            );
        }

        if (dto.getAppointmentDate() == null
                || dto.getAppointmentDate().trim().isEmpty()) {

            throw new RuntimeException(
                    "Appointment date is required."
            );
        }

        if (dto.getAppointmentTime() == null
                || dto.getAppointmentTime().trim().isEmpty()) {

            throw new RuntimeException(
                    "Appointment time is required."
            );
        }
    }

    // =========================================================
    // STATUS NORMALIZATION
    // =========================================================

    private String normalizeAppointmentStatus(
            String status
    ) {

        if (status == null
                || status.trim().isEmpty()) {

            throw new RuntimeException(
                    "Appointment status cannot be empty."
            );
        }

        String value =
                status.trim();

        if ("Booked".equalsIgnoreCase(value)) {
            return "Booked";
        }

        if ("Confirmed".equalsIgnoreCase(value)) {
            return "Confirmed";
        }

        if ("Waiting".equalsIgnoreCase(value)) {
            return "Waiting";
        }

        if ("Serving".equalsIgnoreCase(value)) {
            return "Serving";
        }

        if ("Skipped".equalsIgnoreCase(value)) {
            return "Skipped";
        }

        if ("Completed".equalsIgnoreCase(value)) {
            return "Completed";
        }

        if ("Cancelled".equalsIgnoreCase(value)) {
            return "Cancelled";
        }

        throw new RuntimeException(
                "Invalid appointment status: "
                        + value
                        + ". Allowed values: Booked, Confirmed, Waiting, Serving, Skipped, Completed, Cancelled."
        );
    }

    // =========================================================
    // DTO MAPPING
    // =========================================================

    // =========================================================
// DTO MAPPING
// =========================================================

    private AppointmentViewDTO toAppointmentView(
            Appointment appointment
    ) {

        AppointmentViewDTO dto =
                new AppointmentViewDTO();

        dto.setAppointmentId(
                appointment.getAppointmentId()
        );

        dto.setPatientId(
                appointment.getPatientId()
        );

        dto.setDoctorId(
                appointment.getDoctorId()
        );

        // =====================================================
        // PATIENT
        // =====================================================

        Patient patient =
                patientRepo.findById(
                        appointment.getPatientId()
                ).orElse(null);

        // =====================================================
        // DOCTOR
        // =====================================================

        Doctor doctor =
                doctorRepo.findById(
                        appointment.getDoctorId()
                ).orElse(null);

        // =====================================================
        // PATIENT DETAILS
        // =====================================================

        dto.setPatientName(
                patient != null
                        ? patient.getFullName()
                        : "-"
        );

        dto.setAge(
                patient != null
                        ? queueTokenService.calculatePatientAge(
                        patient
                )
                        : 0
        );

        dto.setHasSpecialNeeds(
                patient != null
                        && Boolean.TRUE.equals(
                        patient.getSpecialNeeds()
                )
        );

        // =====================================================
        // PRIORITY
        // =====================================================

        Integer emergencyLevel =
                appointment.getEmergencyLevel() != null
                        ? appointment.getEmergencyLevel()
                        : 0;

        String calculatedPriority;

        if (emergencyLevel > 0) {

            calculatedPriority = "Emergency";

        } else {

            calculatedPriority =
                    patient != null
                            ? queueTokenService.calculatePatientPriority(
                            patient
                    )
                            : "Normal";
        }

        dto.setCalculatedPriority(
                calculatedPriority
        );

        // =====================================================
        // DOCTOR DETAILS
        // =====================================================

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

        // =====================================================
        // APPOINTMENT DETAILS
        // =====================================================

        dto.setAppointmentDate(
                appointment.getAppointmentDate() != null
                        ? appointment.getAppointmentDate().toString()
                        : "-"
        );

        dto.setAppointmentTime(
                appointment.getAppointmentTime() != null
                        ? appointment.getAppointmentTime().toString()
                        : "-"
        );

        dto.setStatus(
                appointment.getStatus()
        );

        // =====================================================
        // EMERGENCY LEVEL
        // =====================================================

        dto.setEmergencyLevel(
                emergencyLevel
        );

        // =====================================================
        // QUEUE TOKEN
        // =====================================================

        QueueToken token =
                queueTokenRepo
                        .findByAppointmentId(
                                appointment.getAppointmentId()
                        )
                        .orElse(null);

        if (token != null) {

            dto.setQueueToken(
                    token.getTokenNumber()
            );

            dto.setEstimatedWaitTime(
                    token.getEstimatedWaitTime()
            );

        } else {

            dto.setQueueToken("-");

            dto.setEstimatedWaitTime(null);
        }

        return dto;
    }

    private void validateEmergencyLevel(
            Integer emergencyLevel
    ) {

        if (emergencyLevel == null) {
            return;
        }

        if (emergencyLevel < 0
                || emergencyLevel > 3) {

            throw new RuntimeException(
                    "Emergency level must be 0, 1, 2, or 3."
            );
        }
    }
}