package com.hospital.queue_management_system.service;

import java.util.*;

import org.springframework.stereotype.Service;

import com.hospital.queue_management_system.dto.LoginRequest;
import com.hospital.queue_management_system.model.Staff;
import com.hospital.queue_management_system.repository.StaffRepository;

@Service

public class StaffService {

    private final StaffRepository repo;

    public StaffService(
            StaffRepository repo
    ){
        this.repo=repo;
    }

    public List<Staff> getAll(){
        return repo.findAll();
    }

    public Staff save(
            Staff staff
    ){
        return repo.save(staff);
    }

    public void delete(
            Long id
    ){
        repo.deleteById(id);
    }

    public boolean login(
            LoginRequest request
    ){

        return repo
                .findByEmail(
                        request.getEmail()
                )

                .map(

                        staff->

                                staff
                                        .getPassword()
                                        .equals(
                                                request.getPassword()
                                        )

                )

                .orElse(false);

    }

}