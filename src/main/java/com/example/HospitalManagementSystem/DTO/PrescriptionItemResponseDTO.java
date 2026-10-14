package com.example.HospitalManagementSystem.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PrescriptionItemResponseDTO {
    private Integer id;
    private String medicineName;
    private String dosage;
    private String frequency;
    private Integer durationDays;
    private String instructions;
}