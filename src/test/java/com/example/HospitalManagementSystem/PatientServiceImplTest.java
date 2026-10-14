package com.example.HospitalManagementSystem;

import com.example.HospitalManagementSystem.DTO.PatientRequestDTO;
import com.example.HospitalManagementSystem.DTO.PatientResponseDTO;
import com.example.HospitalManagementSystem.Entity.Patient;
import com.example.HospitalManagementSystem.Exception.PatientNotFoundException;
import com.example.HospitalManagementSystem.Repository.PatientRepo;
import com.example.HospitalManagementSystem.ServiceImpl.PatientServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PatientServiceImplTest {

    @Mock
    private PatientRepo patientRepo;

    @InjectMocks
    private PatientServiceImpl service;

    // ---------- helpers ----------

    private PatientRequestDTO request(String name) {
        PatientRequestDTO dto = new PatientRequestDTO();
        dto.setName(name);
        dto.setAge(30);
        dto.setGender('F');
        dto.setEmail("anu@x.com");
        dto.setAddress("Chennai");
        dto.setPhone("9876543210");
        dto.setBloodGroup("O+");
        return dto;
    }

    private Patient patient(int id, String name) {
        Patient p = new Patient();
        p.setId(id);
        p.setName(name);
        p.setAge(30);
        p.setGender('F');
        p.setEmail("anu@x.com");
        p.setAddress("Chennai");
        p.setPhone("9876543210");
        p.setBloodGroup("O+");
        return p;
    }

    // ---------- create ----------

    @Test
    void savePatient_mapsAllFields() {
        when(patientRepo.save(any(Patient.class))).thenAnswer(inv -> {
            Patient p = inv.getArgument(0);
            p.setId(1);
            return p;
        });

        PatientResponseDTO result = service.savePatient(request("Anu"));

        assertThat(result.getId()).isEqualTo(1);
        assertThat(result.getName()).isEqualTo("Anu");
        assertThat(result.getGender()).isEqualTo('F');
        assertThat(result.getBloodGroup()).isEqualTo("O+");
    }

    @Test
    void saveAllPatients_savesAndReturnsAll() {
        when(patientRepo.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));

        List<PatientResponseDTO> result =
                service.saveAllPatients(List.of(request("Anu"), request("Ravi")));

        assertThat(result).hasSize(2);
        assertThat(result.get(1).getName()).isEqualTo("Ravi");
    }

    // ---------- read ----------

    @Test
    void findPatientById_found() {
        when(patientRepo.findById(1)).thenReturn(Optional.of(patient(1, "Anu")));

        PatientResponseDTO result = service.findPatientById(1);

        assertThat(result.getName()).isEqualTo("Anu");
        assertThat(result.getEmail()).isEqualTo("anu@x.com");
    }

    @Test
    void findPatientById_notFound_throws() {
        when(patientRepo.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findPatientById(99))
                .isInstanceOf(PatientNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void findAll_returnsMappedPage() {
        PageRequest pageable = PageRequest.of(0, 5);
        Page<Patient> page = new PageImpl<>(List.of(patient(1, "Anu")), pageable, 1);
        when(patientRepo.findAll(pageable)).thenReturn(page);

        Page<PatientResponseDTO> result = service.findAll(pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("Anu");
    }

    @Test
    void findByNameContainingIgnoreCase_returnsMappedList() {
        when(patientRepo.findByNameContainingIgnoreCase("an"))
                .thenReturn(List.of(patient(1, "Anu")));

        List<PatientResponseDTO> result = service.findByNameContainingIgnoreCase("an");

        assertThat(result).hasSize(1);
    }

    @Test
    void findByAgeGreaterThan_returnsMappedList() {
        when(patientRepo.findByAgeGreaterThan(20)).thenReturn(List.of(patient(1, "Anu")));

        List<PatientResponseDTO> result = service.findByAgeGreaterThan(20);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getAge()).isEqualTo(30);
    }

    @Test
    void searchPatients_passesFiltersToRepo() {
        PageRequest pageable = PageRequest.of(0, 5);
        Page<Patient> page = new PageImpl<>(List.of(patient(1, "Anu")), pageable, 1);
        when(patientRepo.searchPatients("an", "O+", 18, 60, pageable)).thenReturn(page);

        Page<PatientResponseDTO> result = service.searchPatients("an", "O+", 18, 60, pageable);

        assertThat(result.getContent()).hasSize(1);
        verify(patientRepo).searchPatients("an", "O+", 18, 60, pageable);
    }

    // ---------- update ----------

    @Test
    void updatePatient_success_copiesFields() {
        Patient existing = patient(1, "Old Name");
        when(patientRepo.findById(1)).thenReturn(Optional.of(existing));
        when(patientRepo.save(existing)).thenReturn(existing);

        PatientRequestDTO dto = request("New Name");
        dto.setBloodGroup("A+");

        PatientResponseDTO result = service.updatePatient(1, dto);

        assertThat(result.getName()).isEqualTo("New Name");
        assertThat(result.getBloodGroup()).isEqualTo("A+");
        assertThat(existing.getName()).isEqualTo("New Name");
    }

    @Test
    void updatePatient_notFound_throwsAndSavesNothing() {
        when(patientRepo.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updatePatient(99, request("X")))
                .isInstanceOf(PatientNotFoundException.class);

        verify(patientRepo, never()).save(any());
    }

    // ---------- delete ----------

    @Test
    void deletePatientById_callsRepo() {
        service.deletePatientById(1);

        verify(patientRepo).deleteById(1);
    }

    @Test
    void deleteAllPatients_callsRepo() {
        service.deleteAllPatients();

        verify(patientRepo).deleteAll();
    }
}