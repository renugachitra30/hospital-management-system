package com.example.HospitalManagementSystem.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DoctorResponseDTO {
    private Integer id;
    private String name;
    private Integer age;
    private String gender;
    private Integer departmentId;
    private String departmentName;
    private String email;
    private String phone;
    private String address;
    private Integer experience;
    private String licenseNumber;
    private BigDecimal salary;
    private BigDecimal consultationFee;
}