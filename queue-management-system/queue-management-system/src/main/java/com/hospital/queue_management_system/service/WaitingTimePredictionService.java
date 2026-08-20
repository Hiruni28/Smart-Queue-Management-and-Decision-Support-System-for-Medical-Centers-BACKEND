package com.hospital.queue_management_system.service;

import com.hospital.queue_management_system.dto.WaitingTimeRequest;
import com.hospital.queue_management_system.dto.WaitingTimeResponse;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
public class WaitingTimePredictionService {

    private static final String API_URL =
            "http://127.0.0.1:8000/predict";

    private final RestTemplate restTemplate;

    public WaitingTimePredictionService(
            RestTemplate restTemplate
    ) {
        this.restTemplate = restTemplate;
    }

    public double predictWaitingTime(
            WaitingTimeRequest request
    ) {

        if (request == null) {
            throw new RuntimeException(
                    "Waiting time request is required."
            );
        }

        try {

            WaitingTimeResponse response =
                    restTemplate.postForObject(
                            API_URL,
                            request,
                            WaitingTimeResponse.class
                    );

            if (response == null) {

                throw new RuntimeException(
                        "Waiting time API returned an empty response."
                );
            }

            double result =
                    response.getEstimatedWaitingTime();

            if (Double.isNaN(result)
                    || Double.isInfinite(result)) {

                throw new RuntimeException(
                        "Waiting time API returned an invalid value."
                );
            }

            return Math.max(result, 0);

        } catch (RestClientException e) {

            throw new RuntimeException(
                    "Waiting time prediction API is unavailable: "
                            + e.getMessage(),
                    e
            );

        } catch (RuntimeException e) {

            throw e;

        } catch (Exception e) {

            throw new RuntimeException(
                    "Waiting time prediction failed: "
                            + e.getMessage(),
                    e
            );
        }
    }
}