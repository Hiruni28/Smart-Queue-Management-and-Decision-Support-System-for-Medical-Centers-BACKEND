package com.hospital.queue_management_system.service;

import java.util.List;
import java.util.Optional;
import java.time.LocalDate;
import java.time.Period;

import org.springframework.stereotype.Service;

import com.hospital.queue_management_system.model.Patient;
import com.hospital.queue_management_system.repository.PatientRepository;

@Service
public class PatientService {

    private final PatientRepository repo;

    public PatientService(PatientRepository repo) {
        this.repo = repo;
    }

    public List<Patient> getAll() {
        return repo.findAll();
    }

    public String register(
            Patient patient
    ) {

        if (patient == null) {

            return "Patient data is required";
        }

        if (patient.getDateOfBirth() != null) {

            LocalDate dateOfBirth =
                    patient.getDateOfBirth()
                            .toLocalDate();

            if (dateOfBirth.isAfter(
                    LocalDate.now()
            )) {

                return "Date of birth cannot be in the future";
            }
        }

        if (patient.getEmail() != null
                && repo.findByEmail(
                patient.getEmail()
        ).isPresent()) {

            return "Email already exists";
        }

        // Default special needs to false
        if (patient.getSpecialNeeds() == null) {

            patient.setSpecialNeeds(false);
        }

        if (!Boolean.TRUE.equals(
                patient.getSpecialNeeds()
        )) {

            patient.setDisabilityType(null);
        }

        repo.save(patient);

        return "Registration Success";
    }

    public String login(
            String email,
            String password
    ) {

        Optional<Patient> patient =
                repo.findByEmail(email);

        if (patient.isPresent()
                && patient.get()
                .getPassword()
                .equals(password)) {

            return "Login Success";
        }

        return "Invalid Credentials";
    }

    public Patient getProfile(String email) {

        return repo
                .findByEmail(email)
                .orElse(null);
    }

    // UPDATED METHOD
    public Patient updateProfile(Patient updatedPatient) {

        if (updatedPatient == null) {
            throw new RuntimeException(
                    "Patient data is required."
            );
        }

        if (updatedPatient.getPatientId() == null) {
            throw new RuntimeException(
                    "Patient ID is required."
            );
        }

        Patient existingPatient = repo
                .findById(updatedPatient.getPatientId())
                .orElseThrow(() ->
                        new RuntimeException(
                                "Patient not found."
                        )
                );

        // Update full name
        if (updatedPatient.getFullName() != null
                && !updatedPatient.getFullName()
                .trim()
                .isEmpty()) {

            existingPatient.setFullName(
                    updatedPatient.getFullName().trim()
            );
        }

        // Update phone
        if (updatedPatient.getPhone() != null
                && !updatedPatient.getPhone()
                .trim()
                .isEmpty()) {

            existingPatient.setPhone(
                    updatedPatient.getPhone().trim()
            );
        }

        // Update date of birth
        if (updatedPatient.getDateOfBirth() != null) {

            existingPatient.setDateOfBirth(
                    updatedPatient.getDateOfBirth()
            );
        }

        // Update password only if entered
        if (updatedPatient.getPassword() != null
                && !updatedPatient.getPassword()
                .trim()
                .isEmpty()) {

            existingPatient.setPassword(
                    updatedPatient.getPassword()
            );
        }

        // Email is intentionally not updated.
        // PatientProfile.jsx treats email as read-only.

        return repo.save(existingPatient);
    }

    public List<Patient> search(String keyword) {

        try {

            Long id =
                    Long.parseLong(keyword);

            return repo.findById(id)
                    .map(List::of)
                    .orElse(List.of());

        } catch (Exception ignored) {
        }

        List<Patient> byName =
                repo.findByFullNameContainingIgnoreCase(
                        keyword
                );

        if (!byName.isEmpty()) {
            return byName;
        }

        return repo.findByPhoneContaining(
                keyword
        );
    }

    // =========================================================
// CALCULATE AGE
// =========================================================

    public Integer calculateAge(
            Patient patient
    ) {

        if (patient == null
                || patient.getDateOfBirth() == null) {

            return null;
        }

        LocalDate dateOfBirth =
                patient.getDateOfBirth()
                        .toLocalDate();

        LocalDate today =
                LocalDate.now();

        if (dateOfBirth.isAfter(today)) {

            throw new RuntimeException(
                    "Date of birth cannot be in the future."
            );
        }

        return Period.between(
                dateOfBirth,
                today
        ).getYears();
    }


// =========================================================
// CALCULATE PATIENT PRIORITY
// =========================================================

    public String calculatePatientPriority(
            Patient patient
    ) {

        if (patient == null) {

            return "Normal";
        }

        // ------------------------------------------
        // SPECIAL NEEDS → ORDER 2
        // ------------------------------------------

        if (Boolean.TRUE.equals(
                patient.getSpecialNeeds()
        )) {

            return "Special Needs";
        }

        // ------------------------------------------
        // AGE-BASED PRIORITY
        // ------------------------------------------

        Integer age =
                calculateAge(patient);

        if (age == null) {

            return "Normal";
        }

        // ------------------------------------------
        // ELDERLY → 65+
        // ------------------------------------------

        if (age >= 65) {

            return "Elderly";
        }

        // ------------------------------------------
        // CHILD → UNDER 5
        // ------------------------------------------

        if (age < 5) {

            return "Child";
        }

        // ------------------------------------------
        // NORMAL
        // ------------------------------------------

        return "Normal";
    }
}