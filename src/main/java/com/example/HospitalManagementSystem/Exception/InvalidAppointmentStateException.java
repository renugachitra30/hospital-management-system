package com.example.HospitalManagementSystem.Exception;

public class InvalidAppointmentStateException extends RuntimeException {
    public InvalidAppointmentStateException(String message) {
        super(message);
    }
}
