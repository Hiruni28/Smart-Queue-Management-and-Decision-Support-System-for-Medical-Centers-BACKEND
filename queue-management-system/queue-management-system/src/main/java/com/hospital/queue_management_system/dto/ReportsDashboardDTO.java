package com.hospital.queue_management_system.dto;

import java.util.List;
import java.util.Map;

public class ReportsDashboardDTO {

    private long totalAppointments;

    private long bookedAppointments;
    private long confirmedAppointments;
    private long waitingAppointments;
    private long servingAppointments;
    private long completedAppointments;
    private long cancelledAppointments;
    private long skippedAppointments;

    private double averageWaitingTime;
    private double predictedWaitingTime;

    private List<PeakHourDTO> peakHours;

    private String busiestHour;
    private long busiestHourAppointmentCount;

    private String decisionSupportMessage;

    private Map<String, Long> statusSummary;

    public ReportsDashboardDTO() {
    }

    public long getTotalAppointments() {
        return totalAppointments;
    }

    public void setTotalAppointments(long totalAppointments) {
        this.totalAppointments = totalAppointments;
    }

    public long getBookedAppointments() {
        return bookedAppointments;
    }

    public void setBookedAppointments(long bookedAppointments) {
        this.bookedAppointments = bookedAppointments;
    }

    public long getConfirmedAppointments() {
        return confirmedAppointments;
    }

    public void setConfirmedAppointments(long confirmedAppointments) {
        this.confirmedAppointments = confirmedAppointments;
    }

    public long getWaitingAppointments() {
        return waitingAppointments;
    }

    public void setWaitingAppointments(long waitingAppointments) {
        this.waitingAppointments = waitingAppointments;
    }

    public long getServingAppointments() {
        return servingAppointments;
    }

    public void setServingAppointments(long servingAppointments) {
        this.servingAppointments = servingAppointments;
    }

    public long getCompletedAppointments() {
        return completedAppointments;
    }

    public void setCompletedAppointments(long completedAppointments) {
        this.completedAppointments = completedAppointments;
    }

    public long getCancelledAppointments() {
        return cancelledAppointments;
    }

    public void setCancelledAppointments(long cancelledAppointments) {
        this.cancelledAppointments = cancelledAppointments;
    }

    public long getSkippedAppointments() {
        return skippedAppointments;
    }

    public void setSkippedAppointments(long skippedAppointments) {
        this.skippedAppointments = skippedAppointments;
    }

    public double getAverageWaitingTime() {
        return averageWaitingTime;
    }

    public void setAverageWaitingTime(double averageWaitingTime) {
        this.averageWaitingTime = averageWaitingTime;
    }

    public double getPredictedWaitingTime() {
        return predictedWaitingTime;
    }

    public void setPredictedWaitingTime(double predictedWaitingTime) {
        this.predictedWaitingTime = predictedWaitingTime;
    }

    public List<PeakHourDTO> getPeakHours() {
        return peakHours;
    }

    public void setPeakHours(List<PeakHourDTO> peakHours) {
        this.peakHours = peakHours;
    }

    public String getBusiestHour() {
        return busiestHour;
    }

    public void setBusiestHour(String busiestHour) {
        this.busiestHour = busiestHour;
    }

    public long getBusiestHourAppointmentCount() {
        return busiestHourAppointmentCount;
    }

    public void setBusiestHourAppointmentCount(
            long busiestHourAppointmentCount
    ) {
        this.busiestHourAppointmentCount =
                busiestHourAppointmentCount;
    }

    public String getDecisionSupportMessage() {
        return decisionSupportMessage;
    }

    public void setDecisionSupportMessage(
            String decisionSupportMessage
    ) {
        this.decisionSupportMessage =
                decisionSupportMessage;
    }

    public Map<String, Long> getStatusSummary() {
        return statusSummary;
    }

    public void setStatusSummary(
            Map<String, Long> statusSummary
    ) {
        this.statusSummary = statusSummary;
    }
}