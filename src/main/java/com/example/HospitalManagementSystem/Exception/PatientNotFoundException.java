package com.example.HospitalManagementSystem.Exception;

public class PatientNotFoundException extends RuntimeException {
    public PatientNotFoundException() {
    }
    public PatientNotFoundException(String message) {
        super(message);
    }
}
