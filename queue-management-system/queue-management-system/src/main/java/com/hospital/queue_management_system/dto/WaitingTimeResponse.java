package com.hospital.queue_management_system.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WaitingTimeResponse {

    @JsonProperty("estimated_waiting_time")
    private double estimatedWaitingTime;

    public WaitingTimeResponse() {
    }

    public WaitingTimeResponse(double estimatedWaitingTime) {
        this.estimatedWaitingTime = estimatedWaitingTime;
    }
}