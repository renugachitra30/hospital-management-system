package com.example.HospitalManagementSystem.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DepartmentRequestDTO {

    @NotBlank(message = "Department name is required")
    @Size(max = 100)
    private String name;

    private String description;
    private String location;
    private String phone;
}