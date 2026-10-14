package com.example.HospitalManagementSystem.DTO;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DoctorRequestDTO {

    @NotBlank(message = "Name is required")
    private String name;

    @Positive
    private Integer age;

    private String gender;

    @NotNull(message = "Department id is required")
    private Integer departmentId;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    private String phone;
    private String address;

    @PositiveOrZero
    private Integer experience;

    @NotBlank(message = "License number is required")
    private String licenseNumber;

    @PositiveOrZero
    private BigDecimal salary;

    @PositiveOrZero
    private BigDecimal consultationFee;
}