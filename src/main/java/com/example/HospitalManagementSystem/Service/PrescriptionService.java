package com.example.HospitalManagementSystem.Service;

import com.example.HospitalManagementSystem.DTO.PrescriptionRequestDTO;
import com.example.HospitalManagementSystem.DTO.PrescriptionResponseDTO;
import com.example.HospitalManagementSystem.DTO.PrescriptionUpdateDTO;

public interface PrescriptionService {
    PrescriptionResponseDTO create(PrescriptionRequestDTO dto);

    PrescriptionResponseDTO findById(int id);

    PrescriptionResponseDTO findByAppointment(int appointmentId);

    PrescriptionResponseDTO update(int id, PrescriptionUpdateDTO dto);

    void delete(int id);
}