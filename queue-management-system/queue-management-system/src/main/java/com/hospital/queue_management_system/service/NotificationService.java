package com.hospital.queue_management_system.service;

import com.hospital.queue_management_system.model.Notification;
import com.hospital.queue_management_system.repository.NotificationRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository repo;

    public NotificationService(
            NotificationRepository repo
    ) {
        this.repo = repo;
    }

    // =========================================================
    // GET ALL PATIENT NOTIFICATIONS
    // =========================================================

    public List<Notification> getPatientNotifications(
            Long patientId
    ) {

        validatePatientId(patientId);

        return repo.findByPatientIdOrderByCreatedAtDesc(
                patientId
        );
    }

    // =========================================================
    // GET UNREAD NOTIFICATIONS
    // =========================================================

    public List<Notification> getUnreadNotifications(
            Long patientId
    ) {

        validatePatientId(patientId);

        return repo
                .findByPatientIdAndIsReadFalseOrderByCreatedAtDesc(
                        patientId
                );
    }

    // =========================================================
    // GET UNREAD COUNT
    // =========================================================

    public long getUnreadCount(
            Long patientId
    ) {

        validatePatientId(patientId);

        return repo.countByPatientIdAndIsReadFalse(
                patientId
        );
    }

    // =========================================================
    // GET ONE NOTIFICATION
    // =========================================================

    public Notification getNotification(
            Long notificationId
    ) {

        if (notificationId == null) {
            throw new RuntimeException(
                    "Notification ID is required."
            );
        }

        return repo.findById(notificationId)
                .orElseThrow(
                        () -> new RuntimeException(
                                "Notification not found."
                        )
                );
    }

    // =========================================================
    // MARK ONE AS READ
    // =========================================================

    @Transactional
    public Notification markAsRead(
            Long notificationId
    ) {

        if (notificationId == null) {
            throw new RuntimeException(
                    "Notification ID is required."
            );
        }

        Notification notification =
                repo.findById(notificationId)
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "Notification not found."
                                )
                        );

        notification.setIsRead(true);

        return repo.save(notification);
    }

    // =========================================================
    // MARK ALL AS READ
    // =========================================================

    @Transactional
    public void markAllAsRead(
            Long patientId
    ) {

        validatePatientId(patientId);

        List<Notification> notifications =
                repo.findByPatientIdOrderByCreatedAtDesc(
                        patientId
                );

        for (Notification notification : notifications) {
            notification.setIsRead(true);
        }

        repo.saveAll(notifications);
    }

    // =========================================================
    // CREATE NOTIFICATION
    // =========================================================

    @Transactional
    public Notification createNotification(
            Long patientId,
            Long appointmentId,
            Long queueId,
            Long doctorId,
            String notificationType,
            String title,
            String message
    ) {

        validatePatientId(patientId);

        if (notificationType == null
                || notificationType.trim().isEmpty()) {

            throw new RuntimeException(
                    "Notification type is required."
            );
        }

        if (title == null
                || title.trim().isEmpty()) {

            throw new RuntimeException(
                    "Notification title is required."
            );
        }

        if (message == null
                || message.trim().isEmpty()) {

            throw new RuntimeException(
                    "Notification message is required."
            );
        }

        Notification notification =
                new Notification();

        notification.setPatientId(
                patientId
        );

        notification.setAppointmentId(
                appointmentId
        );

        notification.setQueueId(
                queueId
        );

        notification.setDoctorId(
                doctorId
        );

        notification.setNotificationType(
                notificationType.trim()
        );

        notification.setTitle(
                title.trim()
        );

        notification.setMessage(
                message.trim()
        );

        notification.setIsRead(false);

        return repo.save(notification);
    }

    // =========================================================
    // QUEUE TOKEN CREATED
    // =========================================================

    @Transactional
    public Notification notifyTokenCreated(
            Long patientId,
            Long appointmentId,
            Long queueId,
            Long doctorId,
            String tokenNumber,
            Integer estimatedWaitTime
    ) {

        String waitMessage =
                estimatedWaitTime != null
                        ? "Estimated waiting time: "
                        + estimatedWaitTime
                        + " minutes."
                        : "Please wait for your turn.";

        return createNotification(
                patientId,
                appointmentId,
                queueId,
                doctorId,
                "QUEUE_TOKEN_CREATED",
                "Queue Token Created",
                "Your queue token "
                        + tokenNumber
                        + " has been created. "
                        + waitMessage
        );
    }

    // =========================================================
    // PATIENT CALLED
    // =========================================================

    @Transactional
    public Notification notifyPatientCalled(
            Long patientId,
            Long appointmentId,
            Long queueId,
            Long doctorId,
            String tokenNumber
    ) {

        return createNotification(
                patientId,
                appointmentId,
                queueId,
                doctorId,
                "PATIENT_CALLED",
                "Your Turn",
                "Token "
                        + tokenNumber
                        + " is now being served. "
                        + "Please proceed to the doctor."
        );
    }

    // =========================================================
    // PATIENT SKIPPED
    // =========================================================

    @Transactional
    public Notification notifyPatientSkipped(
            Long patientId,
            Long appointmentId,
            Long queueId,
            Long doctorId,
            String tokenNumber
    ) {

        return createNotification(
                patientId,
                appointmentId,
                queueId,
                doctorId,
                "PATIENT_SKIPPED",
                "Queue Token Skipped",
                "Your token "
                        + tokenNumber
                        + " has been skipped. "
                        + "Please contact the hospital staff."
        );
    }

    // =========================================================
    // APPOINTMENT COMPLETED
    // =========================================================

    @Transactional
    public Notification notifyAppointmentCompleted(
            Long patientId,
            Long appointmentId,
            Long queueId,
            Long doctorId,
            String tokenNumber
    ) {

        return createNotification(
                patientId,
                appointmentId,
                queueId,
                doctorId,
                "APPOINTMENT_COMPLETED",
                "Appointment Completed",
                "Your appointment for token "
                        + tokenNumber
                        + " has been completed."
        );
    }

    // =========================================================
    // EMERGENCY TRANSFER
    // =========================================================

    @Transactional
    public Notification notifyEmergencyTransfer(
            Long patientId,
            Long appointmentId,
            Long queueId,
            Long oldDoctorId,
            Long newDoctorId,
            String reason
    ) {

        return createNotification(
                patientId,
                appointmentId,
                queueId,
                newDoctorId,
                "EMERGENCY_TRANSFER",
                "Emergency Transfer",
                "You have been transferred to another doctor "
                        + "for emergency care. "
                        + "Reason: "
                        + reason
        );
    }

    // =========================================================
    // NOTIFY DOCTOR DELAYED
    // =========================================================

    @Transactional
    public Notification notifyDoctorDelayed(
            Long patientId,
            Long appointmentId,
            Long queueId,
            Long doctorId,
            String reason,
            String doctorName
    ) {

        String message;

        if (
                reason != null
                        &&
                        !reason.trim().isEmpty()
        ) {

            message =
                    "Your doctor is currently delayed. " +
                            "Reason: " +
                            reason.trim();

        } else {

            message =
                    "Your doctor is currently delayed. " +
                            "Please expect a longer waiting time.";
        }

        return createNotification(
                patientId,
                appointmentId,
                queueId,
                doctorId,
                "DOCTOR_DELAYED",
                "Doctor Delayed",
                message
        );
    }

    // =========================================================
    // NOTIFY DOCTOR ARRIVED (NEW)
    // =========================================================

    @Transactional
    public Notification notifyDoctorArrived(
            Long patientId,
            Long appointmentId,
            Long queueId,
            Long doctorId
    ) {

        return createNotification(
                patientId,
                appointmentId,
                queueId,
                doctorId,
                "DOCTOR_ARRIVED",
                "Doctor Arrived",
                "Your doctor has arrived. " +
                        "The queue will start moving shortly."
        );
    }

    // =========================================================
    // NOTIFY DOCTOR UNAVAILABLE
    // =========================================================

    @Transactional
    public Notification notifyDoctorUnavailable(
            Long patientId,
            Long appointmentId,
            Long queueId,
            Long doctorId
    ) {

        return createNotification(
                patientId,
                appointmentId,
                queueId,
                doctorId,
                "DOCTOR_UNAVAILABLE",
                "Doctor Unavailable",
                "Your doctor is currently unavailable. " +
                        "Please wait for further updates."
        );
    }

    // =========================================================
    // VALIDATE PATIENT ID
    // =========================================================

    private void validatePatientId(
            Long patientId
    ) {

        if (patientId == null) {
            throw new RuntimeException(
                    "Patient ID is required."
            );
        }
    }
}