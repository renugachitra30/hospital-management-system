package com.example.HospitalManagementSystem.Exception;

public class InvalidBillStateException extends RuntimeException {
    public InvalidBillStateException(String message) {
        super(message);
    }
}
