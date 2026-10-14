package com.example.HospitalManagementSystem.ServiceImpl;

import com.example.HospitalManagementSystem.DTO.*;
import com.example.HospitalManagementSystem.Entity.Appointment;
import com.example.HospitalManagementSystem.Entity.Prescription;
import com.example.HospitalManagementSystem.Entity.PrescriptionItem;
import com.example.HospitalManagementSystem.Enum.AppointmentStatus;
import com.example.HospitalManagementSystem.Exception.AppointmentNotFoundException;
import com.example.HospitalManagementSystem.Exception.DuplicatePrescriptionException;
import com.example.HospitalManagementSystem.Exception.InvalidAppointmentStateException;
import com.example.HospitalManagementSystem.Exception.PrescriptionNotFoundException;
import com.example.HospitalManagementSystem.Repository.AppointmentRepo;
import com.example.HospitalManagementSystem.Repository.PrescriptionRepo;
import com.example.HospitalManagementSystem.Service.PrescriptionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class PrescriptionServiceImpl implements PrescriptionService {

    private final PrescriptionRepo prescriptionRepo;
    private final AppointmentRepo appointmentRepo;

    public PrescriptionServiceImpl(PrescriptionRepo prescriptionRepo, AppointmentRepo appointmentRepo) {
        this.prescriptionRepo = prescriptionRepo;
        this.appointmentRepo = appointmentRepo;
    }

    @Override
    public PrescriptionResponseDTO findById(int id) {
        return toResponse(findOrThrow(id));
    }

    @Override
    public PrescriptionResponseDTO findByAppointment(int appointmentId) {
        Prescription p = prescriptionRepo.findByAppointmentId(appointmentId).orElseThrow(() -> new PrescriptionNotFoundException("No prescription for appointment: " + appointmentId));
        return toResponse(p);
    }

    @Override
    @Transactional
    public PrescriptionResponseDTO create(PrescriptionRequestDTO dto) {
        Appointment appointment = appointmentRepo.findById(dto.getAppointmentId()).orElseThrow(() -> new AppointmentNotFoundException("Appointment not found: " + dto.getAppointmentId()));

        if (appointment.getStatus() != AppointmentStatus.COMPLETED) {
            throw new InvalidAppointmentStateException("Prescription allowed only for COMPLETED appointments. Current status: " + appointment.getStatus());
        }
        if (prescriptionRepo.existsByAppointmentId(appointment.getId())) {
            throw new DuplicatePrescriptionException("Prescription already exists for appointment: " + appointment.getId());
        }

        Prescription p = new Prescription();
        p.setAppointment(appointment);
        p.setDiagnosis(dto.getDiagnosis());
        p.setNotes(dto.getNotes());
        addItems(p, dto.getItems());

        return toResponse(prescriptionRepo.save(p));   // cascade saves the items
    }

    @Override
    @Transactional
    public PrescriptionResponseDTO update(int id, PrescriptionUpdateDTO dto) {
        Prescription p = findOrThrow(id);
        p.setDiagnosis(dto.getDiagnosis());
        p.setNotes(dto.getNotes());
        p.clearItems();                  // orphanRemoval deletes the old rows
        addItems(p, dto.getItems());     // cascade inserts the new rows
        return toResponse(prescriptionRepo.saveAndFlush(p));  // dirty checking saves at commit
        //try without save and flush, ids would be null
    }

    @Override
    @Transactional
    public void delete(int id) {
        Prescription p = findOrThrow(id);
        prescriptionRepo.delete(p);      // cascade deletes the items too
    }

    // ---------- helpers ----------

    private Prescription findOrThrow(int id) {
        return prescriptionRepo.findById(id).orElseThrow(() -> new PrescriptionNotFoundException("Prescription not found: " + id));
    }

    private void addItems(Prescription p, List<PrescriptionItemRequestDTO> dtos) {
        for (PrescriptionItemRequestDTO i : dtos) {
            PrescriptionItem item = new PrescriptionItem();
            item.setMedicineName(i.getMedicineName());
            item.setDosage(i.getDosage());
            item.setFrequency(i.getFrequency());
            item.setDurationDays(i.getDurationDays());
            item.setInstructions(i.getInstructions());
            p.addItem(item);             // sets both sides of the relationship
        }
    }

    private PrescriptionResponseDTO toResponse(Prescription p) {
        Appointment a = p.getAppointment();

        List<PrescriptionItemResponseDTO> items = p.getItems().stream().map(i -> new PrescriptionItemResponseDTO(i.getId(), i.getMedicineName(), i.getDosage(), i.getFrequency(), i.getDurationDays(), i.getInstructions())).collect(Collectors.toList());

        return new PrescriptionResponseDTO(p.getId(), a.getId(), a.getPatient().getId(), a.getPatient().getName(), a.getDoctor().getId(), a.getDoctor().getName(), p.getDiagnosis(), p.getNotes(), p.getCreatedAt(), items);
    }
}