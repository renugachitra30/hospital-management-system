package com.example.HospitalManagementSystem.DTO;

import com.example.HospitalManagementSystem.Enum.PaymentMethod;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BillPaymentDTO {

    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;
}