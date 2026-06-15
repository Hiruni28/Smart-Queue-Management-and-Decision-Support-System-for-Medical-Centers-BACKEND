package com.hospital.queue_management_system.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hospital.queue_management_system.model.DoctorSchedule;

public interface DoctorScheduleRepository
        extends JpaRepository<DoctorSchedule,Long>{

    List<DoctorSchedule> findByDoctorId(
            Long doctorId
    );

}