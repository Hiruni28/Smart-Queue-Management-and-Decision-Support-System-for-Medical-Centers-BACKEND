package com.hospital.queue_management_system.service;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.hospital.queue_management_system.model.DoctorStatus;
import com.hospital.queue_management_system.repository.DoctorStatusRepository;

@Service
public class DoctorStatusService {

    private final DoctorStatusRepository repo;

    public DoctorStatusService(
            DoctorStatusRepository repo
    ) {

        this.repo = repo;

    }

    public List<DoctorStatus> getAll() {

        return repo.findAll();

    }

    public DoctorStatus save(
            DoctorStatus status
    ) {

        Optional<DoctorStatus> existing =
                repo.findByDoctorId(
                        status.getDoctorId()
                );

        DoctorStatus doctorStatus;

        if (existing.isPresent()) {

            doctorStatus =
                    existing.get();

            doctorStatus.setArrivalStatus(
                    status.getArrivalStatus()
            );

            doctorStatus.setDelayReason(
                    status.getDelayReason()
            );

        }

        else {

            doctorStatus =
                    new DoctorStatus();

            doctorStatus.setDoctorId(
                    status.getDoctorId()
            );

            doctorStatus.setArrivalStatus(
                    status.getArrivalStatus()
            );

            doctorStatus.setDelayReason(
                    status.getDelayReason()
            );

        }

        doctorStatus.setUpdatedAt(
                new Timestamp(
                        System.currentTimeMillis()
                )
        );

        return repo.save(
                doctorStatus
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