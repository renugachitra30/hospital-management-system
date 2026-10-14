package com.example.HospitalManagementSystem.DTO;

import com.example.HospitalManagementSystem.Enum.BillStatus;
import com.example.HospitalManagementSystem.Enum.PaymentMethod;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BillResponseDTO {
    private Integer id;
    private Integer appointmentId;
    private Integer patientId;
    private String patientName;
    private Integer doctorId;
    private String doctorName;
    private BigDecimal consultationFee;
    private BigDecimal medicineCharges;
    private BigDecimal totalAmount;
    private BillStatus status;
    private PaymentMethod paymentMethod;
    private LocalDateTime paidAt;
    private LocalDateTime createdAt;
}