package com.example.HospitalManagementSystem.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PrescriptionItemRequestDTO {

    @NotBlank(message = "Medicine name is required")
    private String medicineName;

    private String dosage;

    private String frequency;

    @Positive(message = "Duration must be at least 1 day")
    private Integer durationDays;

    private String instructions;
}