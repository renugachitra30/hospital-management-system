package com.example.HospitalManagementSystem.DTO;

import com.example.HospitalManagementSystem.Enum.AppointmentStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AppointmentResponseDTO {
    private Integer id;
    private Integer patientId;
    private String patientName;
    private Integer doctorId;
    private String doctorName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private AppointmentStatus status;
    private String reason;
}