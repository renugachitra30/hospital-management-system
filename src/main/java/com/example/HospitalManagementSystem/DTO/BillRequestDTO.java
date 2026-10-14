package com.example.HospitalManagementSystem.DTO;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BillRequestDTO {

    @NotNull(message = "Appointment id is required")
    private Integer appointmentId;

    // optional: if not sent, treated as 0
    @PositiveOrZero(message = "Medicine charges cannot be negative")
    private BigDecimal medicineCharges;
}