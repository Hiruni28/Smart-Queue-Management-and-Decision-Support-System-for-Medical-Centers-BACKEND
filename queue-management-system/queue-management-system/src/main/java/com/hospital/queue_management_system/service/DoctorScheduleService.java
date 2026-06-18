package com.hospital.queue_management_system.service;

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

    public List<DoctorSchedule> getAll() {

        return repo.findAll();

    }

    public List<DoctorSchedule> getByDoctor(
            Long doctorId
    ) {

        return repo.findByDoctorId(
                doctorId
        );

    }

    public DoctorSchedule add(
            DoctorSchedule schedule
    ) {

        return repo.save(
                schedule
        );

    }

    public DoctorSchedule update(
            DoctorSchedule schedule
    ) {

        return repo.save(
                schedule
        );

    }

    public void delete(
            Long id
    ) {

        repo.deleteById(
                id
        );

    }

}