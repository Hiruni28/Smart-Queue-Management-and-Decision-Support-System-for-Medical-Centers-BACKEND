package com.hospital.queue_management_system.controller;

import java.util.*;

import org.springframework.web.bind.annotation.*;

import com.hospital.queue_management_system.model.Staff;
import com.hospital.queue_management_system.service.StaffService;

@RestController

@RequestMapping("/staff")

@CrossOrigin(
        origins="http://localhost:5173"
)

public class StaffController {

    private final StaffService service;

    public StaffController(
            StaffService service
    ){

        this.service=service;

    }

    @GetMapping

    public List<Staff> getAll(){

        return service.getAll();

    }

    @PostMapping

    public Staff save(
            @RequestBody
            Staff staff
    ){

        return service.save(
                staff
        );

    }

    @PutMapping

    public Staff update(
            @RequestBody
            Staff staff
    ){

        return service.save(
                staff
        );

    }

    @DeleteMapping("/{id}")

    public void delete(
            @PathVariable Long id
    ){

        service.delete(
                id
        );

    }

}
