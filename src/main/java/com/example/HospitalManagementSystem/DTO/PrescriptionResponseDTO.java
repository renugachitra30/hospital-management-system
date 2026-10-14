package com.example.HospitalManagementSystem.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PrescriptionResponseDTO {
    private Integer id;
    private Integer appointmentId;
    private Integer patientId;
    private String patientName;
    private Integer doctorId;
    private String doctorName;
    private String diagnosis;
    private String notes;
    private LocalDateTime createdAt;
    private List<PrescriptionItemResponseDTO> items;
}