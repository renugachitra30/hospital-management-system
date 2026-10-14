package com.example.HospitalManagementSystem.Service;

import com.example.HospitalManagementSystem.DTO.AppointmentRequestDTO;
import com.example.HospitalManagementSystem.DTO.AppointmentRescheduleDTO;
import com.example.HospitalManagementSystem.DTO.AppointmentResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AppointmentService {
    AppointmentResponseDTO book(AppointmentRequestDTO dto);

    AppointmentResponseDTO findById(int id);

    Page<AppointmentResponseDTO> findAll(Pageable pageable);

    Page<AppointmentResponseDTO> findByDoctor(int doctorId, Pageable pageable);

    Page<AppointmentResponseDTO> findByPatient(int patientId, Pageable pageable);

    AppointmentResponseDTO cancel(int id);

    AppointmentResponseDTO complete(int id);

    AppointmentResponseDTO reschedule(int id, AppointmentRescheduleDTO dto);
}