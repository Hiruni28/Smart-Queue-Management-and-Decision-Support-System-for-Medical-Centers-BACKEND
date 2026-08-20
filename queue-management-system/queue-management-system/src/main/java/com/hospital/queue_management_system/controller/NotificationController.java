package com.hospital.queue_management_system.controller;

import com.hospital.queue_management_system.model.Notification;
import com.hospital.queue_management_system.service.NotificationService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notifications")
@CrossOrigin(origins = "http://localhost:5173")
public class NotificationController {

    private final NotificationService service;

    public NotificationController(
            NotificationService service
    ) {
        this.service = service;
    }

    // =========================================================
    // GET PATIENT NOTIFICATIONS
    // =========================================================

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<Notification>>
    getPatientNotifications(
            @PathVariable Long patientId
    ) {

        return ResponseEntity.ok(
                service.getPatientNotifications(
                        patientId
                )
        );
    }

    // =========================================================
    // GET UNREAD NOTIFICATIONS
    // =========================================================

    @GetMapping("/patient/{patientId}/unread")
    public ResponseEntity<List<Notification>>
    getUnreadNotifications(
            @PathVariable Long patientId
    ) {

        return ResponseEntity.ok(
                service.getUnreadNotifications(
                        patientId
                )
        );
    }

    // =========================================================
    // GET UNREAD COUNT
    // =========================================================

    @GetMapping("/patient/{patientId}/unread-count")
    public ResponseEntity<Long>
    getUnreadCount(
            @PathVariable Long patientId
    ) {

        return ResponseEntity.ok(
                service.getUnreadCount(
                        patientId
                )
        );
    }

    // =========================================================
    // GET ONE NOTIFICATION
    // =========================================================

    @GetMapping("/{notificationId}")
    public ResponseEntity<Notification>
    getNotification(
            @PathVariable Long notificationId
    ) {

        if (notificationId == null) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok(
                service.getNotification(
                        notificationId
                )
        );
    }

    // =========================================================
    // MARK ONE AS READ
    // =========================================================

    @PutMapping("/{notificationId}/read")
    public ResponseEntity<Notification>
    markAsRead(
            @PathVariable Long notificationId
    ) {

        return ResponseEntity.ok(
                service.markAsRead(
                        notificationId
                )
        );
    }

    // =========================================================
    // MARK ALL AS READ
    // =========================================================

    @PutMapping("/patient/{patientId}/read-all")
    public ResponseEntity<Void>
    markAllAsRead(
            @PathVariable Long patientId
    ) {

        service.markAllAsRead(
                patientId
        );

        return ResponseEntity.ok().build();
    }
}