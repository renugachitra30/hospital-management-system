package com.example.HospitalManagementSystem.Exception;

public class DuplicateBillException extends RuntimeException {
    public DuplicateBillException(String message) {
        super(message);
    }
}
