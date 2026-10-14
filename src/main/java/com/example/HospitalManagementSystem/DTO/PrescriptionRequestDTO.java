package com.example.HospitalManagementSystem.DTO;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PrescriptionRequestDTO {

    @NotNull(message = "Appointment id is required")
    private Integer appointmentId;

    @NotBlank(message = "Diagnosis is required")
    private String diagnosis;

    private String notes;

    @NotEmpty(message = "At least one medicine is required")
    @Valid                      // validates each item inside the list
    private List<PrescriptionItemRequestDTO> items;
}