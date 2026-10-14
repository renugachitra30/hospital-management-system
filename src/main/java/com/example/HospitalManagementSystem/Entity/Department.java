package com.example.HospitalManagementSystem.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "departments")
@Getter
@Setter
@NoArgsConstructor
public class Department {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    private String description;
    private String location;
    private String phone;

    // inverse side: the FK lives in Doctor.department
    @OneToMany(mappedBy = "department")
    private List<Doctor> doctors = new ArrayList<>();
}