package com.example.HospitalManagementSystem.ServiceImpl;

import com.example.HospitalManagementSystem.DTO.AppointmentRequestDTO;
import com.example.HospitalManagementSystem.DTO.AppointmentRescheduleDTO;
import com.example.HospitalManagementSystem.DTO.AppointmentResponseDTO;
import com.example.HospitalManagementSystem.Entity.Appointment;
import com.example.HospitalManagementSystem.Entity.Doctor;
import com.example.HospitalManagementSystem.Entity.Patient;
import com.example.HospitalManagementSystem.Enum.AppointmentStatus;
import com.example.HospitalManagementSystem.Exception.*;
import com.example.HospitalManagementSystem.Repository.AppointmentRepo;
import com.example.HospitalManagementSystem.Repository.DoctorRepo;
import com.example.HospitalManagementSystem.Repository.PatientRepo;
import com.example.HospitalManagementSystem.Service.AppointmentService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional(readOnly = true)
public class AppointmentServiceImpl implements AppointmentService {

    private final AppointmentRepo appointmentRepo;
    private final DoctorRepo doctorRepo;
    private final PatientRepo patientRepo;

    public AppointmentServiceImpl(AppointmentRepo appointmentRepo,
                                  DoctorRepo doctorRepo,
                                  PatientRepo patientRepo) {
        this.appointmentRepo = appointmentRepo;
        this.doctorRepo = doctorRepo;
        this.patientRepo = patientRepo;
    }

    @Override
    public AppointmentResponseDTO findById(int id) {
        return toResponse(findOrThrow(id));
    }

    @Override
    public Page<AppointmentResponseDTO> findAll(Pageable pageable) {
        return appointmentRepo.findAll(pageable).map(this::toResponse);
    }

    @Override
    public Page<AppointmentResponseDTO> findByDoctor(int doctorId, Pageable pageable) {
        return appointmentRepo.findByDoctorId(doctorId, pageable).map(this::toResponse);
    }

    @Override
    public Page<AppointmentResponseDTO> findByPatient(int patientId, Pageable pageable) {
        return appointmentRepo.findByPatientId(patientId, pageable).map(this::toResponse);
    }

    @Override
    @Transactional
    public AppointmentResponseDTO book(AppointmentRequestDTO dto) {
        validateTimes(dto.getStartTime(), dto.getEndTime());

        // lock the doctor row: bookings for the same doctor now run one at a time
        Doctor doctor = doctorRepo.findByIdForUpdate(dto.getDoctorId())
                .orElseThrow(() -> new DoctorNotFoundException(
                        "Doctor not found: " + dto.getDoctorId()));
        Patient patient = patientRepo.findById(dto.getPatientId())
                .orElseThrow(() -> new PatientNotFoundException(
                        "Patient not found: " + dto.getPatientId()));

        checkNoOverlap(doctor.getId(), patient.getId(),
                dto.getStartTime(), dto.getEndTime(), null);

        Appointment a = new Appointment();
        a.setDoctor(doctor);
        a.setPatient(patient);
        a.setStartTime(dto.getStartTime());
        a.setEndTime(dto.getEndTime());
        a.setReason(dto.getReason());
        a.setStatus(AppointmentStatus.SCHEDULED);
        return toResponse(appointmentRepo.save(a));
    }

    @Override
    @Transactional
    public AppointmentResponseDTO cancel(int id) {
        Appointment a = findOrThrow(id);
        requireScheduled(a, "cancelled");
        a.setStatus(AppointmentStatus.CANCELLED);   // dirty checking saves at commit
        return toResponse(a);
    }

    @Override
    @Transactional
    public AppointmentResponseDTO complete(int id) {
        Appointment a = findOrThrow(id);
        requireScheduled(a, "completed");
        a.setStatus(AppointmentStatus.COMPLETED);
        return toResponse(a);
    }

    @Override
    @Transactional
    public AppointmentResponseDTO reschedule(int id, AppointmentRescheduleDTO dto) {
        validateTimes(dto.getStartTime(), dto.getEndTime());

        Appointment a = findOrThrow(id);
        requireScheduled(a, "rescheduled");

        // lock the doctor, then re-check overlap ignoring this appointment itself
        doctorRepo.findByIdForUpdate(a.getDoctor().getId());
        checkNoOverlap(a.getDoctor().getId(), a.getPatient().getId(),
                dto.getStartTime(), dto.getEndTime(), a.getId());

        a.setStartTime(dto.getStartTime());
        a.setEndTime(dto.getEndTime());
        return toResponse(a);
    }

    // ---------- helpers ----------

    private Appointment findOrThrow(int id) {
        return appointmentRepo.findById(id)
                .orElseThrow(() -> new AppointmentNotFoundException(
                        "Appointment not found: " + id));
    }

    private void validateTimes(LocalDateTime start, LocalDateTime end) {
        if (!end.isAfter(start)) {
            throw new InvalidAppointmentTimeException("End time must be after start time");
        }
    }

    private void requireScheduled(Appointment a, String action) {
        if (a.getStatus() != AppointmentStatus.SCHEDULED) {
            throw new InvalidAppointmentStateException(
                    "Only SCHEDULED appointments can be " + action
                            + ". Current status: " + a.getStatus());
        }
    }

    private void checkNoOverlap(Integer doctorId, Integer patientId,
                                LocalDateTime start, LocalDateTime end, Integer excludeId) {
        if (appointmentRepo.existsDoctorOverlap(
                doctorId, AppointmentStatus.SCHEDULED, start, end, excludeId)) {
            throw new AppointmentConflictException("Doctor is not available in this time slot");
        }
        if (appointmentRepo.existsPatientOverlap(
                patientId, AppointmentStatus.SCHEDULED, start, end, excludeId)) {
            throw new AppointmentConflictException(
                    "Patient already has an appointment in this time slot");
        }
    }

    private AppointmentResponseDTO toResponse(Appointment a) {
        AppointmentResponseDTO dto = new AppointmentResponseDTO();
        dto.setId(a.getId());
        dto.setPatientId(a.getPatient().getId());
        dto.setPatientName(a.getPatient().getName());
        dto.setDoctorId(a.getDoctor().getId());
        dto.setDoctorName(a.getDoctor().getName());
        dto.setStartTime(a.getStartTime());
        dto.setEndTime(a.getEndTime());
        dto.setStatus(a.getStatus());
        dto.setReason(a.getReason());
        return dto;
    }
}