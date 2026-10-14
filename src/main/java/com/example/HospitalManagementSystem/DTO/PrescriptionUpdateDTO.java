package com.example.HospitalManagementSystem.DTO;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PrescriptionUpdateDTO {

    @NotBlank(message = "Diagnosis is required")
    private String diagnosis;

    private String notes;

    @NotEmpty(message = "At least one medicine is required")
    @Valid
    private List<PrescriptionItemRequestDTO> items;
}