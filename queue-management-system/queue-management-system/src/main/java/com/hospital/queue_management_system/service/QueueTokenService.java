package com.hospital.queue_management_system.service;

import com.hospital.queue_management_system.dto.EmergencyTransferDTO;
import com.hospital.queue_management_system.dto.QueueTokenDTO;
import com.hospital.queue_management_system.model.Appointment;
import com.hospital.queue_management_system.model.Doctor;
import com.hospital.queue_management_system.model.DoctorSchedule;
import com.hospital.queue_management_system.model.DoctorStatus;
import com.hospital.queue_management_system.model.QueueToken;
import com.hospital.queue_management_system.model.QueueRule;
import com.hospital.queue_management_system.model.Patient;

import com.hospital.queue_management_system.repository.AppointmentRepository;
import com.hospital.queue_management_system.repository.DoctorRepository;
import com.hospital.queue_management_system.repository.DoctorScheduleRepository;
import com.hospital.queue_management_system.repository.DoctorStatusRepository;
import com.hospital.queue_management_system.repository.QueueTokenRepository;
import com.hospital.queue_management_system.repository.QueueRuleRepository;

import org.springframework.stereotype.Service;
import com.hospital.queue_management_system.service.NotificationService;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;

@Service
public class QueueTokenService {

    private final QueueTokenRepository repo;
    private final AppointmentRepository appointmentRepo;
    private final DoctorRepository doctorRepo;
    private final DoctorStatusRepository doctorStatusRepo;
    private final DoctorScheduleRepository doctorScheduleRepo;
    private final QueueRuleRepository queueRuleRepo;
    private final NotificationService notificationService;

    public QueueTokenService(
            QueueTokenRepository repo,
            AppointmentRepository appointmentRepo,
            DoctorRepository doctorRepo,
            DoctorStatusRepository doctorStatusRepo,
            DoctorScheduleRepository doctorScheduleRepo,
            QueueRuleRepository queueRuleRepo,
            NotificationService notificationService
    ) {
        this.repo = repo;
        this.appointmentRepo = appointmentRepo;
        this.doctorRepo = doctorRepo;
        this.doctorStatusRepo = doctorStatusRepo;
        this.doctorScheduleRepo = doctorScheduleRepo;
        this.queueRuleRepo = queueRuleRepo;
        this.notificationService = notificationService;
    }

    // =========================================================
    // GENERATE TOKEN WITHOUT EXTERNAL WAIT TIME
    // =========================================================

    @Transactional
    public QueueToken generateToken(
            QueueTokenDTO dto
    ) {

        validateDTO(dto);

        Appointment appointment =
                validateAppointment(dto);

        preventDuplicateToken(
                dto.getAppointmentId()
        );

        String tokenNumber =
                generateNextTokenNumber(
                        appointment
                );

        QueueToken token =
                createToken(
                        dto,
                        tokenNumber,
                        appointment
                );

        int estimatedWaitingTime =
                calculateInitialEstimatedWait(
                        token
                );

        token.setEstimatedWaitTime(
                Math.max(
                        estimatedWaitingTime,
                        0
                )
        );

        QueueToken savedToken = repo.save(token);

        // =========================================================
// NOTIFICATION
// =========================================================
        notificationService.notifyTokenCreated(
                appointment.getPatientId(),
                appointment.getAppointmentId(),
                savedToken.getQueueId(),
                savedToken.getDoctorId(),
                savedToken.getTokenNumber(),
                savedToken.getEstimatedWaitTime()
        );

        return savedToken;
    }

    // =========================================================
// INITIAL ESTIMATED WAIT
// =========================================================

    private int calculateInitialEstimatedWait(
            QueueToken newToken
    ) {

        if (newToken == null
                || newToken.getDoctorId() == null) {

            return 0;
        }

        LocalDate today =
                LocalDate.now();

        List<QueueToken> waitingTokens =
                repo.findByDoctorIdOrderByQueueIdAsc(
                                newToken.getDoctorId()
                        )
                        .stream()
                        .filter(this::isWaiting)
                        .filter(token ->
                                isAppointmentToday(
                                        token,
                                        today
                                )
                        )
                        .filter(token ->
                                token != newToken
                        )
                        .toList();

        int position = 0;

        for (QueueToken token : waitingTokens) {

            /*
             * If the existing token has a higher or equal queue
             * priority, it is ahead of the new token.
             */
            if (compareQueuePriority(
                    token,
                    newToken
            ) <= 0) {

                position++;
            }
        }

        return position * 15;
    }

    // =========================================================
// COMPARE QUEUE PRIORITY
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

        // =====================================================
        // 1. DATABASE QUEUE RULE
        // =====================================================

        int firstPriority =
                getPriorityOrder(first);

        int secondPriority =
                getPriorityOrder(second);

        if (firstPriority != secondPriority) {

            return Integer.compare(
                    firstPriority,
                    secondPriority
            );
        }

        // =====================================================
        // 2. EMERGENCY LEVEL
        // =====================================================

        if (isEmergency(first)
                && isEmergency(second)) {

            int firstEmergency =
                    getEmergencyLevelOrder(first);

            int secondEmergency =
                    getEmergencyLevelOrder(second);

            if (firstEmergency != secondEmergency) {

                return Integer.compare(
                        firstEmergency,
                        secondEmergency
                );
            }
        }

        // =====================================================
        // 3. CREATED TIME
        // =====================================================

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

        // =====================================================
        // 4. QUEUE ID
        // =====================================================

        if (first.getQueueId() != null
                && second.getQueueId() != null) {

            return Long.compare(
                    first.getQueueId(),
                    second.getQueueId()
            );
        }

        if (first.getQueueId() != null) {
            return -1;
        }

        if (second.getQueueId() != null) {
            return 1;
        }

        return 0;
    }

    // =========================================================
// IS EMERGENCY
// =========================================================

    private boolean isEmergency(
            QueueToken token
    ) {

        return token != null
                && "Emergency".equalsIgnoreCase(
                token.getPriorityType()
        );
    }

    // =========================================================
    // CREATE TOKEN
    // =========================================================

    // =========================================================
// CREATE TOKEN
// =========================================================

    private QueueToken createToken(
            QueueTokenDTO dto,
            String tokenNumber,
            Appointment appointment
    ) {

        QueueToken token =
                new QueueToken();

        token.setAppointmentId(
                dto.getAppointmentId()
        );

        token.setDoctorId(
                dto.getDoctorId()
        );

        token.setTokenNumber(
                tokenNumber
        );

        token.setQueueStatus(
                "Waiting"
        );

        Integer emergencyLevel =
                appointment.getEmergencyLevel() != null
                        ? appointment.getEmergencyLevel()
                        : 0;

        String priority =
                dto.getPriorityType();

        // =====================================================
        // EMERGENCY
        // =====================================================

        if (emergencyLevel > 0) {

            priority = "Emergency";

        } else if (priority == null
                || priority.trim().isEmpty()) {

            priority = "Normal";

        } else {

            priority =
                    normalizePriority(priority);
        }

        validateEmergencyLevel(
                priority,
                emergencyLevel
        );

        token.setPriorityType(
                priority
        );

        token.setEmergencyLevel(
                emergencyLevel
        );

        token.setEmergencyReason(
                dto.getEmergencyReason()
        );

        token.setCreatedAt(
                new Timestamp(
                        System.currentTimeMillis()
                )
        );

        return token;
    }

    // =========================================================
    // VALIDATE DTO
    // =========================================================

    private void validateDTO(
            QueueTokenDTO dto
    ) {

        if (dto == null) {
            throw new RuntimeException(
                    "Queue token data is required."
            );
        }

        if (dto.getAppointmentId() == null) {
            throw new RuntimeException(
                    "Appointment ID is required."
            );
        }

        if (dto.getDoctorId() == null) {
            throw new RuntimeException(
                    "Doctor ID is required."
            );
        }
    }

    // =========================================================
    // VALIDATE APPOINTMENT
    // =========================================================

    private Appointment validateAppointment(
            QueueTokenDTO dto
    ) {

        Appointment appointment =
                appointmentRepo.findById(
                        dto.getAppointmentId()
                ).orElseThrow(
                        () -> new RuntimeException(
                                "Appointment not found."
                        )
                );

        if (appointment.getDoctorId() == null) {
            throw new RuntimeException(
                    "Appointment has no assigned doctor."
            );
        }

        if (!appointment.getDoctorId()
                .equals(dto.getDoctorId())) {

            throw new RuntimeException(
                    "The selected doctor does not match the appointment doctor."
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

        return appointment;
    }

    // =========================================================
    // DUPLICATE TOKEN
    // =========================================================

    private void preventDuplicateToken(
            Long appointmentId
    ) {

        if (repo.findByAppointmentId(
                appointmentId
        ).isPresent()) {

            throw new RuntimeException(
                    "A queue token already exists for this appointment."
            );
        }
    }

    // =========================================================
    // NEXT TOKEN NUMBER
    // =========================================================

    private String generateNextTokenNumber(
            Appointment appointment
    ) {

        Long doctorId =
                appointment.getDoctorId();

        LocalDate date =
                appointment
                        .getAppointmentDate()
                        .toLocalDate();

        List<QueueToken> doctorTokens =
                repo.findByDoctorIdOrderByQueueIdAsc(
                        doctorId
                );

        int maxTokenNumber = 0;

        for (QueueToken token : doctorTokens) {

            if (token.getTokenNumber() == null
                    || token.getAppointmentId() == null) {
                continue;
            }

            Appointment tokenAppointment =
                    appointmentRepo.findById(
                            token.getAppointmentId()
                    ).orElse(null);

            if (tokenAppointment == null
                    || tokenAppointment.getAppointmentDate() == null) {
                continue;
            }

            if (!doctorId.equals(
                    tokenAppointment.getDoctorId()
            )) {
                continue;
            }

            if (!date.equals(
                    tokenAppointment
                            .getAppointmentDate()
                            .toLocalDate()
            )) {
                continue;
            }

            if ("Cancelled".equalsIgnoreCase(
                    token.getQueueStatus()
            )) {
                continue;
            }

            int number =
                    extractTokenNumber(
                            token.getTokenNumber()
                    );

            maxTokenNumber =
                    Math.max(
                            maxTokenNumber,
                            number
                    );
        }

        return "Q-" + (maxTokenNumber + 1);
    }

    // =========================================================
    // EXTRACT TOKEN NUMBER
    // =========================================================

    private int extractTokenNumber(
            String token
    ) {

        if (token == null
                || !token.startsWith("Q-")) {

            return 0;
        }

        try {

            return Integer.parseInt(
                    token.substring(2)
            );

        } catch (NumberFormatException e) {

            return 0;
        }
    }

    // =========================================================
// CALCULATE PATIENT PRIORITY
// =========================================================

    public String calculatePatientPriority(
            Patient patient
    ) {

        if (patient == null) {
            return "Normal";
        }

        // Special needs has highest normal priority.
        if (Boolean.TRUE.equals(
                patient.getSpecialNeeds()
        )) {
            return "Special Needs";
        }

        if (patient.getDateOfBirth() == null) {
            return "Normal";
        }

        LocalDate dateOfBirth =
                patient.getDateOfBirth().toLocalDate();

        LocalDate today =
                LocalDate.now();

        if (dateOfBirth.isAfter(today)) {
            return "Normal";
        }

        int age =
                java.time.Period
                        .between(
                                dateOfBirth,
                                today
                        )
                        .getYears();

        // Elderly = 65+
        if (age >= 65) {
            return "Elderly";
        }

        // Child = under 5
        if (age < 5) {
            return "Child";
        }

        return "Normal";
    }

    // =========================================================
// CALCULATE AGE
// =========================================================

    public int calculatePatientAge(
            Patient patient
    ) {

        if (patient == null
                || patient.getDateOfBirth() == null) {

            return 0;
        }

        LocalDate dateOfBirth =
                patient.getDateOfBirth()
                        .toLocalDate();

        LocalDate today =
                LocalDate.now();

        if (dateOfBirth.isAfter(today)) {
            return 0;
        }

        return java.time.Period
                .between(
                        dateOfBirth,
                        today
                )
                .getYears();
    }

    // =========================================================
    // PRIORITY
    // =========================================================

    private String normalizePriority(
            String priority
    ) {

        if (priority == null
                || priority.trim().isEmpty()) {

            return "Normal";
        }

        String value =
                priority.trim();

        if ("Emergency".equalsIgnoreCase(value)) {

            return "Emergency";
        }

        if ("Special Needs".equalsIgnoreCase(value)
                || "SpecialNeeds".equalsIgnoreCase(value)
                || "Special_Needs".equalsIgnoreCase(value)) {

            return "Special Needs";
        }

        if ("Elderly".equalsIgnoreCase(value)) {

            return "Elderly";
        }

        if ("Child".equalsIgnoreCase(value)) {

            return "Child";
        }

        if ("Normal".equalsIgnoreCase(value)) {

            return "Normal";
        }

        throw new RuntimeException(
                "Invalid priority. Allowed values: "
                        + "Emergency, Special Needs, Elderly, "
                        + "Child, Normal."
        );
    }

    // =========================================================
    // GET ALL
    // =========================================================

    public List<QueueToken> getAll() {
        return repo.findAll();
    }

    // =========================================================
    // DOCTOR QUEUE
    // =========================================================

    public List<QueueToken> doctorQueue(
            Long doctorId
    ) {

        if (doctorId == null) {
            throw new RuntimeException(
                    "Doctor ID is required."
            );
        }

        LocalDate today =
                LocalDate.now();

        return repo
                .findByDoctorIdOrderByQueueIdAsc(
                        doctorId
                )
                .stream()
                .filter(token ->
                        isAppointmentToday(
                                token,
                                today
                        )
                )
                .toList();
    }

    // =========================================================
    // CALL NEXT
    // =========================================================

    // =========================================================
// CALL NEXT
// =========================================================

    @Transactional
    public QueueToken callNext(
            Long doctorId
    ) {

        if (doctorId == null) {

            throw new RuntimeException(
                    "Doctor ID is required."
            );
        }

        DoctorStatus status =
                doctorStatusRepo
                        .findTopByDoctorIdOrderByUpdatedAtDesc(
                                doctorId
                        )
                        .orElse(null);

        if (status == null) {

            throw new RuntimeException(
                    "Doctor status has not been set."
            );
        }

        // =====================================================
        // DOCTOR MUST BE ARRIVED
        // =====================================================

        if (!"Arrived".equalsIgnoreCase(
                status.getArrivalStatus()
        )) {

            throw new RuntimeException(
                    "The doctor must be marked Arrived before calling the next patient."
            );
        }

        LocalDate today =
                LocalDate.now();

        // =====================================================
        // CHECK CURRENTLY SERVING PATIENT
        // =====================================================

        boolean alreadyServing =
                repo.findByDoctorIdOrderByQueueIdAsc(
                                doctorId
                        )
                        .stream()
                        .anyMatch(token ->
                                "Serving".equalsIgnoreCase(
                                        token.getQueueStatus()
                                )
                                        && isAppointmentToday(
                                        token,
                                        today
                                )
                        );

        if (alreadyServing) {

            throw new RuntimeException(
                    "The doctor is currently serving another patient. "
                            + "Complete or skip the current patient first."
            );
        }

        // =====================================================
        // GET TODAY'S WAITING PATIENTS
        // =====================================================

        List<QueueToken> waitingTokens =
                repo
                        .findByDoctorIdOrderByQueueIdAsc(
                                doctorId
                        )
                        .stream()
                        .filter(this::isWaiting)
                        .filter(token ->
                                isAppointmentToday(
                                        token,
                                        today
                                )
                        )
                        .toList();

        if (waitingTokens.isEmpty()) {

            throw new RuntimeException(
                    "No waiting patients for this doctor today."
            );
        }

        // =====================================================
        // SELECT NEXT PATIENT
        // ====================================================

        QueueToken token =
                waitingTokens
                        .stream()
                        .sorted(
                                this::compareQueuePriority
                        )
                        .findFirst()
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "No waiting patients for this doctor today."
                                )
                        );

        // =====================================================
        // MARK AS SERVING
        // =====================================================

        token.setQueueStatus(
                "Serving"
        );

        updateAppointmentStatus(
                token,
                "Serving"
        );

        QueueToken savedToken = repo.save(token);

        Appointment appointment = getAppointment(
                savedToken
        );

        notificationService.notifyPatientCalled(
                appointment.getPatientId(),
                appointment.getAppointmentId(),
                savedToken.getQueueId(),
                savedToken.getDoctorId(),
                savedToken.getTokenNumber()
        );

        return savedToken;
    }

    // =========================================================
    // WAITING
    // =========================================================

    private boolean isWaiting(
            QueueToken token
    ) {

        return token != null
                && "Waiting".equalsIgnoreCase(
                token.getQueueStatus()
        );
    }

    // =========================================================
    // TODAY
    // =========================================================

    private boolean isAppointmentToday(
            QueueToken token,
            LocalDate today
    ) {

        if (token == null
                || token.getAppointmentId() == null) {

            return false;
        }

        Appointment appointment =
                appointmentRepo.findById(
                        token.getAppointmentId()
                ).orElse(null);

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
    // PRIORITY ORDER
    // =========================================================

    // =========================================================
// PRIORITY ORDER
// =========================================================

    private int getPriorityOrder(
            QueueToken token
    ) {

        if (token == null
                || token.getPriorityType() == null
                || token.getPriorityType().trim().isEmpty()) {

            return Integer.MAX_VALUE;
        }

        String priority =
                normalizePriority(
                        token.getPriorityType()
                );

        return queueRuleRepo
                .findByPriorityTypeIgnoreCase(priority)
                .filter(rule ->
                        Boolean.TRUE.equals(
                                rule.getIsActive()
                        )
                )
                .map(QueueRule::getPriorityOrder)
                .orElse(Integer.MAX_VALUE);
    }

    // =========================================================
// EMERGENCY LEVEL ORDER
// =========================================================

    private int getEmergencyLevelOrder(
            QueueToken token
    ) {

        if (!isEmergency(token)) {

            return Integer.MAX_VALUE;
        }

        Integer level =
                token.getEmergencyLevel();

        if (level == null
                || level < 1
                || level > 3) {

            return Integer.MAX_VALUE;
        }

        /*
         * Level 1 = most urgent
         * Level 2 = medium
         * Level 3 = least urgent emergency
         */

        return level;
    }

    // =========================================================
    // COMPLETE
    // =========================================================

    @Transactional
    public QueueToken complete(
            Long queueId
    ) {

        QueueToken token =
                findToken(queueId);

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

        updateAppointmentStatus(
                token,
                "Completed"
        );

        QueueToken savedToken = repo.save(token);

        Appointment appointment = getAppointment(
                savedToken
        );

        notificationService.notifyAppointmentCompleted(
                appointment.getPatientId(),
                appointment.getAppointmentId(),
                savedToken.getQueueId(),
                savedToken.getDoctorId(),
                savedToken.getTokenNumber()
        );

        return savedToken;
    }

    // =========================================================
    // SKIP
    // =========================================================

    @Transactional
    public QueueToken skip(
            Long queueId
    ) {

        QueueToken token =
                findToken(queueId);

        if (!"Waiting".equalsIgnoreCase(
                token.getQueueStatus()
        )
                && !"Serving".equalsIgnoreCase(
                token.getQueueStatus()
        )) {

            throw new RuntimeException(
                    "Only Waiting or Serving patients can be skipped."
            );
        }

        token.setQueueStatus(
                "Skipped"
        );

        updateAppointmentStatus(
                token,
                "Skipped"
        );

        QueueToken savedToken = repo.save(token);

        Appointment appointment = getAppointment(
                savedToken
        );

        notificationService.notifyPatientSkipped(
                appointment.getPatientId(),
                appointment.getAppointmentId(),
                savedToken.getQueueId(),
                savedToken.getDoctorId(),
                savedToken.getTokenNumber()
        );

        return savedToken;
    }

    // =========================================================
    // UPDATE PRIORITY
    // =========================================================

    @Transactional
    public QueueToken updatePriority(
            Long queueId,
            String priority
    ) {

        QueueToken token =
                findToken(queueId);

        token.setPriorityType(
                normalizePriority(priority)
        );

        return repo.save(token);
    }

    // =========================================================
    // EMERGENCY TRANSFER
    // =========================================================

    @Transactional
    public QueueToken transferEmergency(
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
                    "Replacement doctor is required."
            );
        }

        if (dto.getReason() == null
                || dto.getReason().trim().isEmpty()) {

            throw new RuntimeException(
                    "Emergency transfer reason is required."
            );
        }

        QueueToken token =
                findToken(dto.getQueueId());

        if (!"Waiting".equalsIgnoreCase(
                token.getQueueStatus()
        )
                && !"Serving".equalsIgnoreCase(
                token.getQueueStatus()
        )) {

            throw new RuntimeException(
                    "Only Waiting or Serving patients can be transferred."
            );
        }

        Appointment appointment =
                getAppointment(token);

        LocalDate today =
                LocalDate.now();

        if (appointment.getAppointmentDate() == null) {
            throw new RuntimeException(
                    "Appointment date is missing."
            );
        }

        if (!appointment.getAppointmentDate()
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

        if (oldDoctorId == null) {
            throw new RuntimeException(
                    "Current doctor is not assigned."
            );
        }

        if (oldDoctorId.equals(newDoctorId)) {
            throw new RuntimeException(
                    "Patient is already assigned to this doctor."
            );
        }

        Doctor oldDoctor =
                doctorRepo.findById(
                        oldDoctorId
                ).orElseThrow(
                        () -> new RuntimeException(
                                "Current doctor could not be found."
                        )
                );

        Doctor newDoctor =
                doctorRepo.findById(
                        newDoctorId
                ).orElseThrow(
                        () -> new RuntimeException(
                                "Replacement doctor not found."
                        )
                );

        DoctorStatus newDoctorStatus =
                doctorStatusRepo
                        .findTopByDoctorIdOrderByUpdatedAtDesc(
                                newDoctorId
                        )
                        .orElse(null);

        if (newDoctorStatus == null) {
            throw new RuntimeException(
                    "Replacement doctor has no current arrival status."
            );
        }

        if (!"Arrived".equalsIgnoreCase(
                newDoctorStatus.getArrivalStatus()
        )) {

            throw new RuntimeException(
                    "Replacement doctor must be marked Arrived."
            );
        }

        LocalTime now =
                LocalTime.now();

        List<DoctorSchedule> schedules =
                doctorScheduleRepo
                        .findByDoctorIdAndAvailableDate(
                                newDoctorId,
                                today
                        );

        boolean currentlyScheduled =
                schedules
                        .stream()
                        .anyMatch(schedule -> {

                            if (schedule.getStartTime() == null
                                    || schedule.getEndTime() == null) {

                                return false;
                            }

                            return !now.isBefore(
                                    schedule.getStartTime()
                            )
                                    && !now.isAfter(
                                    schedule.getEndTime()
                            );
                        });

        if (!currentlyScheduled) {

            throw new RuntimeException(
                    "Replacement doctor is not currently scheduled to work."
            );
        }

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

        if (!oldSpecialization.trim()
                .equalsIgnoreCase(
                        newSpecialization.trim()
                )) {

            throw new RuntimeException(
                    "Emergency patient must be transferred to a doctor with the same specialization. "
                            + "Required: "
                            + oldSpecialization
                            + ", Selected: "
                            + newSpecialization
            );
        }

        // ==========================================
        // TRANSFER
        // ==========================================

        token.setPriorityType(
                "Emergency"
        );

        Integer emergencyLevel =
                appointment.getEmergencyLevel();

        if (emergencyLevel == null
                || emergencyLevel < 1) {

            emergencyLevel = 1;
        }

        token.setEmergencyLevel(
                emergencyLevel
        );

        appointment.setEmergencyLevel(
                emergencyLevel
        );

        token.setTransferredToDoctorId(
                newDoctorId
        );

        token.setEmergencyReason(
                dto.getReason().trim()
        );

        token.setDoctorId(
                newDoctorId
        );

        token.setQueueStatus(
                "Waiting"
        );

        token.setEstimatedWaitTime(
                0
        );

        appointment.setDoctorId(
                newDoctorId
        );

        appointment.setStatus(
                "Waiting"
        );

        appointmentRepo.save(
                appointment
        );

        QueueToken savedToken = repo.save(token);

        notificationService.notifyEmergencyTransfer(
                appointment.getPatientId(),
                appointment.getAppointmentId(),
                savedToken.getQueueId(),
                oldDoctorId,
                newDoctorId,
                dto.getReason().trim()
        );

        return savedToken;
    }

    // =========================================================
    // UPDATE QUEUE STATUS
    // =========================================================

    @Transactional
    public QueueToken updateStatus(
            Long queueId,
            String status
    ) {

        QueueToken token =
                findToken(queueId);

        if (status == null
                || status.trim().isEmpty()) {

            throw new RuntimeException(
                    "Queue status is required."
            );
        }

        String value =
                status.trim();

        String normalized;

        if ("Waiting".equalsIgnoreCase(value)) {

            normalized = "Waiting";

        } else if ("Serving".equalsIgnoreCase(value)) {

            normalized = "Serving";

        } else if ("Skipped".equalsIgnoreCase(value)) {

            normalized = "Skipped";

        } else if ("Completed".equalsIgnoreCase(value)) {

            normalized = "Completed";

        } else if ("Cancelled".equalsIgnoreCase(value)) {

            normalized = "Cancelled";

        } else {

            throw new RuntimeException(
                    "Invalid queue status. Allowed values: Waiting, Serving, Skipped, Completed, Cancelled."
            );
        }

        token.setQueueStatus(
                normalized
        );

        updateAppointmentStatus(
                token,
                normalized
        );

        return repo.save(token);
    }

    // =========================================================
    // FIND TOKEN
    // =========================================================

    private QueueToken findToken(
            Long queueId
    ) {

        if (queueId == null) {
            throw new RuntimeException(
                    "Queue ID is required."
            );
        }

        return repo.findById(
                queueId
        ).orElseThrow(
                () -> new RuntimeException(
                        "Queue token not found."
                )
        );
    }

    // =========================================================
    // GET APPOINTMENT
    // =========================================================

    private Appointment getAppointment(
            QueueToken token
    ) {

        if (token.getAppointmentId() == null) {

            throw new RuntimeException(
                    "This queue token is not linked to an appointment."
            );
        }

        return appointmentRepo.findById(
                token.getAppointmentId()
        ).orElseThrow(
                () -> new RuntimeException(
                        "Appointment not found for this queue token."
                )
        );
    }

    // =========================================================
    // UPDATE APPOINTMENT STATUS
    // =========================================================

    private void updateAppointmentStatus(
            QueueToken token,
            String queueStatus
    ) {
        if (token.getAppointmentId() == null) {
            return;
        }

        Appointment appointment =
                appointmentRepo.findById(
                        token.getAppointmentId()
                ).orElse(null);

        if (appointment == null) {
            return;
        }

        /*
         * Queue status and appointment status are separate.
         *
         * Waiting  -> Appointment remains Confirmed
         * Serving  -> Appointment remains Confirmed
         * Skipped  -> Appointment remains Confirmed
         * Completed -> Appointment becomes Completed
         * Canceled -> Appointment becomes Canceled
         */

        if ("Completed".equalsIgnoreCase(queueStatus)) {

            appointment.setStatus("Completed");

            appointmentRepo.save(appointment);

        } else if ("Cancelled".equalsIgnoreCase(queueStatus)) {

            appointment.setStatus("Cancelled");

            appointmentRepo.save(appointment);
        }
    }

    private void validateEmergencyLevel(
            String priority,
            Integer emergencyLevel
    ) {

        int level =
                emergencyLevel == null
                        ? 0
                        : emergencyLevel;

        if (level < 0 || level > 3) {

            throw new RuntimeException(
                    "Emergency level must be 0, 1, 2, or 3."
            );
        }

        if ("Emergency".equalsIgnoreCase(priority)) {

            if (level < 1) {

                throw new RuntimeException(
                        "Emergency patients must have emergency level 1, 2, or 3."
                );
            }

        } else if (level != 0) {

            throw new RuntimeException(
                    "Only Emergency patients can have an emergency level."
            );
        }
    }

    // =========================================================
// NOTIFY PATIENT - YOUR TURN
// =========================================================

    private void notifyPatientYourTurn(
            QueueToken token
    ) {

        if (token == null) {
            return;
        }

        Appointment appointment =
                getAppointment(token);

        Long patientId =
                appointment.getPatientId();

        if (patientId == null) {
            return;
        }

        String doctorName =
                "your doctor";

        if (token.getDoctorId() != null) {

            Doctor doctor =
                    doctorRepo
                            .findById(
                                    token.getDoctorId()
                            )
                            .orElse(null);

            if (
                    doctor != null
                            &&
                            doctor.getDoctorName() != null
                            &&
                            !doctor.getDoctorName()
                                    .trim()
                                    .isEmpty()
            ) {

                doctorName =
                        doctor.getDoctorName();
            }
        }

        String message =
                "It is now your turn. " +
                        "Please proceed to see " +
                        doctorName +
                        ".";

        notificationService.createNotification(
                patientId,
                appointment.getAppointmentId(),
                token.getQueueId(),
                token.getDoctorId(),
                "YOUR_TURN",
                "Your Turn",
                message
        );
    }

    // =========================================================
// NOTIFY PATIENT - CONSULTATION COMPLETED
// =========================================================

    private void notifyPatientCompleted(
            QueueToken token
    ) {

        if (token == null) {
            return;
        }

        Appointment appointment =
                getAppointment(token);

        Long patientId =
                appointment.getPatientId();

        if (patientId == null) {
            return;
        }

        notificationService.createNotification(
                patientId,
                appointment.getAppointmentId(),
                token.getQueueId(),
                token.getDoctorId(),
                "CONSULTATION_COMPLETED",
                "Consultation Completed",
                "Your consultation has been completed."
        );
    }

    // =========================================================
// NOTIFY PATIENT - EMERGENCY TRANSFER
// =========================================================

    private void notifyEmergencyTransfer(
            QueueToken token,
            Doctor oldDoctor,
            Doctor newDoctor
    ) {

        if (token == null) {
            return;
        }

        Appointment appointment =
                getAppointment(token);

        Long patientId =
                appointment.getPatientId();

        if (patientId == null) {
            return;
        }

        String oldDoctorName =
                oldDoctor != null
                        && oldDoctor.getDoctorName() != null
                        ? oldDoctor.getDoctorName()
                        : "your previous doctor";

        String newDoctorName =
                newDoctor != null
                        && newDoctor.getDoctorName() != null
                        ? newDoctor.getDoctorName()
                        : "your new doctor";

        String message =
                "Due to an emergency, your consultation " +
                        "has been transferred from " +
                        oldDoctorName +
                        " to " +
                        newDoctorName +
                        ". Please wait for your turn.";

        notificationService.createNotification(
                patientId,
                appointment.getAppointmentId(),
                token.getQueueId(),
                token.getDoctorId(),
                "EMERGENCY_TRANSFER",
                "Doctor Changed",
                message
        );
    }
}