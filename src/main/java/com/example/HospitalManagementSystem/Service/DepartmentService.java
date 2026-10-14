package com.example.HospitalManagementSystem.Service;

import com.example.HospitalManagementSystem.DTO.DepartmentRequestDTO;
import com.example.HospitalManagementSystem.DTO.DepartmentResponseDTO;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface DepartmentService {
    DepartmentResponseDTO findById(int id);

    Page<DepartmentResponseDTO> findAll(String name, Pageable pageable);

    DepartmentResponseDTO create(DepartmentRequestDTO dto);

    List<DepartmentResponseDTO> createAll(List<@Valid DepartmentRequestDTO> dtos);

    DepartmentResponseDTO update(int id, DepartmentRequestDTO dto);

    void delete(int id);
}