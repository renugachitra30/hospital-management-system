package com.example.HospitalManagementSystem.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "prescription_items")
@Getter
@Setter
@NoArgsConstructor
public class PrescriptionItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "prescription_id", nullable = false)
    private Prescription prescription;

    @Column(nullable = false)
    private String medicineName;

    private String dosage;          // e.g. "500 mg"
    private String frequency;       // e.g. "Twice a day"
    private Integer durationDays;   // e.g. 5
    private String instructions;    // e.g. "After food"
}