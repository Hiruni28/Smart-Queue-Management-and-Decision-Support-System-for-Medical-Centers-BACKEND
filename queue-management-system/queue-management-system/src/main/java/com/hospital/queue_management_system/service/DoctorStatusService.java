package com.hospital.queue_management_system.service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hospital.queue_management_system.model.Appointment;
import com.hospital.queue_management_system.model.DoctorStatus;
import com.hospital.queue_management_system.model.QueueToken;
import com.hospital.queue_management_system.repository.AppointmentRepository;
import com.hospital.queue_management_system.repository.DoctorStatusRepository;
import com.hospital.queue_management_system.repository.QueueTokenRepository;

@Service
public class DoctorStatusService {

    private final DoctorStatusRepository statusRepo;
    private final QueueTokenRepository queueRepo;
    private final AppointmentRepository appointmentRepo;
    private final NotificationService notificationService;

    public DoctorStatusService(
            DoctorStatusRepository statusRepo,
            QueueTokenRepository queueRepo,
            AppointmentRepository appointmentRepo,
            NotificationService notificationService
    ) {
        this.statusRepo = statusRepo;
        this.queueRepo = queueRepo;
        this.appointmentRepo = appointmentRepo;
        this.notificationService = notificationService;
    }

    // =========================================================
    // GET ALL CURRENT STATUS RECORDS
    // =========================================================

    public List<DoctorStatus> getAll() {
        return statusRepo.findAllByOrderByUpdatedAtDesc();
    }

    // =========================================================
    // GET CURRENT DOCTOR STATUS
    // =========================================================

    public DoctorStatus getCurrentStatus(Long doctorId) {

        if (doctorId == null) {
            throw new RuntimeException("Doctor ID is required.");
        }

        return statusRepo
                .findTopByDoctorIdOrderByUpdatedAtDesc(doctorId)
                .orElse(null);
    }

    // =========================================================
    // SAVE / UPDATE DOCTOR STATUS
    // =========================================================

    @Transactional
    public DoctorStatus save(DoctorStatus incoming) {

        if (incoming == null) {
            throw new RuntimeException("Doctor status data is required.");
        }

        if (incoming.getDoctorId() == null) {
            throw new RuntimeException("Doctor ID is required.");
        }

        String newStatus = normalizeStatus(incoming.getArrivalStatus());

        DoctorStatus existing = getCurrentStatus(incoming.getDoctorId());

        LocalDateTime now = LocalDateTime.now();

        String oldStatus = existing != null ? existing.getArrivalStatus() : null;

        // =====================================================
        // CHECK IF DOCTOR IS ENTERING / LEAVING DELAYED
        // =====================================================

        boolean enteringDelayed =
                "Delayed".equalsIgnoreCase(newStatus)
                        && !"Delayed".equalsIgnoreCase(oldStatus);

        boolean leavingDelayed =
                existing != null
                        && "Delayed".equalsIgnoreCase(oldStatus)
                        && !"Delayed".equalsIgnoreCase(newStatus)
                        && existing.getUpdatedAt() != null;

        // =====================================================
        // CHECK IF DOCTOR IS ENTERING ARRIVED / UNAVAILABLE
        // =====================================================

        boolean enteringArrived =
                "Arrived".equalsIgnoreCase(newStatus)
                        && !"Arrived".equalsIgnoreCase(oldStatus);

        boolean enteringUnavailable =
                "Unavailable".equalsIgnoreCase(newStatus)
                        && !"Unavailable".equalsIgnoreCase(oldStatus);

        // =====================================================
        // WHEN DELAY ENDS
        // =====================================================

        if (leavingDelayed) {

            long delayMinutes =
                    Duration.between(existing.getUpdatedAt(), now).toMinutes();

            if (delayMinutes > 0) {
                increaseWaitingTimes(existing.getDoctorId(), (int) delayMinutes);
            }
        }

        // =====================================================
        // CREATE FIRST STATUS RECORD
        // =====================================================

        if (existing == null) {
            existing = new DoctorStatus();
            existing.setDoctorId(incoming.getDoctorId());
        }

        // =====================================================
        // DOCTOR DELAYED
        // =====================================================

        if ("Delayed".equalsIgnoreCase(newStatus)) {

            if (enteringDelayed || existing.getUpdatedAt() == null) {
                existing.setUpdatedAt(now);
            }

            existing.setDelayReason(cleanReason(incoming.getDelayReason()));
        }

        // =====================================================
        // DOCTOR ARRIVED / UNAVAILABLE
        // =====================================================

        else {
            existing.setDelayReason(null);
            existing.setUpdatedAt(now);
        }

        // =====================================================
        // SAVE ACTUAL STATUS
        // =====================================================

        existing.setArrivalStatus(newStatus);

        DoctorStatus savedStatus = statusRepo.save(existing);

        // =====================================================
        // NOTIFY WHEN DOCTOR BECOMES DELAYED
        // =====================================================

        if (enteringDelayed) {
            notifyWaitingPatientsAboutDelay(
                    incoming.getDoctorId(),
                    savedStatus.getDelayReason()
            );
        }

        // =====================================================
        // NOTIFY WHEN DOCTOR ARRIVES
        // =====================================================

        if (enteringArrived) {
            notifyWaitingPatientsAboutArrival(incoming.getDoctorId());
        }

        // =====================================================
        // NOTIFY WHEN DOCTOR BECOMES UNAVAILABLE
        // =====================================================

        if (enteringUnavailable) {
            notifyWaitingPatientsAboutUnavailable(incoming.getDoctorId());
        }

        return savedStatus;
    }

    // =========================================================
    // NORMALIZE STATUS
    // =========================================================

    private String normalizeStatus(String status) {

        if (status == null || status.trim().isEmpty()) {
            throw new RuntimeException("Arrival status is required.");
        }

        String value = status.trim();

        if ("Arrived".equalsIgnoreCase(value)) {
            return "Arrived";
        }

        if ("Delayed".equalsIgnoreCase(value)) {
            return "Delayed";
        }

        if ("Unavailable".equalsIgnoreCase(value)) {
            return "Unavailable";
        }

        throw new RuntimeException(
                "Invalid doctor status. Allowed values: Arrived, Delayed, Unavailable."
        );
    }

    // =========================================================
    // CLEAN DELAY REASON
    // =========================================================

    private String cleanReason(String reason) {

        if (reason == null) {
            return null;
        }

        String value = reason.trim();

        return value.isEmpty() ? null : value;
    }

    // =========================================================
    // INCREASE WAITING TIMES
    // =========================================================

    private void increaseWaitingTimes(Long doctorId, int delayMinutes) {

        if (doctorId == null || delayMinutes <= 0) {
            return;
        }

        LocalDate today = LocalDate.now();

        List<QueueToken> tokens =
                queueRepo.findByDoctorIdOrderByQueueIdAsc(doctorId);

        for (QueueToken token : tokens) {

            if (token == null) continue;

            if (token.getQueueStatus() == null
                    || !"Waiting".equalsIgnoreCase(token.getQueueStatus())) {
                continue;
            }

            if (token.getAppointmentId() == null) continue;

            Appointment appointment = getAppointment(token.getAppointmentId());

            if (appointment == null) continue;

            if (appointment.getAppointmentDate() == null) continue;

            LocalDate appointmentDate =
                    appointment.getAppointmentDate().toLocalDate();

            if (!appointmentDate.equals(today)) continue;

            int currentWait =
                    token.getEstimatedWaitTime() != null
                            ? token.getEstimatedWaitTime()
                            : 0;

            token.setEstimatedWaitTime(Math.max(currentWait, 0) + delayMinutes);
        }

        queueRepo.saveAll(tokens);
    }

    // =========================================================
    // NOTIFY PATIENTS ABOUT DOCTOR DELAY
    // =========================================================

    private void notifyWaitingPatientsAboutDelay(Long doctorId, String reason) {

        if (doctorId == null) return;

        List<QueueToken> tokens =
                queueRepo.findByDoctorIdOrderByQueueIdAsc(doctorId);

        for (QueueToken token : tokens) {

            if (token == null) continue;

            if (!"Waiting".equalsIgnoreCase(token.getQueueStatus())) continue;

            if (token.getAppointmentId() == null) continue;

            Appointment appointment = getAppointment(token.getAppointmentId());

            if (appointment == null) continue;

            notificationService.notifyDoctorDelayed(
                    appointment.getPatientId(),
                    appointment.getAppointmentId(),
                    token.getQueueId(),
                    doctorId,
                    reason,
                    null
            );
        }
    }

    // =========================================================
    // NOTIFY PATIENTS ABOUT DOCTOR ARRIVAL (NEW)
    // =========================================================

    private void notifyWaitingPatientsAboutArrival(Long doctorId) {

        if (doctorId == null) return;

        List<QueueToken> tokens =
                queueRepo.findByDoctorIdOrderByQueueIdAsc(doctorId);

        for (QueueToken token : tokens) {

            if (token == null) continue;

            if (!"Waiting".equalsIgnoreCase(token.getQueueStatus())) continue;

            if (token.getAppointmentId() == null) continue;

            Appointment appointment = getAppointment(token.getAppointmentId());

            if (appointment == null) continue;

            notificationService.notifyDoctorArrived(
                    appointment.getPatientId(),
                    appointment.getAppointmentId(),
                    token.getQueueId(),
                    doctorId
            );
        }
    }

    // =========================================================
    // NOTIFY PATIENTS ABOUT DOCTOR UNAVAILABLE
    // =========================================================

    private void notifyWaitingPatientsAboutUnavailable(Long doctorId) {

        if (doctorId == null) return;

        List<QueueToken> tokens =
                queueRepo.findByDoctorIdOrderByQueueIdAsc(doctorId);

        for (QueueToken token : tokens) {

            if (token == null) continue;

            if (!"Waiting".equalsIgnoreCase(token.getQueueStatus())) continue;

            if (token.getAppointmentId() == null) continue;

            Appointment appointment = getAppointment(token.getAppointmentId());

            if (appointment == null) continue;

            notificationService.notifyDoctorUnavailable(
                    appointment.getPatientId(),
                    appointment.getAppointmentId(),
                    token.getQueueId(),
                    doctorId
            );
        }
    }

    // =========================================================
    // GET APPOINTMENT
    // =========================================================

    private Appointment getAppointment(Long appointmentId) {

        if (appointmentId == null) return null;

        return appointmentRepo.findById(appointmentId).orElse(null);
    }

    // =========================================================
    // DELETE STATUS
    // =========================================================

    @Transactional
    public void delete(Long id) {

        if (id == null) {
            throw new RuntimeException("Status ID is required.");
        }

        if (!statusRepo.existsById(id)) {
            throw new RuntimeException("Doctor status not found.");
        }

        statusRepo.deleteById(id);
    }
}