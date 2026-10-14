package com.example.HospitalManagementSystem;

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
import com.example.HospitalManagementSystem.ServiceImpl.AppointmentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceImplTest {
    
    @Mock
    private AppointmentRepo appointmentRepo;
    @Mock
    private DoctorRepo doctorRepo;
    @Mock
    private PatientRepo patientRepo;

    @InjectMocks
    private AppointmentServiceImpl service;

    private Doctor doctor;
    private Patient patient;
    private LocalDateTime start;
    private LocalDateTime end;

    @BeforeEach
    void setUp() {
        doctor = new Doctor();
        doctor.setId(1);
        doctor.setName("Dr. Arjun");

        patient = new Patient();
        patient.setId(10);
        patient.setName("Anu");

        start = LocalDateTime.of(2026, 10, 20, 10, 0);
        end = LocalDateTime.of(2026, 10, 20, 10, 30);
    }

    // ---------- helpers ----------

    private AppointmentRequestDTO request() {
        return new AppointmentRequestDTO(10, 1, start, end, "Chest pain");
    }

    private Appointment appointment(int id, AppointmentStatus status) {
        Appointment a = new Appointment();
        a.setId(id);
        a.setDoctor(doctor);
        a.setPatient(patient);
        a.setStartTime(start);
        a.setEndTime(end);
        a.setStatus(status);
        return a;
    }

    // ---------- book ----------

    @Test
    void book_success_savesScheduledAppointment() {
        when(doctorRepo.findByIdForUpdate(1)).thenReturn(Optional.of(doctor));
        when(patientRepo.findById(10)).thenReturn(Optional.of(patient));
        when(appointmentRepo.existsDoctorOverlap(1, AppointmentStatus.SCHEDULED, start, end, null)).thenReturn(false);
        when(appointmentRepo.existsPatientOverlap(10, AppointmentStatus.SCHEDULED, start, end, null)).thenReturn(false);
        when(appointmentRepo.save(any(Appointment.class))).thenAnswer(inv -> inv.getArgument(0));

        AppointmentResponseDTO result = service.book(request());

        assertThat(result.getStatus()).isEqualTo(AppointmentStatus.SCHEDULED);
        assertThat(result.getDoctorName()).isEqualTo("Dr. Arjun");
        assertThat(result.getPatientName()).isEqualTo("Anu");
        assertThat(result.getStartTime()).isEqualTo(start);
        verify(appointmentRepo).save(any(Appointment.class));
    }

    @Test
    void book_endBeforeStart_throwsInvalidTime() {
        AppointmentRequestDTO dto = new AppointmentRequestDTO(10, 1, end, start, "x");

        assertThatThrownBy(() -> service.book(dto)).isInstanceOf(InvalidAppointmentTimeException.class);

        verifyNoInteractions(doctorRepo, patientRepo, appointmentRepo);
    }

    @Test
    void book_endEqualsStart_throwsInvalidTime() {
        AppointmentRequestDTO dto = new AppointmentRequestDTO(10, 1, start, start, "x");

        assertThatThrownBy(() -> service.book(dto)).isInstanceOf(InvalidAppointmentTimeException.class);
    }

    @Test
    void book_doctorNotFound_throws() {
        when(doctorRepo.findByIdForUpdate(1)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.book(request())).isInstanceOf(DoctorNotFoundException.class).hasMessageContaining("1");

        verify(appointmentRepo, never()).save(any());
    }

    @Test
    void book_patientNotFound_throws() {
        when(doctorRepo.findByIdForUpdate(1)).thenReturn(Optional.of(doctor));
        when(patientRepo.findById(10)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.book(request())).isInstanceOf(PatientNotFoundException.class);

        verify(appointmentRepo, never()).save(any());
    }

    @Test
    void book_doctorBusy_throwsConflict() {
        when(doctorRepo.findByIdForUpdate(1)).thenReturn(Optional.of(doctor));
        when(patientRepo.findById(10)).thenReturn(Optional.of(patient));
        when(appointmentRepo.existsDoctorOverlap(1, AppointmentStatus.SCHEDULED, start, end, null)).thenReturn(true);

        assertThatThrownBy(() -> service.book(request())).isInstanceOf(AppointmentConflictException.class).hasMessageContaining("Doctor");

        verify(appointmentRepo, never()).save(any());
    }

    @Test
    void book_patientBusy_throwsConflict() {
        when(doctorRepo.findByIdForUpdate(1)).thenReturn(Optional.of(doctor));
        when(patientRepo.findById(10)).thenReturn(Optional.of(patient));
        when(appointmentRepo.existsDoctorOverlap(1, AppointmentStatus.SCHEDULED, start, end, null)).thenReturn(false);
        when(appointmentRepo.existsPatientOverlap(10, AppointmentStatus.SCHEDULED, start, end, null)).thenReturn(true);

        assertThatThrownBy(() -> service.book(request())).isInstanceOf(AppointmentConflictException.class).hasMessageContaining("Patient");

        verify(appointmentRepo, never()).save(any());
    }

    // ---------- cancel ----------

    @Test
    void cancel_scheduled_becomesCancelled() {
        Appointment a = appointment(5, AppointmentStatus.SCHEDULED);
        when(appointmentRepo.findById(5)).thenReturn(Optional.of(a));

        AppointmentResponseDTO result = service.cancel(5);

        assertThat(result.getStatus()).isEqualTo(AppointmentStatus.CANCELLED);
        assertThat(a.getStatus()).isEqualTo(AppointmentStatus.CANCELLED);
    }

    @Test
    void cancel_alreadyCancelled_throwsInvalidState() {
        when(appointmentRepo.findById(5)).thenReturn(Optional.of(appointment(5, AppointmentStatus.CANCELLED)));

        assertThatThrownBy(() -> service.cancel(5)).isInstanceOf(InvalidAppointmentStateException.class).hasMessageContaining("cancelled");
    }

    @Test
    void cancel_completed_throwsInvalidState() {
        when(appointmentRepo.findById(5)).thenReturn(Optional.of(appointment(5, AppointmentStatus.COMPLETED)));

        assertThatThrownBy(() -> service.cancel(5)).isInstanceOf(InvalidAppointmentStateException.class);
    }

    @Test
    void cancel_notFound_throws() {
        when(appointmentRepo.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.cancel(99)).isInstanceOf(AppointmentNotFoundException.class);
    }

    // ---------- complete ----------

    @Test
    void complete_scheduled_becomesCompleted() {
        Appointment a = appointment(5, AppointmentStatus.SCHEDULED);
        when(appointmentRepo.findById(5)).thenReturn(Optional.of(a));

        AppointmentResponseDTO result = service.complete(5);

        assertThat(result.getStatus()).isEqualTo(AppointmentStatus.COMPLETED);
    }

    @Test
    void complete_cancelled_throwsInvalidState() {
        when(appointmentRepo.findById(5)).thenReturn(Optional.of(appointment(5, AppointmentStatus.CANCELLED)));

        assertThatThrownBy(() -> service.complete(5)).isInstanceOf(InvalidAppointmentStateException.class);
    }

    // ---------- reschedule ----------

    @Test
    void reschedule_success_updatesTimes() {
        Appointment a = appointment(5, AppointmentStatus.SCHEDULED);
        LocalDateTime newStart = LocalDateTime.of(2026, 10, 21, 11, 0);
        LocalDateTime newEnd = LocalDateTime.of(2026, 10, 21, 11, 30);

        when(appointmentRepo.findById(5)).thenReturn(Optional.of(a));
        when(appointmentRepo.existsDoctorOverlap(1, AppointmentStatus.SCHEDULED, newStart, newEnd, 5)).thenReturn(false);
        when(appointmentRepo.existsPatientOverlap(10, AppointmentStatus.SCHEDULED, newStart, newEnd, 5)).thenReturn(false);

        AppointmentResponseDTO result = service.reschedule(5, new AppointmentRescheduleDTO(newStart, newEnd));

        assertThat(result.getStartTime()).isEqualTo(newStart);
        assertThat(result.getEndTime()).isEqualTo(newEnd);
        verify(doctorRepo).findByIdForUpdate(1);   // doctor row was locked
    }

    @Test
    void reschedule_passesOwnIdAsExcludeId() {
        Appointment a = appointment(5, AppointmentStatus.SCHEDULED);
        when(appointmentRepo.findById(5)).thenReturn(Optional.of(a));
        when(appointmentRepo.existsDoctorOverlap(1, AppointmentStatus.SCHEDULED, start, end, 5)).thenReturn(false);
        when(appointmentRepo.existsPatientOverlap(10, AppointmentStatus.SCHEDULED, start, end, 5)).thenReturn(false);

        service.reschedule(5, new AppointmentRescheduleDTO(start, end));

        // proves the appointment ignores itself when checking overlap
        verify(appointmentRepo).existsDoctorOverlap(1, AppointmentStatus.SCHEDULED, start, end, 5);
    }

    @Test
    void reschedule_completed_throwsInvalidState() {
        when(appointmentRepo.findById(5)).thenReturn(Optional.of(appointment(5, AppointmentStatus.COMPLETED)));

        assertThatThrownBy(() -> service.reschedule(5, new AppointmentRescheduleDTO(start, end))).isInstanceOf(InvalidAppointmentStateException.class).hasMessageContaining("rescheduled");
    }

    @Test
    void reschedule_slotTaken_throwsConflict() {
        Appointment a = appointment(5, AppointmentStatus.SCHEDULED);
        when(appointmentRepo.findById(5)).thenReturn(Optional.of(a));
        when(appointmentRepo.existsDoctorOverlap(1, AppointmentStatus.SCHEDULED, start, end, 5)).thenReturn(true);

        assertThatThrownBy(() -> service.reschedule(5, new AppointmentRescheduleDTO(start, end))).isInstanceOf(AppointmentConflictException.class);
    }

    @Test
    void reschedule_endBeforeStart_throwsInvalidTime() {
        assertThatThrownBy(() -> service.reschedule(5, new AppointmentRescheduleDTO(end, start))).isInstanceOf(InvalidAppointmentTimeException.class);

        verifyNoInteractions(appointmentRepo);
    }

    // ---------- reads ----------

    @Test
    void findById_found_mapsFields() {
        Appointment a = appointment(5, AppointmentStatus.SCHEDULED);
        a.setReason("Fever");
        when(appointmentRepo.findById(5)).thenReturn(Optional.of(a));

        AppointmentResponseDTO result = service.findById(5);

        assertThat(result.getId()).isEqualTo(5);
        assertThat(result.getReason()).isEqualTo("Fever");
        assertThat(result.getDoctorId()).isEqualTo(1);
        assertThat(result.getPatientId()).isEqualTo(10);
    }

    @Test
    void findById_notFound_throws() {
        when(appointmentRepo.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(99)).isInstanceOf(AppointmentNotFoundException.class).hasMessageContaining("99");
    }

    @Test
    void findByDoctor_returnsMappedPage() {
        PageRequest pageable = PageRequest.of(0, 5);
        Page<Appointment> page = new PageImpl<>(List.of(appointment(5, AppointmentStatus.SCHEDULED)), pageable, 1);
        when(appointmentRepo.findByDoctorId(1, pageable)).thenReturn(page);

        Page<AppointmentResponseDTO> result = service.findByDoctor(1, pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getDoctorName()).isEqualTo("Dr. Arjun");
    }
}