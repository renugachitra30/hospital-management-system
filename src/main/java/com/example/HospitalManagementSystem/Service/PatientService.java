package com.example.HospitalManagementSystem.Service;

import com.example.HospitalManagementSystem.DTO.PatientRequestDTO;
import com.example.HospitalManagementSystem.DTO.PatientResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface PatientService {

    PatientResponseDTO findPatientById(int id);

    Page<PatientResponseDTO> findAll(Pageable pageable);

    PatientResponseDTO savePatient(PatientRequestDTO patient);

    List<PatientResponseDTO> saveAllPatients(List<PatientRequestDTO> patients);

    PatientResponseDTO updatePatient(int id, PatientRequestDTO patient);

    void deletePatientById(int id);

    void deleteAllPatients();

    List<PatientResponseDTO> findByNameContainingIgnoreCase(String name);

    List<PatientResponseDTO> findByAgeGreaterThan(int age);

    Page<PatientResponseDTO> searchPatients(String name, String bloodGroup, Integer minAge, Integer maxAge, Pageable pageable);
}
