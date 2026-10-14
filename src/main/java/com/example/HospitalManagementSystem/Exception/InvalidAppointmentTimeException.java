package com.example.HospitalManagementSystem.Exception;

public class InvalidAppointmentTimeException
        extends RuntimeException {

    public InvalidAppointmentTimeException(String message) {
        super(message);
    }
}