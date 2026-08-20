package com.hospital.queue_management_system.dto;

public class PeakHourDTO {

    private String hour;
    private long appointmentCount;

    public PeakHourDTO() {
    }

    public PeakHourDTO(
            String hour,
            long appointmentCount
    ) {
        this.hour = hour;
        this.appointmentCount = appointmentCount;
    }

    public String getHour() {
        return hour;
    }

    public void setHour(String hour) {
        this.hour = hour;
    }

    public long getAppointmentCount() {
        return appointmentCount;
    }

    public void setAppointmentCount(long appointmentCount) {
        this.appointmentCount = appointmentCount;
    }
}