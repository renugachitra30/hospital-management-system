package com.example.HospitalManagementSystem.Exception;

import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(PatientNotFoundException.class)
    public ResponseEntity<ErrorResponse> handlePatientNotFound(
            PatientNotFoundException exception) {
        ErrorResponse errorResponse = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.NOT_FOUND.value(),
                "NOT_FOUND",
                exception.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    // 404
    @ExceptionHandler({
            DoctorNotFoundException.class,
            DepartmentNotFoundException.class,
            AppointmentNotFoundException.class,
            PrescriptionNotFoundException.class,
            BillNotFoundException.class})
    public ResponseEntity<Map<String, String>> notFound(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", ex.getMessage()));
    }

    // 409: our own business-rule checks
    @ExceptionHandler({
            DuplicateDoctorException.class,
            DuplicateDepartmentException.class,
            DepartmentInUseException.class,
            AppointmentConflictException.class,
            InvalidAppointmentStateException.class,
            DuplicatePrescriptionException.class,
            DuplicateBillException.class,
            InvalidBillStateException.class})
    public ResponseEntity<Map<String, String>> conflict(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("error", ex.getMessage()));
    }

    // 409: two requests changed the same bill at the same time (@Version)
    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<Map<String, String>> optimisticLock(
            ObjectOptimisticLockingFailureException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("error", "The record was changed by someone else. Please retry."));
    }

    // 409: database unique / foreign-key violation (race conditions)
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, String>> dataIntegrity(DataIntegrityViolationException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("error", "Data conflicts with an existing record"));
    }

    // 400
    @ExceptionHandler(InvalidAppointmentTimeException.class)
    public ResponseEntity<Map<String, String>> invalidTime(InvalidAppointmentTimeException ex) {
        return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> invalid(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(e -> errors.put(e.getField(), e.getDefaultMessage()));
        return ResponseEntity.badRequest().body(errors);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, String>> constraintViolation(ConstraintViolationException ex) {
        return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
    }
}