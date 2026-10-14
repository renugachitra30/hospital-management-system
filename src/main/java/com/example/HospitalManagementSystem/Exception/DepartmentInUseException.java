package com.example.HospitalManagementSystem.Exception;

public class DepartmentInUseException extends RuntimeException {
    public DepartmentInUseException(String message) {
        super(message);
    }
}
