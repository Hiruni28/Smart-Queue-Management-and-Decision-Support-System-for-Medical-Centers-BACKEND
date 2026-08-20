package com.hospital.queue_management_system.service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hospital.queue_management_system.dto.EmergencyTransferDTO;
import com.hospital.queue_management_system.dto.QueueTokenDTO;
import com.hospital.queue_management_system.model.Appointment;
import com.hospital.queue_management_system.model.Doctor;
import com.hospital.queue_management_system.model.DoctorStatus;
import com.hospital.queue_management_system.model.Patient;
import com.hospital.queue_management_system.model.QueueRule;
import com.hospital.queue_management_system.model.QueueToken;
import com.hospital.queue_management_system.repository.AppointmentRepository;
import com.hospital.queue_management_system.repository.DoctorRepository;
import com.hospital.queue_management_system.repository.DoctorStatusRepository;
import com.hospital.queue_management_system.repository.PatientRepository;
import com.hospital.queue_management_system.repository.QueueRuleRepository;
import com.hospital.queue_management_system.repository.QueueTokenRepository;

@Service
public class StaffQueueService {

    private final QueueTokenRepository queueRepo;
    private final AppointmentRepository appointmentRepo;
    private final PatientRepository patientRepo;
    private final DoctorRepository doctorRepo;
    private final DoctorStatusRepository doctorStatusRepo;
    private final QueueRuleRepository queueRuleRepo;

    public StaffQueueService(
            QueueTokenRepository queueRepo,
            AppointmentRepository appointmentRepo,
            PatientRepository patientRepo,
            DoctorRepository doctorRepo,
            DoctorStatusRepository doctorStatusRepo,
            QueueRuleRepository queueRuleRepo
    ) {
        this.queueRepo = queueRepo;
        this.appointmentRepo = appointmentRepo;
        this.patientRepo = patientRepo;
        this.doctorRepo = doctorRepo;
        this.doctorStatusRepo = doctorStatusRepo;
        this.queueRuleRepo = queueRuleRepo;
    }

    // =========================================================
    // GET TODAY'S QUEUE
    // =========================================================

    public List<QueueTokenDTO> getQueue(Long doctorId) {

        LocalDate today = LocalDate.now();

        return queueRepo
                .findAll()
                .stream()

                // -------------------------------------------------
                // FILTER BY DOCTOR
                // -------------------------------------------------
                .filter(queue ->
                        doctorId == null
                                ||
                                (
                                        queue.getDoctorId() != null
                                                &&
                                                queue.getDoctorId().equals(doctorId)
                                )
                )

                // -------------------------------------------------
                // ONLY TODAY'S APPOINTMENTS
                // -------------------------------------------------
                .filter(queue ->
                        isAppointmentToday(
                                queue,
                                today
                        )
                )

                // -------------------------------------------------
                // SORT BY REAL QUEUE PRIORITY
                // -------------------------------------------------
                .sorted(
                        this::compareQueuePriority
                )

                .map(this::convert)

                .collect(Collectors.toList());
    }

    // =========================================================
    // CALL NEXT PATIENT
    // =========================================================

    @Transactional
    public QueueToken callNextPatient(
            Long doctorId
    ) {

        if (doctorId == null) {
            throw new RuntimeException(
                    "Doctor ID is required."
            );
        }

        LocalDate today =
                LocalDate.now();

        // =====================================================
        // GET CURRENT DOCTOR STATUS
        // =====================================================

        DoctorStatus doctorStatus =
                doctorStatusRepo
                        .findTopByDoctorIdOrderByUpdatedAtDesc(
                                doctorId
                        )
                        .orElse(null);

        if (doctorStatus == null) {

            throw new RuntimeException(
                    "Doctor status has not been updated. " +
                            "Staff must mark the doctor as Arrived first."
            );
        }

        // =====================================================
        // IMPORTANT:
        // OLD STATUS FROM YESTERDAY MUST NOT BE USED
        // =====================================================

        if (doctorStatus.getUpdatedAt() == null
                || !doctorStatus
                .getUpdatedAt()
                .toLocalDate()
                .equals(today)) {

            throw new RuntimeException(
                    "The doctor's arrival status has not been updated for today. " +
                            "Please mark the doctor as Arrived today."
            );
        }

        String status =
                doctorStatus.getArrivalStatus();

        if ("Delayed".equalsIgnoreCase(status)) {

            throw new RuntimeException(
                    "Doctor is delayed. Please wait until the doctor arrives."
            );
        }

        if (!"Arrived".equalsIgnoreCase(status)) {

            throw new RuntimeException(
                    "Doctor is not available for queue service."
            );
        }

        // =====================================================
        // GET DOCTOR QUEUE
        // =====================================================

        List<QueueToken> doctorTokens =
                queueRepo
                        .findByDoctorIdOrderByQueueIdAsc(
                                doctorId
                        );

        // =====================================================
        // ONLY ONE SERVING PATIENT
        //
        // IMPORTANT FIX:
        //
        // A Serving token from an OLD APPOINTMENT must NOT
        // block today's queue.
        // =====================================================

        boolean alreadyServing =
                doctorTokens
                        .stream()
                        .filter(token ->
                                "Serving".equalsIgnoreCase(
                                        token.getQueueStatus()
                                )
                        )
                        .anyMatch(token ->
                                isAppointmentToday(
                                        token,
                                        today
                                )
                        );

        if (alreadyServing) {

            throw new RuntimeException(
                    "The doctor is currently serving another patient. " +
                            "Complete that patient first."
            );
        }

        // =====================================================
        // FIND NEXT PATIENT
        //
        // ONLY:
        // 1. Waiting
        // 2. Today's appointment
        //
        // Priority:
        // Emergency
        // Emergency level
        // QueueRule priority
        // Queue ID
        // =====================================================

        QueueToken token =
                doctorTokens
                        .stream()

                        .filter(queue ->
                                "Waiting".equalsIgnoreCase(
                                        queue.getQueueStatus()
                                )
                        )

                        .filter(queue ->
                                isAppointmentToday(
                                        queue,
                                        today
                                )
                        )

                        .sorted(
                                this::compareQueuePriority
                        )

                        .findFirst()

                        .orElseThrow(() ->
                                new RuntimeException(
                                        "No waiting patients for this doctor today."
                                )
                        );

        // =====================================================
        // MARK AS SERVING
        // =====================================================

        token.setQueueStatus(
                "Serving"
        );

        return queueRepo.save(token);
    }

    // =========================================================
    // CHECK WHETHER QUEUE TOKEN BELONGS TO TODAY
    // =========================================================

    private boolean isAppointmentToday(
            QueueToken token,
            LocalDate today
    ) {

        if (token == null
                || token.getAppointmentId() == null
                || today == null) {

            return false;
        }

        Appointment appointment =
                appointmentRepo
                        .findById(
                                token.getAppointmentId()
                        )
                        .orElse(null);

        if (appointment == null
                || appointment.getAppointmentDate() == null) {

            return false;
        }

        return appointment
                .getAppointmentDate()
                .toLocalDate()
                .equals(today);
    }

    // =========================================================
    // QUEUE PRIORITY COMPARATOR
    // =========================================================

    private int compareQueuePriority(
            QueueToken first,
            QueueToken second
    ) {

        if (first == null && second == null) {
            return 0;
        }

        if (first == null) {
            return 1;
        }

        if (second == null) {
            return -1;
        }

        // -----------------------------------------------------
        // 1. PRIORITY RULE
        // -----------------------------------------------------

        int priorityComparison =
                Integer.compare(
                        getPriorityOrder(first),
                        getPriorityOrder(second)
                );

        if (priorityComparison != 0) {

            return priorityComparison;
        }

        // -----------------------------------------------------
        // 2. EMERGENCY LEVEL
        //
        // Emergency 1 -> 2 -> 3
        // -----------------------------------------------------

        int emergencyComparison =
                Integer.compare(
                        getEmergencyLevelOrder(first),
                        getEmergencyLevelOrder(second)
                );

        if (emergencyComparison != 0) {

            return emergencyComparison;
        }

        // -----------------------------------------------------
        // 3. CREATED TIME
        //
        // Older token first.
        // -----------------------------------------------------

        if (first.getCreatedAt() != null
                && second.getCreatedAt() != null) {

            int createdComparison =
                    first.getCreatedAt()
                            .compareTo(
                                    second.getCreatedAt()
                            );

            if (createdComparison != 0) {

                return createdComparison;
            }
        }

        // -----------------------------------------------------
        // 4. QUEUE ID
        // -----------------------------------------------------

        if (first.getQueueId() != null
                && second.getQueueId() != null) {

            return Long.compare(
                    first.getQueueId(),
                    second.getQueueId()
            );
        }

        if (first.getQueueId() == null
                && second.getQueueId() != null) {

            return 1;
        }

        if (first.getQueueId() != null
                && second.getQueueId() == null) {

            return -1;
        }

        return 0;
    }

    // =========================================================
    // PRIORITY ORDER
    // =========================================================

    private int getPriorityOrder(
            QueueToken token
    ) {

        if (token == null
                || token.getPriorityType() == null) {

            return Integer.MAX_VALUE;
        }

        String priority =
                token.getPriorityType()
                        .trim();

        // -----------------------------------------------------
        // EMERGENCY ALWAYS FIRST
        // -----------------------------------------------------

        if ("Emergency".equalsIgnoreCase(priority)) {

            return 0;
        }

        // -----------------------------------------------------
        // USE QUEUE RULE TABLE
        // -----------------------------------------------------

        return queueRuleRepo
                .findByPriorityTypeIgnoreCase(
                        priority
                )
                .filter(rule ->
                        Boolean.TRUE.equals(
                                rule.getIsActive()
                        )
                )
                .map(QueueRule::getPriorityOrder)
                .orElse(
                        Integer.MAX_VALUE
                );
    }

    // =========================================================
    // EMERGENCY LEVEL ORDER
    // =========================================================

    private int getEmergencyLevelOrder(
            QueueToken token
    ) {

        if (token == null) {

            return Integer.MAX_VALUE;
        }

        if (!"Emergency".equalsIgnoreCase(
                token.getPriorityType()
        )) {

            return Integer.MAX_VALUE;
        }

        Integer level =
                token.getEmergencyLevel();

        if (level == null
                || level < 1) {

            return Integer.MAX_VALUE;
        }

        return level;
    }

    // =========================================================
    // SKIP PATIENT
    // =========================================================

    @Transactional
    public QueueToken skipPatient(
            Long id
    ) {

        QueueToken token =
                queueRepo
                        .findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Queue token not found."
                                )
                        );

        if ("Completed".equalsIgnoreCase(
                token.getQueueStatus()
        )) {

            throw new RuntimeException(
                    "Completed patient cannot be skipped."
            );
        }

        if ("Cancelled".equalsIgnoreCase(
                token.getQueueStatus()
        )) {

            throw new RuntimeException(
                    "Cancelled patient cannot be skipped."
            );
        }

        token.setQueueStatus(
                "Skipped"
        );

        return queueRepo.save(token);
    }

    // =========================================================
    // COMPLETE PATIENT
    // =========================================================

    @Transactional
    public QueueToken completePatient(
            Long id
    ) {

        QueueToken token =
                queueRepo
                        .findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Queue token not found."
                                )
                        );

        if (!"Serving".equalsIgnoreCase(
                token.getQueueStatus()
        )) {

            throw new RuntimeException(
                    "Only a serving patient can be completed."
            );
        }

        token.setQueueStatus(
                "Completed"
        );

        if (token.getAppointmentId() != null) {

            Appointment appointment =
                    appointmentRepo
                            .findById(
                                    token.getAppointmentId()
                            )
                            .orElse(null);

            if (appointment != null) {

                appointment.setStatus(
                        "Completed"
                );

                appointmentRepo.save(
                        appointment
                );
            }
        }

        return queueRepo.save(token);
    }

    // =========================================================
    // UPDATE QUEUE STATUS
    // =========================================================

    @Transactional
    public QueueToken updateStatus(
            Long id,
            String status
    ) {

        QueueToken token =
                queueRepo
                        .findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Queue token not found."
                                )
                        );

        if (status == null
                || status.trim().isEmpty()) {

            throw new RuntimeException(
                    "Queue status cannot be empty."
            );
        }

        String normalizedStatus =
                status.trim();

        if (
                !normalizedStatus.equalsIgnoreCase("Waiting")
                        &&
                        !normalizedStatus.equalsIgnoreCase("Serving")
                        &&
                        !normalizedStatus.equalsIgnoreCase("Skipped")
                        &&
                        !normalizedStatus.equalsIgnoreCase("Completed")
                        &&
                        !normalizedStatus.equalsIgnoreCase("Cancelled")
        ) {

            throw new RuntimeException(
                    "Invalid queue status. " +
                            "Allowed values: Waiting, Serving, Skipped, Completed, Cancelled."
            );
        }

        if ("Completed".equalsIgnoreCase(
                normalizedStatus
        )
                &&
                !"Serving".equalsIgnoreCase(
                        token.getQueueStatus()
                )) {

            throw new RuntimeException(
                    "Only a serving patient can be completed."
            );
        }

        token.setQueueStatus(
                normalizeQueueStatus(
                        normalizedStatus
                )
        );

        if ("Completed".equalsIgnoreCase(
                normalizedStatus
        )) {

            updateAppointmentStatus(
                    token,
                    "Completed"
            );
        }

        if ("Cancelled".equalsIgnoreCase(
                normalizedStatus
        )) {

            updateAppointmentStatus(
                    token,
                    "Cancelled"
            );
        }

        return queueRepo.save(token);
    }

    // =========================================================
    // NORMALIZE QUEUE STATUS
    // =========================================================

    private String normalizeQueueStatus(
            String status
    ) {

        if ("Waiting".equalsIgnoreCase(status)) {
            return "Waiting";
        }

        if ("Serving".equalsIgnoreCase(status)) {
            return "Serving";
        }

        if ("Skipped".equalsIgnoreCase(status)) {
            return "Skipped";
        }

        if ("Completed".equalsIgnoreCase(status)) {
            return "Completed";
        }

        if ("Cancelled".equalsIgnoreCase(status)) {
            return "Cancelled";
        }

        return status;
    }

    // =========================================================
    // UPDATE APPOINTMENT STATUS
    // =========================================================

    private void updateAppointmentStatus(
            QueueToken token,
            String queueStatus
    ) {

        if (token == null
                || token.getAppointmentId() == null) {

            return;
        }

        Appointment appointment =
                appointmentRepo
                        .findById(
                                token.getAppointmentId()
                        )
                        .orElse(null);

        if (appointment == null) {
            return;
        }

        if ("Completed".equalsIgnoreCase(
                queueStatus
        )) {

            appointment.setStatus(
                    "Completed"
            );

            appointmentRepo.save(
                    appointment
            );

        } else if ("Cancelled".equalsIgnoreCase(
                queueStatus
        )) {

            appointment.setStatus(
                    "Cancelled"
            );

            appointmentRepo.save(
                    appointment
            );
        }
    }

    // =========================================================
    // EMERGENCY TRANSFER
    // =========================================================

    @Transactional
    public QueueTokenDTO transferEmergency(
            EmergencyTransferDTO dto
    ) {

        if (dto == null) {

            throw new RuntimeException(
                    "Emergency transfer data is required."
            );
        }

        if (dto.getQueueId() == null) {

            throw new RuntimeException(
                    "Queue ID is required."
            );
        }

        if (dto.getNewDoctorId() == null) {

            throw new RuntimeException(
                    "New doctor ID is required."
            );
        }

        if (dto.getReason() == null
                || dto.getReason().trim().isEmpty()) {

            throw new RuntimeException(
                    "Emergency transfer reason is required."
            );
        }

        QueueToken token =
                queueRepo
                        .findById(
                                dto.getQueueId()
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Queue token not found."
                                )
                        );

        if ("Completed".equalsIgnoreCase(
                token.getQueueStatus()
        )) {

            throw new RuntimeException(
                    "Completed patient cannot be transferred."
            );
        }

        if ("Cancelled".equalsIgnoreCase(
                token.getQueueStatus()
        )) {

            throw new RuntimeException(
                    "Cancelled patient cannot be transferred."
            );
        }

        if (token.getAppointmentId() == null) {

            throw new RuntimeException(
                    "Queue token has no appointment."
            );
        }

        Appointment appointment =
                appointmentRepo
                        .findById(
                                token.getAppointmentId()
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Appointment not found."
                                )
                        );

        LocalDate today =
                LocalDate.now();

        if (appointment.getAppointmentDate() == null
                ||
                !appointment
                        .getAppointmentDate()
                        .toLocalDate()
                        .equals(today)) {

            throw new RuntimeException(
                    "Emergency transfer is only available for today's patients."
            );
        }

        Long oldDoctorId =
                token.getDoctorId();

        Long newDoctorId =
                dto.getNewDoctorId();

        if (oldDoctorId != null
                && oldDoctorId.equals(newDoctorId)) {

            throw new RuntimeException(
                    "Patient is already assigned to this doctor."
            );
        }

        // =====================================================
        // CURRENT DOCTOR
        // =====================================================

        Doctor oldDoctor =
                oldDoctorId == null
                        ? null
                        : doctorRepo
                        .findById(oldDoctorId)
                        .orElse(null);

        // =====================================================
        // NEW DOCTOR
        // =====================================================

        Doctor newDoctor =
                doctorRepo
                        .findById(
                                newDoctorId
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "New doctor not found."
                                )
                        );

        // =====================================================
        // NEW DOCTOR MUST BE ARRIVED
        // =====================================================

        DoctorStatus newDoctorStatus =
                doctorStatusRepo
                        .findTopByDoctorIdOrderByUpdatedAtDesc(
                                newDoctorId
                        )
                        .orElse(null);

        if (newDoctorStatus == null
                ||
                newDoctorStatus.getUpdatedAt() == null
                ||
                !newDoctorStatus
                        .getUpdatedAt()
                        .toLocalDate()
                        .equals(today)
                ||
                !"Arrived".equalsIgnoreCase(
                        newDoctorStatus.getArrivalStatus()
                )) {

            throw new RuntimeException(
                    "The selected doctor is not available. " +
                            "The replacement doctor must be marked Arrived today."
            );
        }

        // =====================================================
        // SAME SPECIALIZATION
        // =====================================================

        if (oldDoctor != null) {

            String oldSpecialization =
                    oldDoctor.getSpecialization();

            String newSpecialization =
                    newDoctor.getSpecialization();

            if (oldSpecialization == null
                    || oldSpecialization.trim().isEmpty()) {

                throw new RuntimeException(
                        "Current doctor's specialization is not available."
                );
            }

            if (newSpecialization == null
                    || newSpecialization.trim().isEmpty()) {

                throw new RuntimeException(
                        "Replacement doctor's specialization is not available."
                );
            }

            if (!oldSpecialization
                    .trim()
                    .equalsIgnoreCase(
                            newSpecialization.trim()
                    )) {

                throw new RuntimeException(
                        "Emergency patient must be transferred to a doctor with the same specialization. " +
                                "Required: "
                                + oldSpecialization
                                + ", Selected: "
                                + newSpecialization
                );
            }
        }

        // =====================================================
        // UPDATE APPOINTMENT
        // =====================================================

        appointment.setDoctorId(
                newDoctorId
        );

        appointmentRepo.save(
                appointment
        );

        // =====================================================
        // UPDATE QUEUE TOKEN
        // =====================================================

        token.setDoctorId(
                newDoctorId
        );

        token.setTransferredToDoctorId(
                newDoctorId
        );

        token.setEmergencyReason(
                dto.getReason().trim()
        );

        token.setPriorityType(
                "Emergency"
        );

        token.setQueueStatus(
                "Waiting"
        );

        if (token.getEmergencyLevel() == null
                || token.getEmergencyLevel() < 1) {

            token.setEmergencyLevel(
                    1
            );
        }

        if (token.getEstimatedWaitTime() == null) {

            token.setEstimatedWaitTime(
                    0
            );
        }

        QueueToken saved =
                queueRepo.save(
                        token
                );

        return convert(saved);
    }

    // =========================================================
    // ENTITY -> DTO
    // =========================================================

    private QueueTokenDTO convert(
            QueueToken queue
    ) {

        Appointment appointment = null;

        if (queue.getAppointmentId() != null) {

            appointment =
                    appointmentRepo
                            .findById(
                                    queue.getAppointmentId()
                            )
                            .orElse(null);
        }

        String patientName = "-";
        String doctorName = "-";

        // =====================================================
        // PATIENT
        // =====================================================

        if (appointment != null
                && appointment.getPatientId() != null) {

            Patient patient =
                    patientRepo
                            .findById(
                                    appointment.getPatientId()
                            )
                            .orElse(null);

            if (patient != null) {

                patientName =
                        patient.getFullName();
            }
        }

        // =====================================================
        // DOCTOR
        // =====================================================

        Long actualDoctorId =
                queue.getDoctorId();

        if (actualDoctorId != null) {

            Doctor doctor =
                    doctorRepo
                            .findById(
                                    actualDoctorId
                            )
                            .orElse(null);

            if (doctor != null) {

                doctorName =
                        doctor.getDoctorName();
            }
        }

        // =====================================================
        // DOCTOR STATUS
        // =====================================================

        String doctorStatusValue =
                "Unavailable";

        Integer delayMinutes =
                0;

        if (actualDoctorId != null) {

            DoctorStatus doctorStatus =
                    doctorStatusRepo
                            .findTopByDoctorIdOrderByUpdatedAtDesc(
                                    actualDoctorId
                            )
                            .orElse(null);

            if (doctorStatus != null) {

                String arrivalStatus =
                        doctorStatus.getArrivalStatus();

                if (arrivalStatus != null
                        && !arrivalStatus.trim().isEmpty()) {

                    doctorStatusValue =
                            normalizeDoctorStatus(
                                    arrivalStatus
                            );
                }

                if (
                        "Delayed".equalsIgnoreCase(
                                doctorStatusValue
                        )
                                &&
                                doctorStatus.getUpdatedAt() != null
                ) {

                    long minutes =
                            Duration.between(
                                    doctorStatus.getUpdatedAt(),
                                    LocalDateTime.now()
                            ).toMinutes();

                    delayMinutes =
                            (int) Math.max(
                                    minutes,
                                    0
                            );
                }
            }
        }

        // =====================================================
        // CREATE DTO
        // =====================================================

        QueueTokenDTO dto =
                new QueueTokenDTO();

        dto.setQueueId(
                queue.getQueueId()
        );

        dto.setTokenNumber(
                queue.getTokenNumber()
        );

        dto.setAppointmentId(
                queue.getAppointmentId()
        );

        dto.setDoctorId(
                queue.getDoctorId()
        );

        dto.setPatientName(
                patientName
        );

        dto.setDoctorName(
                doctorName
        );

        dto.setQueueStatus(
                queue.getQueueStatus()
        );

        dto.setPriorityType(
                queue.getPriorityType()
        );

        dto.setEmergencyLevel(
                queue.getEmergencyLevel()
        );

        dto.setEmergencyReason(
                queue.getEmergencyReason()
        );

        dto.setTransferredToDoctorId(
                queue.getTransferredToDoctorId()
        );

        dto.setEstimatedWaitTime(
                queue.getEstimatedWaitTime()
        );

        dto.setDoctorStatus(
                doctorStatusValue
        );

        dto.setDelayMinutes(
                delayMinutes
        );

        return dto;
    }

    // =========================================================
    // NORMALIZE DOCTOR STATUS
    // =========================================================

    private String normalizeDoctorStatus(
            String status
    ) {

        String value =
                status.trim();

        if ("arrived".equalsIgnoreCase(value)) {
            return "Arrived";
        }

        if ("delayed".equalsIgnoreCase(value)) {
            return "Delayed";
        }

        if ("not arrived".equalsIgnoreCase(value)) {
            return "Not Arrived";
        }

        if ("unavailable".equalsIgnoreCase(value)) {
            return "Unavailable";
        }

        return value;
    }
}