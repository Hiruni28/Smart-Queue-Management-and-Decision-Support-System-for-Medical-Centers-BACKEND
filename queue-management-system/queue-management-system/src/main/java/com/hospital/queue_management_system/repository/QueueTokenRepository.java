package com.hospital.queue_management_system.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hospital.queue_management_system.model.QueueToken;

public interface QueueTokenRepository
        extends JpaRepository<QueueToken, Long> {

    List<QueueToken> findByQueueStatusOrderByQueueIdAsc(
            String queueStatus
    );

    Optional<QueueToken> findFirstByQueueStatusOrderByQueueIdAsc(
            String queueStatus
    );

    List<QueueToken> findByDoctorIdOrderByQueueIdAsc(
            Long doctorId
    );

    Optional<QueueToken> findFirstByDoctorIdAndQueueStatusOrderByQueueIdAsc(
            Long doctorId,
            String queueStatus
    );

    Long countByQueueStatus(
            String queueStatus
    );

    Optional<QueueToken> findByAppointmentId(
            Long appointmentId
    );

    void deleteByAppointmentId(
            Long appointmentId
    );

    List<QueueToken> findByDoctorIdAndQueueStatus(
            Long doctorId,
            String queueStatus
    );
}