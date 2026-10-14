package com.example.HospitalManagementSystem.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PatientResponseDTO {

    private Integer id;
    private String name;
    private Integer age;
    private char gender;
    private String email;
    private String address;
    private String phone;
    private String bloodGroup;
}
