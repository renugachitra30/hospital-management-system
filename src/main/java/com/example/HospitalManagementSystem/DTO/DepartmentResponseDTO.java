package com.example.HospitalManagementSystem.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DepartmentResponseDTO {
    private Integer id;
    private String name;
    private String description;
    private String location;
    private String phone;
}