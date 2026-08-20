package com.hospital.queue_management_system.repository;

import com.hospital.queue_management_system.model.DoctorSchedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DoctorScheduleRepository
        extends JpaRepository<DoctorSchedule, Long> {

    List<DoctorSchedule> findByDoctorId(Long doctorId);

    List<DoctorSchedule> findByAvailableDate(LocalDate availableDate);

    List<DoctorSchedule> findByDoctorIdAndAvailableDate(
            Long doctorId,
            LocalDate availableDate
    );

    Optional<DoctorSchedule> findFirstByDoctorIdAndAvailableDate(
            Long doctorId,
            LocalDate availableDate
    );
}