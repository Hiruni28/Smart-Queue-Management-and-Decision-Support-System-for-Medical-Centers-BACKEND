package com.hospital.queue_management_system.service;

import java.sql.Time;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.hospital.queue_management_system.dto.PeakHourDTO;
import com.hospital.queue_management_system.dto.ReportsDashboardDTO;
import com.hospital.queue_management_system.model.Appointment;
import com.hospital.queue_management_system.model.QueueToken;
import com.hospital.queue_management_system.repository.AppointmentRepository;
import com.hospital.queue_management_system.repository.QueueTokenRepository;

@Service
public class ReportsService {

    private final AppointmentRepository appointmentRepository;
    private final QueueTokenRepository queueTokenRepository;

    public ReportsService(
            AppointmentRepository appointmentRepository,
            QueueTokenRepository queueTokenRepository
    ) {
        this.appointmentRepository = appointmentRepository;
        this.queueTokenRepository = queueTokenRepository;
    }

    // =========================================================
    // REPORT DASHBOARD
    // =========================================================

    public ReportsDashboardDTO getDashboard() {

        List<Appointment> appointments =
                appointmentRepository.findAll();

        ReportsDashboardDTO dto =
                new ReportsDashboardDTO();

        // =====================================================
        // TOTAL APPOINTMENTS
        // =====================================================

        dto.setTotalAppointments(
                appointments.size()
        );

        // =====================================================
        // STATUS COUNTS
        // =====================================================

        long booked = countStatus(
                appointments,
                "Booked"
        );

        long confirmed = countStatus(
                appointments,
                "Confirmed"
        );

        long waiting = countStatus(
                appointments,
                "Waiting"
        );

        long serving = countStatus(
                appointments,
                "Serving"
        );

        long completed = countStatus(
                appointments,
                "Completed"
        );

        long cancelled = countStatus(
                appointments,
                "Cancelled"
        );

        long skipped = countStatus(
                appointments,
                "Skipped"
        );

        dto.setBookedAppointments(booked);
        dto.setConfirmedAppointments(confirmed);
        dto.setWaitingAppointments(waiting);
        dto.setServingAppointments(serving);
        dto.setCompletedAppointments(completed);
        dto.setCancelledAppointments(cancelled);
        dto.setSkippedAppointments(skipped);

        // =====================================================
        // STATUS SUMMARY
        // =====================================================

        Map<String, Long> statusSummary =
                new HashMap<>();

        statusSummary.put("Booked", booked);
        statusSummary.put("Confirmed", confirmed);
        statusSummary.put("Waiting", waiting);
        statusSummary.put("Serving", serving);
        statusSummary.put("Completed", completed);
        statusSummary.put("Cancelled", cancelled);
        statusSummary.put("Skipped", skipped);

        dto.setStatusSummary(statusSummary);

        // =====================================================
        // PEAK HOURS
        // =====================================================

        Map<Integer, Long> hourlyCounts =
                new HashMap<>();

        for (Appointment appointment : appointments) {

            Time appointmentTime =
                    appointment.getAppointmentTime();

            if (appointmentTime == null) {
                continue;
            }

            int hour =
                    appointmentTime
                            .toLocalTime()
                            .getHour();

            hourlyCounts.put(
                    hour,
                    hourlyCounts.getOrDefault(
                            hour,
                            0L
                    ) + 1
            );
        }

        List<PeakHourDTO> peakHours =
                new ArrayList<>();

        for (Map.Entry<Integer, Long> entry :
                hourlyCounts.entrySet()) {

            int hour = entry.getKey();

            String formattedHour =
                    formatHour(hour);

            peakHours.add(
                    new PeakHourDTO(
                            formattedHour,
                            entry.getValue()
                    )
            );
        }

        peakHours.sort(
                Comparator.comparing(
                        PeakHourDTO::getHour
                )
        );

        dto.setPeakHours(peakHours);

        // =====================================================
        // BUSIEST HOUR
        // =====================================================

        Map.Entry<Integer, Long> busiest =
                hourlyCounts.entrySet()
                        .stream()
                        .max(
                                Map.Entry.comparingByValue()
                        )
                        .orElse(null);

        if (busiest != null) {

            dto.setBusiestHour(
                    formatHour(
                            busiest.getKey()
                    )
            );

            dto.setBusiestHourAppointmentCount(
                    busiest.getValue()
            );

        } else {

            dto.setBusiestHour("No data");
            dto.setBusiestHourAppointmentCount(0);
        }

        // =====================================================
        // WAITING TIME
        // =====================================================

        List<QueueToken> tokens =
                queueTokenRepository.findAll();

        double averageWaiting =
                calculateAverageWaitingTime(
                        tokens
                );

        dto.setAverageWaitingTime(
                round(averageWaiting)
        );

        // =====================================================
        // PREDICTED WAITING TIME
        // =====================================================

        double predictedWaiting =
                calculatePredictedWaitingTime(
                        averageWaiting,
                        waiting,
                        serving,
                        dto.getBusiestHourAppointmentCount()
                );

        dto.setPredictedWaitingTime(
                round(predictedWaiting)
        );

        // =====================================================
        // DECISION SUPPORT
        // =====================================================

        dto.setDecisionSupportMessage(
                generateDecisionSupportMessage(
                        dto
                )
        );

        return dto;
    }

    // =========================================================
    // COUNT STATUS
    // =========================================================

    private long countStatus(
            List<Appointment> appointments,
            String status
    ) {

        return appointments
                .stream()
                .filter(
                        appointment ->
                                appointment.getStatus() != null
                                        && appointment.getStatus()
                                        .equalsIgnoreCase(status)
                )
                .count();
    }

    // =========================================================
    // AVERAGE WAITING TIME
    // =========================================================

    private double calculateAverageWaitingTime(
            List<QueueToken> tokens
    ) {

        if (tokens == null || tokens.isEmpty()) {
            return 0;
        }

        double total = 0;
        int count = 0;

        for (QueueToken token : tokens) {

            if (token == null) {
                continue;
            }

            Integer waitTime =
                    token.getEstimatedWaitTime();

            if (waitTime == null) {
                continue;
            }

            if (waitTime < 0) {
                continue;
            }

            total += waitTime;
            count++;
        }

        if (count == 0) {
            return 0;
        }

        return total / count;
    }

    // =========================================================
    // SIMPLE DATA-DRIVEN PREDICTION
    // =========================================================

    private double calculatePredictedWaitingTime(
            double averageWaiting,
            long waitingAppointments,
            long servingAppointments,
            long busiestHourAppointments
    ) {

        if (averageWaiting <= 0) {
            return 0;
        }

        double prediction =
                averageWaiting;

        /*
         * More patients currently waiting
         * means the expected waiting time increases.
         */
        if (waitingAppointments >= 5) {

            prediction += 5;

        } else if (waitingAppointments >= 3) {

            prediction += 3;

        }

        /*
         * If someone is currently being served,
         * add a small buffer.
         */
        if (servingAppointments > 0) {

            prediction += 2;
        }

        /*
         * Busy periods receive an additional
         * small prediction buffer.
         */
        if (busiestHourAppointments >= 10) {

            prediction += 5;

        } else if (busiestHourAppointments >= 5) {

            prediction += 2;
        }

        return prediction;
    }

    // =========================================================
    // DECISION SUPPORT MESSAGE
    // =========================================================

    private String generateDecisionSupportMessage(
            ReportsDashboardDTO dto
    ) {

        if (dto.getTotalAppointments() == 0) {

            return "No appointment data is currently available.";
        }

        if (dto.getPredictedWaitingTime() >= 30) {

            return "High waiting time predicted. Consider allocating additional staff during peak periods.";
        }

        if (dto.getWaitingAppointments() >= 5) {

            return "Queue demand is currently high. Monitor waiting patients and consider additional service capacity.";
        }

        if (dto.getBusiestHourAppointmentCount() >= 10) {

            return "A high appointment concentration was detected during the busiest hour. Consider increasing staff coverage during this period.";
        }

        return "Queue conditions are currently within a manageable range.";
    }

    // =========================================================
    // FORMAT HOUR
    // =========================================================

    private String formatHour(int hour) {

        LocalTime time =
                LocalTime.of(hour, 0);

        LocalTime nextHour =
                time.plusHours(1);

        return String.format(
                "%02d:00 - %02d:00",
                time.getHour(),
                nextHour.getHour()
        );
    }

    // =========================================================
    // ROUND
    // =========================================================

    private double round(double value) {

        return Math.round(
                value * 100.0
        ) / 100.0;
    }
}