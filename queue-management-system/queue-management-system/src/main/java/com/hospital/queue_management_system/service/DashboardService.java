package com.hospital.queue_management_system.service;

import org.springframework.stereotype.Service;

import com.hospital.queue_management_system.dto.DashboardStatsDTO;
import com.hospital.queue_management_system.repository.*;

@Service

public class DashboardService {

    private final DoctorRepository doctorRepo;

    private final StaffRepository staffRepo;

    private final AppointmentRepository appointmentRepo;

    public DashboardService(

            DoctorRepository doctorRepo,

            StaffRepository staffRepo,

            AppointmentRepository appointmentRepo

    ){

        this.doctorRepo=
                doctorRepo;

        this.staffRepo=
                staffRepo;

        this.appointmentRepo=
                appointmentRepo;

    }

    public DashboardStatsDTO getStats(){

        return new DashboardStatsDTO(

                doctorRepo.count(),

                staffRepo.count(),

                appointmentRepo.count()

        );

    }

}