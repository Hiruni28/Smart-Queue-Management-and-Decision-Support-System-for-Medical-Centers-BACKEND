package com.hospital.queue_management_system.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import com.hospital.queue_management_system.model.DoctorSchedule;
import com.hospital.queue_management_system.repository.DoctorScheduleRepository;

@Service
public class DoctorScheduleService {

    private final DoctorScheduleRepository repo;

    public DoctorScheduleService(
            DoctorScheduleRepository repo
    ) {
        this.repo = repo;
    }

    // ==========================================
    // GET ALL
    // ==========================================

    public List<DoctorSchedule> getAll() {

        return repo.findAll();
    }

    // ==========================================
    // GET BY DOCTOR
    // ==========================================

    public List<DoctorSchedule> getByDoctor(
            Long doctorId
    ) {

        if (doctorId == null) {
            throw new RuntimeException(
                    "Doctor ID is required."
            );
        }

        return repo.findByDoctorId(doctorId);
    }

    // ==========================================
    // GET TODAY
    // ==========================================

    public List<DoctorSchedule> getTodaySchedules() {

        return repo.findByAvailableDate(
                LocalDate.now()
        );
    }

    // ==========================================
    // ADD
    // ==========================================

    public DoctorSchedule add(
            DoctorSchedule schedule
    ) {

        validateSchedule(schedule);

        return repo.save(schedule);
    }

    // ==========================================
    // UPDATE
    // ==========================================

    public DoctorSchedule update(
            DoctorSchedule schedule
    ) {

        if (schedule == null) {
            throw new RuntimeException(
                    "Schedule data is required."
            );
        }

        if (schedule.getScheduleId() == null) {
            throw new RuntimeException(
                    "Schedule ID is required."
            );
        }

        if (!repo.existsById(
                schedule.getScheduleId()
        )) {

            throw new RuntimeException(
                    "Schedule not found."
            );
        }

        validateSchedule(schedule);

        return repo.save(schedule);
    }

    // ==========================================
    // VALIDATION
    // ==========================================

    private void validateSchedule(
            DoctorSchedule schedule
    ) {

        if (schedule == null) {
            throw new RuntimeException(
                    "Schedule data is required."
            );
        }

        if (schedule.getAvailableDate() == null) {
            throw new RuntimeException(
                    "Available date is required."
            );
        }

        if (schedule.getStartTime() == null) {
            throw new RuntimeException(
                    "Start time is required."
            );
        }

        if (schedule.getEndTime() == null) {
            throw new RuntimeException(
                    "End time is required."
            );
        }

        if (!schedule.getEndTime().isAfter(
                schedule.getStartTime()
        )) {

            throw new RuntimeException(
                    "End time must be after start time."
            );
        }

        if (schedule.getMaxPatients() == null) {
            throw new RuntimeException(
                    "Maximum patients is required."
            );
        }

        if (schedule.getMaxPatients() <= 0) {
            throw new RuntimeException(
                    "Maximum patients must be greater than zero."
            );
        }
    }

    // ==========================================
    // DELETE
    // ==========================================

    public void delete(Long id) {

        if (id == null) {
            throw new RuntimeException(
                    "Schedule ID is required."
            );
        }

        if (!repo.existsById(id)) {
            throw new RuntimeException(
                    "Schedule not found."
            );
        }

        repo.deleteById(id);
    }
}