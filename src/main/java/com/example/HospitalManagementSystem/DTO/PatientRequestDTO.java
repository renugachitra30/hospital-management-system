package com.example.HospitalManagementSystem.DTO;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PatientRequestDTO {

    @NotNull(message="name is required")
    private String name;

    @NotNull
    @Min(value=0, message ="age should be above 0")
    @Max(value=120, message ="age should be below 120")
    private Integer age;

    private char gender;

    @Email
    private String email;

    private String address;

    @NotNull
    private String phone;

    @NotEmpty
    private String bloodGroup;
}
