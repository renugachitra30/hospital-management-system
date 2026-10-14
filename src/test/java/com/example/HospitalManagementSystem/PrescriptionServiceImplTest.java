package com.example.HospitalManagementSystem;

import com.example.HospitalManagementSystem.DTO.PrescriptionItemRequestDTO;
import com.example.HospitalManagementSystem.DTO.PrescriptionRequestDTO;
import com.example.HospitalManagementSystem.DTO.PrescriptionResponseDTO;
import com.example.HospitalManagementSystem.DTO.PrescriptionUpdateDTO;
import com.example.HospitalManagementSystem.Entity.*;
import com.example.HospitalManagementSystem.Enum.AppointmentStatus;
import com.example.HospitalManagementSystem.Exception.AppointmentNotFoundException;
import com.example.HospitalManagementSystem.Exception.DuplicatePrescriptionException;
import com.example.HospitalManagementSystem.Exception.InvalidAppointmentStateException;
import com.example.HospitalManagementSystem.Exception.PrescriptionNotFoundException;
import com.example.HospitalManagementSystem.Repository.AppointmentRepo;
import com.example.HospitalManagementSystem.Repository.PrescriptionRepo;
import com.example.HospitalManagementSystem.ServiceImpl.PrescriptionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PrescriptionServiceImplTest {

    @Mock
    private PrescriptionRepo prescriptionRepo;
    @Mock
    private AppointmentRepo appointmentRepo;

    @InjectMocks
    private PrescriptionServiceImpl service;

    private Doctor doctor;
    private Patient patient;

    @BeforeEach
    void setUp() {
        doctor = new Doctor();
        doctor.setId(2);
        doctor.setName("Dr. Priya");

        patient = new Patient();
        patient.setId(10);
        patient.setName("Anu");
    }

    // ---------- helpers ----------

    private Appointment appointment(int id, AppointmentStatus status) {
        Appointment a = new Appointment();
        a.setId(id);
        a.setDoctor(doctor);
        a.setPatient(patient);
        a.setStatus(status);
        return a;
    }

    private PrescriptionItemRequestDTO itemDto(String medicine) {
        return new PrescriptionItemRequestDTO(medicine, "500 mg", "Twice a day", 5, "After food");
    }

    private PrescriptionRequestDTO createRequest(int appointmentId, String... medicines) {
        List<PrescriptionItemRequestDTO> items = new java.util.ArrayList<>();
        for (String m : medicines) {
            items.add(itemDto(m));
        }
        return new PrescriptionRequestDTO(appointmentId, "Migraine", "Rest", items);
    }

    private Prescription prescription(int id, String... medicines) {
        Prescription p = new Prescription();
        p.setId(id);
        p.setAppointment(appointment(3, AppointmentStatus.COMPLETED));
        p.setDiagnosis("Migraine");
        p.setNotes("Rest");
        for (String m : medicines) {
            PrescriptionItem item = new PrescriptionItem();
            item.setMedicineName(m);
            p.addItem(item);
        }
        return p;
    }

    // ---------- create ----------

    @Test
    void create_success_savesPrescriptionWithAllItems() {
        when(appointmentRepo.findById(3))
                .thenReturn(Optional.of(appointment(3, AppointmentStatus.COMPLETED)));
        when(prescriptionRepo.existsByAppointmentId(3)).thenReturn(false);
        when(prescriptionRepo.save(any(Prescription.class))).thenAnswer(inv -> inv.getArgument(0));

        PrescriptionResponseDTO result =
                service.create(createRequest(3, "Paracetamol", "Sumatriptan"));

        assertThat(result.getAppointmentId()).isEqualTo(3);
        assertThat(result.getPatientName()).isEqualTo("Anu");
        assertThat(result.getDoctorName()).isEqualTo("Dr. Priya");
        assertThat(result.getDiagnosis()).isEqualTo("Migraine");
        assertThat(result.getItems()).hasSize(2);
        assertThat(result.getItems().get(0).getMedicineName()).isEqualTo("Paracetamol");
    }

    @Test
    void create_setsBothSidesOfRelationship() {
        when(appointmentRepo.findById(3))
                .thenReturn(Optional.of(appointment(3, AppointmentStatus.COMPLETED)));
        when(prescriptionRepo.existsByAppointmentId(3)).thenReturn(false);
        when(prescriptionRepo.save(any(Prescription.class))).thenAnswer(inv -> inv.getArgument(0));

        service.create(createRequest(3, "Paracetamol"));

        ArgumentCaptor<Prescription> captor = ArgumentCaptor.forClass(Prescription.class);
        verify(prescriptionRepo).save(captor.capture());
        Prescription saved = captor.getValue();

        // addItem() must set item.prescription, otherwise prescription_id would be NULL
        assertThat(saved.getItems()).hasSize(1);
        assertThat(saved.getItems().get(0).getPrescription()).isSameAs(saved);
    }

    @Test
    void create_appointmentNotFound_throws() {
        when(appointmentRepo.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(createRequest(99, "X")))
                .isInstanceOf(AppointmentNotFoundException.class);

        verify(prescriptionRepo, never()).save(any());
    }

    @Test
    void create_appointmentNotCompleted_throwsInvalidState() {
        when(appointmentRepo.findById(2))
                .thenReturn(Optional.of(appointment(2, AppointmentStatus.SCHEDULED)));

        assertThatThrownBy(() -> service.create(createRequest(2, "X")))
                .isInstanceOf(InvalidAppointmentStateException.class)
                .hasMessageContaining("COMPLETED");

        verify(prescriptionRepo, never()).save(any());
    }

    @Test
    void create_alreadyExists_throwsDuplicate() {
        when(appointmentRepo.findById(3))
                .thenReturn(Optional.of(appointment(3, AppointmentStatus.COMPLETED)));
        when(prescriptionRepo.existsByAppointmentId(3)).thenReturn(true);

        assertThatThrownBy(() -> service.create(createRequest(3, "X")))
                .isInstanceOf(DuplicatePrescriptionException.class);

        verify(prescriptionRepo, never()).save(any());
    }

    // ---------- update ----------

    @Test
    void update_replacesDiagnosisNotesAndAllItems() {
        Prescription p = prescription(1, "Old1", "Old2");
        when(prescriptionRepo.findById(1)).thenReturn(Optional.of(p));
        when(prescriptionRepo.saveAndFlush(p)).thenReturn(p);

        PrescriptionUpdateDTO dto = new PrescriptionUpdateDTO(
                "Tension headache", "New notes", List.of(itemDto("New1")));

        PrescriptionResponseDTO result = service.update(1, dto);

        assertThat(result.getDiagnosis()).isEqualTo("Tension headache");
        assertThat(result.getNotes()).isEqualTo("New notes");
        assertThat(result.getItems()).hasSize(1);
        assertThat(result.getItems().get(0).getMedicineName()).isEqualTo("New1");
        assertThat(p.getItems()).hasSize(1);   // old items were removed from the list
    }

    @Test
    void update_usesSaveAndFlushSoIdsAreGenerated() {
        Prescription p = prescription(1, "Old");
        when(prescriptionRepo.findById(1)).thenReturn(Optional.of(p));
        when(prescriptionRepo.saveAndFlush(p)).thenReturn(p);

        service.update(1, new PrescriptionUpdateDTO("D", "N", List.of(itemDto("New"))));

        verify(prescriptionRepo).saveAndFlush(p);
    }

    @Test
    void update_notFound_throws() {
        when(prescriptionRepo.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(99,
                new PrescriptionUpdateDTO("D", "N", List.of(itemDto("X")))))
                .isInstanceOf(PrescriptionNotFoundException.class);
    }

    // ---------- delete ----------

    @Test
    void delete_exists_deletesPrescription() {
        Prescription p = prescription(1, "A");
        when(prescriptionRepo.findById(1)).thenReturn(Optional.of(p));

        service.delete(1);

        verify(prescriptionRepo).delete(p);
    }

    @Test
    void delete_notFound_throwsAndDeletesNothing() {
        when(prescriptionRepo.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(99))
                .isInstanceOf(PrescriptionNotFoundException.class);

        verify(prescriptionRepo, never()).delete(any());
    }

    // ---------- reads ----------

    @Test
    void findById_found_mapsEverything() {
        when(prescriptionRepo.findById(1)).thenReturn(Optional.of(prescription(1, "A", "B")));

        PrescriptionResponseDTO result = service.findById(1);

        assertThat(result.getId()).isEqualTo(1);
        assertThat(result.getPatientId()).isEqualTo(10);
        assertThat(result.getDoctorId()).isEqualTo(2);
        assertThat(result.getItems()).hasSize(2);
    }

    @Test
    void findById_notFound_throws() {
        when(prescriptionRepo.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(99))
                .isInstanceOf(PrescriptionNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void findByAppointment_found_returnsPrescription() {
        when(prescriptionRepo.findByAppointmentId(3))
                .thenReturn(Optional.of(prescription(1, "A")));

        PrescriptionResponseDTO result = service.findByAppointment(3);

        assertThat(result.getAppointmentId()).isEqualTo(3);
        assertThat(result.getItems()).hasSize(1);
    }

    @Test
    void findByAppointment_notFound_throws() {
        when(prescriptionRepo.findByAppointmentId(4)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findByAppointment(4))
                .isInstanceOf(PrescriptionNotFoundException.class)
                .hasMessageContaining("4");
    }
}