package com.hospital.queue_management_system.service;

import org.springframework.stereotype.Service;

import com.hospital.queue_management_system.dto.DashboardStatsDTO;
import com.hospital.queue_management_system.repository.*;

@Service

public class DashboardService {

    private final DoctorRepository doctorRepo;

    private final StaffRepository staffRepo;

    private final AppointmentRepository appointmentRepo;

    private final QueueTokenRepository queueRepo;

    public DashboardService(

            DoctorRepository doctorRepo,

            StaffRepository staffRepo,

            AppointmentRepository appointmentRepo,

            QueueTokenRepository queueRepo

    ) {

        this.doctorRepo =
                doctorRepo;

        this.staffRepo =
                staffRepo;

        this.appointmentRepo =
                appointmentRepo;

        this.queueRepo =
                queueRepo;

    }

    public DashboardStatsDTO getStats() {

        Long waitingPatients =
                queueRepo.countByQueueStatus(
                        "Waiting"
                );

        Long servingPatients =
                queueRepo.countByQueueStatus(
                        "Serving"
                );

        return new DashboardStatsDTO(

                doctorRepo.count(),

                staffRepo.count(),

                appointmentRepo.count(),

                waitingPatients,

                servingPatients

        );

    }

}