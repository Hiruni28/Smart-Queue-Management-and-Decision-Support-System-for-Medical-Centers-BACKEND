package com.hospital.queue_management_system.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WaitingTimeRequest {

    @JsonProperty("AgeGroup")
    private String ageGroup;

    @JsonProperty("Department")
    private String department;

    @JsonProperty("AppointmentType")
    private String appointmentType;

    @JsonProperty("InsuranceType")
    private String insuranceType;

    @JsonProperty("ArrivalMethod")
    private String arrivalMethod;

    @JsonProperty("TriageCategory")
    private String triageCategory;

    @JsonProperty("ReasonForVisit")
    private String reasonForVisit;

    @JsonProperty("ConsultationNeeded")
    private boolean consultationNeeded;

    @JsonProperty("FacilityOccupancyRate")
    private double facilityOccupancyRate;

    @JsonProperty("ProvidersOnShift")
    private int providersOnShift;

    @JsonProperty("NursesOnShift")
    private int nursesOnShift;

    @JsonProperty("PatientsWaiting")
    private int patientsWaiting;

    @JsonProperty("AverageServiceTimeMinutes")
    private double averageServiceTimeMinutes;

    @JsonProperty("AverageWaitTimeMinutes")
    private double averageWaitTimeMinutes;

    @JsonProperty("StaffToPatientRatio")
    private double staffToPatientRatio;

    @JsonProperty("IsRegistered")
    private int isRegistered;

    @JsonProperty("IsOnlineBooking")
    private int isOnlineBooking;

    @JsonProperty("BookingType")
    private String bookingType;

    @JsonProperty("Year")
    private int year;

    @JsonProperty("Quarter")
    private int quarter;

    @JsonProperty("ArrivalHour")
    private int arrivalHour;

    @JsonProperty("ArrivalDayOfWeek")
    private int arrivalDayOfWeek;

    @JsonProperty("ArrivalMonth")
    private int arrivalMonth;

    @JsonProperty("IsWeekend")
    private boolean isWeekend;

    public boolean isWeekend() {
        return isWeekend;
    }

    public void setIsWeekend(boolean isWeekend) {
        this.isWeekend = isWeekend;
    }
}