package com.example.HospitalManagementSystem.ServiceImpl;

import com.example.HospitalManagementSystem.DTO.BillPaymentDTO;
import com.example.HospitalManagementSystem.DTO.BillRequestDTO;
import com.example.HospitalManagementSystem.DTO.BillResponseDTO;
import com.example.HospitalManagementSystem.Entity.Appointment;
import com.example.HospitalManagementSystem.Entity.Bill;
import com.example.HospitalManagementSystem.Entity.Doctor;
import com.example.HospitalManagementSystem.Enum.AppointmentStatus;
import com.example.HospitalManagementSystem.Enum.BillStatus;
import com.example.HospitalManagementSystem.Exception.*;
import com.example.HospitalManagementSystem.Repository.AppointmentRepo;
import com.example.HospitalManagementSystem.Repository.BillRepo;
import com.example.HospitalManagementSystem.Service.BillService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@Transactional(readOnly = true)
public class BillServiceImpl implements BillService {

    private final BillRepo billRepo;
    private final AppointmentRepo appointmentRepo;

    public BillServiceImpl(BillRepo billRepo, AppointmentRepo appointmentRepo) {
        this.billRepo = billRepo;
        this.appointmentRepo = appointmentRepo;
    }

    @Override
    public BillResponseDTO findById(int id) {
        return toResponse(findOrThrow(id));
    }

    @Override
    public BillResponseDTO findByAppointment(int appointmentId) {
        Bill bill = billRepo.findByAppointmentId(appointmentId)
                .orElseThrow(() -> new BillNotFoundException(
                        "No bill for appointment: " + appointmentId));
        return toResponse(bill);
    }

    @Override
    public Page<BillResponseDTO> findAll(BillStatus status, Pageable pageable) {
        Page<Bill> page = (status == null)
                ? billRepo.findAll(pageable)
                : billRepo.findByStatus(status, pageable);
        return page.map(this::toResponse);
    }

    @Override
    @Transactional
    public BillResponseDTO generate(BillRequestDTO dto) {
        Appointment appointment = appointmentRepo.findById(dto.getAppointmentId())
                .orElseThrow(() -> new AppointmentNotFoundException(
                        "Appointment not found: " + dto.getAppointmentId()));

        if (appointment.getStatus() != AppointmentStatus.COMPLETED) {
            throw new InvalidAppointmentStateException(
                    "Bill can be generated only for COMPLETED appointments. Current status: "
                            + appointment.getStatus());
        }
        if (billRepo.existsByAppointmentId(appointment.getId())) {
            throw new DuplicateBillException(
                    "Bill already exists for appointment: " + appointment.getId());
        }

        Doctor doctor = appointment.getDoctor();
        if (doctor.getConsultationFee() == null) {
            throw new InvalidBillStateException(
                    "Consultation fee is not set for doctor: " + doctor.getName());
        }

        BigDecimal fee = doctor.getConsultationFee();
        BigDecimal medicine = dto.getMedicineCharges() == null
                ? BigDecimal.ZERO : dto.getMedicineCharges();

        Bill bill = new Bill();
        bill.setAppointment(appointment);
        bill.setConsultationFee(fee);                 // snapshot of today's fee
        bill.setMedicineCharges(medicine);
        bill.setTotalAmount(fee.add(medicine));       // BigDecimal: add(), not +
        bill.setStatus(BillStatus.PENDING);

        return toResponse(billRepo.save(bill));
    }

    @Override
    @Transactional
    public BillResponseDTO pay(int id, BillPaymentDTO dto) {
        Bill bill = findOrThrow(id);
        if (bill.getStatus() != BillStatus.PENDING) {
            throw new InvalidBillStateException(
                    "Only PENDING bills can be paid. Current status: " + bill.getStatus());
        }
        bill.setStatus(BillStatus.PAID);
        bill.setPaymentMethod(dto.getPaymentMethod());
        bill.setPaidAt(LocalDateTime.now());
        return toResponse(bill);     // dirty checking saves at commit
    }

    @Override
    @Transactional
    public BillResponseDTO cancel(int id) {
        Bill bill = findOrThrow(id);
        if (bill.getStatus() != BillStatus.PENDING) {
            throw new InvalidBillStateException(
                    "Only PENDING bills can be cancelled. Current status: " + bill.getStatus());
        }
        bill.setStatus(BillStatus.CANCELLED);
        return toResponse(bill);
    }

    // ---------- helpers ----------

    private Bill findOrThrow(int id) {
        return billRepo.findById(id)
                .orElseThrow(() -> new BillNotFoundException("Bill not found: " + id));
    }

    private BillResponseDTO toResponse(Bill b) {
        Appointment a = b.getAppointment();
        return new BillResponseDTO(
                b.getId(),
                a.getId(),
                a.getPatient().getId(),
                a.getPatient().getName(),
                a.getDoctor().getId(),
                a.getDoctor().getName(),
                b.getConsultationFee(),
                b.getMedicineCharges(),
                b.getTotalAmount(),
                b.getStatus(),
                b.getPaymentMethod(),
                b.getPaidAt(),
                b.getCreatedAt());
    }
}