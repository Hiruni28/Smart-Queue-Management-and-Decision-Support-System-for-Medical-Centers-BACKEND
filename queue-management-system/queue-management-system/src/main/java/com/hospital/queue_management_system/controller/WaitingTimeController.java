package com.hospital.queue_management_system.controller;

import com.hospital.queue_management_system.dto.WaitingTimeRequest;
import com.hospital.queue_management_system.service.WaitingTimePredictionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/ai")
public class WaitingTimeController {

    @Autowired
    private WaitingTimePredictionService service;

    @PostMapping("/predict")
    public double predict(@RequestBody WaitingTimeRequest request) {

        return service.predictWaitingTime(request);

    }

}