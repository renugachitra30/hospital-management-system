package com.example.HospitalManagementSystem.Exception;

public class DuplicatePrescriptionException extends RuntimeException {
    public DuplicatePrescriptionException(String message) {
        super(message);
    }
}
