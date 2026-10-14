package com.example.HospitalManagementSystem.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "doctors")
@Getter
@Setter
@NoArgsConstructor
public class Doctor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;
    private Integer age;
    private String gender;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @Column(unique = true)
    private String email;

    private String phone;
    private String address;
    private Integer experience;

    @Column(unique = true)
    private String licenseNumber;

    private BigDecimal salary;

    @Column(precision = 10, scale = 2)
    private BigDecimal consultationFee;
}