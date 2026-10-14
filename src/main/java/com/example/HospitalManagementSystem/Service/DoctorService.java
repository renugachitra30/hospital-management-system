package com.example.HospitalManagementSystem.Service;

import com.example.HospitalManagementSystem.DTO.DoctorRequestDTO;
import com.example.HospitalManagementSystem.DTO.DoctorResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface DoctorService {
    DoctorResponseDTO findDoctorById(int id);

    Page<DoctorResponseDTO> findAll(Pageable pageable);

    List<DoctorResponseDTO> findByNameContainingIgnoreCase(
            String name);

    Page<DoctorResponseDTO> searchDoctors(
            String name,
            String specialization,
            Integer minExperience,
            Integer maxExperience,
            Pageable pageable);

    DoctorResponseDTO saveDoctor(
            DoctorRequestDTO doctorRequestDTO);

    List<DoctorResponseDTO> saveAllDoctors(
            List<DoctorRequestDTO> doctorRequestDTO);

    DoctorResponseDTO updateDoctor(
            int id,
            DoctorRequestDTO doctorRequestDTO);

    void deleteDoctorById(int id);

    void deleteAllDoctors();
}