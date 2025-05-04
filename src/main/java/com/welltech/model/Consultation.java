package com.welltech.model; // Correct package for models

import java.time.LocalDateTime;
import java.util.Objects;

public class Consultation {

    private int id;
    private User patient;
    private User psychiatrist;
    private LocalDateTime consultationTime;
    private int durationMinutes;
    private String purpose;
    private String notes;
    private ConsultationStatus status;

    // --- Nested Enum for Consultation Status ---
    public enum ConsultationStatus {
        SCHEDULED("Scheduled"),
        COMPLETED("Completed"),
        CANCELLED("Cancelled"),
        RESCHEDULED("Rescheduled"); // Optional: if you need this state

        private final String displayValue;

        ConsultationStatus(String displayValue) {
            this.displayValue = displayValue;
        }

        public String getDisplayValue() {
            return displayValue;
        }

        // Helper method to get enum from string value
        public static ConsultationStatus fromDisplayValue(String value) {
            for (ConsultationStatus status : values()) {
                if (status.displayValue.equalsIgnoreCase(value)) {
                    return status;
                }
            }
            // Handle cases where value doesn't match
            return null;
        }

        @Override
        public String toString() {
            return displayValue; // For ComboBox display
        }
    }
    // --- End Enum ---

    // Default constructor
    public Consultation() {
    }

    // Constructor for creating a new consultation
    public Consultation(User patient, User psychiatrist, LocalDateTime consultationTime, int durationMinutes, String purpose, String notes, ConsultationStatus status) {
        this.patient = patient;
        this.psychiatrist = psychiatrist;
        this.consultationTime = consultationTime;
        this.durationMinutes = durationMinutes;
        this.purpose = purpose;
        this.notes = notes;
        this.status = status;
    }

    // Constructor for fetching from database
    public Consultation(int id, User patient, User psychiatrist, LocalDateTime consultationTime, int durationMinutes, String purpose, String notes, ConsultationStatus status) {
        this.id = id;
        this.patient = patient;
        this.psychiatrist = psychiatrist;
        this.consultationTime = consultationTime;
        this.durationMinutes = durationMinutes;
        this.purpose = purpose;
        this.notes = notes;
        this.status = status;
    }

    // Getters and Setters (Keep as is)
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public User getPatient() { return patient; }
    public void setPatient(User patient) { this.patient = patient; }
    public User getPsychiatrist() { return psychiatrist; }
    public void setPsychiatrist(User psychiatrist) { this.psychiatrist = psychiatrist; }
    public LocalDateTime getConsultationTime() { return consultationTime; }
    public void setConsultationTime(LocalDateTime consultationTime) { this.consultationTime = consultationTime; }
    public int getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(int durationMinutes) { this.durationMinutes = durationMinutes; }
    public String getPurpose() { return purpose; }
    public void setPurpose(String purpose) { this.purpose = purpose; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public ConsultationStatus getStatus() { return status; }
    public void setStatus(ConsultationStatus status) { this.status = status; }

    // Helper getters for TableView/Card display
    public String getPatientName() { return patient != null ? patient.getFullName() : "N/A"; }
    public String getPsychiatristName() { return psychiatrist != null ? psychiatrist.getFullName() : "N/A"; }
    public String getStatusDisplayValue() { return status != null ? status.getDisplayValue() : "N/A"; }

    // equals and hashCode (Keep as is)
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Consultation that = (Consultation) o;
        return id == that.id;
    }
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    // toString (Keep as is)
    @Override
    public String toString() {
        return "Consultation{" +
                "id=" + id +
                ", patient=" + (patient != null ? patient.getFullName() : "null") +
                ", psychiatrist=" + (psychiatrist != null ? psychiatrist.getFullName() : "null") +
                ", time=" + consultationTime +
                ", status=" + (status != null ? status.getDisplayValue() : "null") +
                '}';
    }
}