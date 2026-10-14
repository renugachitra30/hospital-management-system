package com.example.HospitalManagementSystem.Service;

import com.example.HospitalManagementSystem.DTO.BillPaymentDTO;
import com.example.HospitalManagementSystem.DTO.BillRequestDTO;
import com.example.HospitalManagementSystem.DTO.BillResponseDTO;
import com.example.HospitalManagementSystem.Enum.BillStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BillService {
    BillResponseDTO generate(BillRequestDTO dto);

    BillResponseDTO findById(int id);

    BillResponseDTO findByAppointment(int appointmentId);

    Page<BillResponseDTO> findAll(BillStatus status, Pageable pageable);

    BillResponseDTO pay(int id, BillPaymentDTO dto);

    BillResponseDTO cancel(int id);
}