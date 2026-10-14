package com.example.HospitalManagementSystem;

import com.example.HospitalManagementSystem.DTO.DoctorRequestDTO;
import com.example.HospitalManagementSystem.DTO.DoctorResponseDTO;
import com.example.HospitalManagementSystem.Entity.Department;
import com.example.HospitalManagementSystem.Entity.Doctor;
import com.example.HospitalManagementSystem.Exception.DepartmentNotFoundException;
import com.example.HospitalManagementSystem.Exception.DoctorNotFoundException;
import com.example.HospitalManagementSystem.Exception.DuplicateDoctorException;
import com.example.HospitalManagementSystem.Repository.DepartmentRepo;
import com.example.HospitalManagementSystem.Repository.DoctorRepo;
import com.example.HospitalManagementSystem.ServiceImpl.DoctorServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DoctorServiceImplTest {

    @Mock
    private DoctorRepo doctorRepo;
    @Mock
    private DepartmentRepo departmentRepo;

    @InjectMocks
    private DoctorServiceImpl service;

    private Department department;

    @BeforeEach
    void setUp() {
        department = new Department();
        department.setId(1);
        department.setName("Cardiology");
    }

    // ---------- helpers ----------

    private DoctorRequestDTO request(String email, String license) {
        DoctorRequestDTO dto = new DoctorRequestDTO();
        dto.setName("Dr. Arjun");
        dto.setAge(38);
        dto.setGender("M");
        dto.setDepartmentId(1);
        dto.setEmail(email);
        dto.setPhone("9841234501");
        dto.setAddress("Chennai");
        dto.setExperience(13);
        dto.setLicenseNumber(license);
        dto.setSalary(new BigDecimal("450000.00"));
        dto.setConsultationFee(new BigDecimal("800.00"));
        return dto;
    }

    private Doctor doctor(int id, String email, String license) {
        Doctor d = new Doctor();
        d.setId(id);
        d.setName("Dr. Existing");
        d.setDepartment(department);
        d.setEmail(email);
        d.setLicenseNumber(license);
        return d;
    }

    // ---------- saveDoctor ----------

    @Test
    void saveDoctor_success_mapsDepartmentAndFields() {
        DoctorRequestDTO dto = request("a@x.com", "L1");
        when(doctorRepo.existsByLicenseNumber("L1")).thenReturn(false);
        when(doctorRepo.existsByEmail("a@x.com")).thenReturn(false);
        when(departmentRepo.findById(1)).thenReturn(Optional.of(department));
        when(doctorRepo.save(any(Doctor.class))).thenAnswer(inv -> inv.getArgument(0));

        DoctorResponseDTO result = service.saveDoctor(dto);

        assertThat(result.getEmail()).isEqualTo("a@x.com");
        assertThat(result.getDepartmentId()).isEqualTo(1);
        assertThat(result.getDepartmentName()).isEqualTo("Cardiology");
        assertThat(result.getConsultationFee()).isEqualByComparingTo("800.00");
    }

    @Test
    void saveDoctor_duplicateLicense_throws() {
        when(doctorRepo.existsByLicenseNumber("L1")).thenReturn(true);

        assertThatThrownBy(() -> service.saveDoctor(request("a@x.com", "L1")))
                .isInstanceOf(DuplicateDoctorException.class)
                .hasMessageContaining("License");

        verify(doctorRepo, never()).save(any());
    }

    @Test
    void saveDoctor_duplicateEmail_throws() {
        when(doctorRepo.existsByLicenseNumber("L1")).thenReturn(false);
        when(doctorRepo.existsByEmail("a@x.com")).thenReturn(true);

        assertThatThrownBy(() -> service.saveDoctor(request("a@x.com", "L1")))
                .isInstanceOf(DuplicateDoctorException.class)
                .hasMessageContaining("Email");

        verify(doctorRepo, never()).save(any());
    }

    @Test
    void saveDoctor_departmentNotFound_throws() {
        when(doctorRepo.existsByLicenseNumber("L1")).thenReturn(false);
        when(doctorRepo.existsByEmail("a@x.com")).thenReturn(false);
        when(departmentRepo.findById(1)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.saveDoctor(request("a@x.com", "L1")))
                .isInstanceOf(DepartmentNotFoundException.class);

        verify(doctorRepo, never()).save(any());
    }

    // ---------- saveAllDoctors ----------

    @Test
    void saveAllDoctors_success_loadsDepartmentOnlyOnce() {
        List<DoctorRequestDTO> dtos = List.of(
                request("a@x.com", "L1"),
                request("b@x.com", "L2"));
        when(departmentRepo.findById(1)).thenReturn(Optional.of(department));
        when(doctorRepo.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));

        List<DoctorResponseDTO> result = service.saveAllDoctors(dtos);

        assertThat(result).hasSize(2);
        verify(departmentRepo, times(1)).findById(1);   // same department, one lookup
    }

    @Test
    void saveAllDoctors_duplicateEmailInsideRequest_throwsBeforeSaving() {
        List<DoctorRequestDTO> dtos = List.of(
                request("same@x.com", "L1"),
                request("same@x.com", "L2"));

        assertThatThrownBy(() -> service.saveAllDoctors(dtos))
                .isInstanceOf(DuplicateDoctorException.class)
                .hasMessageContaining("Duplicate email in request");

        verify(doctorRepo, never()).saveAll(anyList());
    }

    @Test
    void saveAllDoctors_duplicateLicenseInsideRequest_throwsBeforeSaving() {
        List<DoctorRequestDTO> dtos = List.of(
                request("a@x.com", "SAME"),
                request("b@x.com", "SAME"));

        assertThatThrownBy(() -> service.saveAllDoctors(dtos))
                .isInstanceOf(DuplicateDoctorException.class)
                .hasMessageContaining("Duplicate license number in request");

        verify(doctorRepo, never()).saveAll(anyList());
    }

    @Test
    void saveAllDoctors_duplicateAgainstDatabase_throws() {
        List<DoctorRequestDTO> dtos = List.of(request("a@x.com", "L1"));
        when(doctorRepo.existsByLicenseNumber("L1")).thenReturn(true);

        assertThatThrownBy(() -> service.saveAllDoctors(dtos))
                .isInstanceOf(DuplicateDoctorException.class);

        verify(doctorRepo, never()).saveAll(anyList());
    }

    // ---------- updateDoctor ----------

    @Test
    void updateDoctor_success_usesIdNotChecksAndCopiesFields() {
        Doctor existing = doctor(5, "old@x.com", "OLD");
        DoctorRequestDTO dto = request("new@x.com", "NEW");
        when(doctorRepo.findById(5)).thenReturn(Optional.of(existing));
        when(doctorRepo.existsByEmailAndIdNot("new@x.com", 5)).thenReturn(false);
        when(doctorRepo.existsByLicenseNumberAndIdNot("NEW", 5)).thenReturn(false);
        when(departmentRepo.findById(1)).thenReturn(Optional.of(department));
        when(doctorRepo.save(existing)).thenReturn(existing);

        DoctorResponseDTO result = service.updateDoctor(5, dto);

        assertThat(result.getEmail()).isEqualTo("new@x.com");
        assertThat(result.getLicenseNumber()).isEqualTo("NEW");
        assertThat(existing.getEmail()).isEqualTo("new@x.com");

        // the "ignore myself" version must be used, not the plain existsBy
        verify(doctorRepo, never()).existsByEmail(any());
        verify(doctorRepo, never()).existsByLicenseNumber(any());
    }

    @Test
    void updateDoctor_emailTakenByAnotherDoctor_throws() {
        when(doctorRepo.findById(5)).thenReturn(Optional.of(doctor(5, "old@x.com", "OLD")));
        when(doctorRepo.existsByEmailAndIdNot("taken@x.com", 5)).thenReturn(true);

        assertThatThrownBy(() -> service.updateDoctor(5, request("taken@x.com", "NEW")))
                .isInstanceOf(DuplicateDoctorException.class)
                .hasMessageContaining("Email");

        verify(doctorRepo, never()).save(any());
    }

    @Test
    void updateDoctor_licenseTakenByAnotherDoctor_throws() {
        when(doctorRepo.findById(5)).thenReturn(Optional.of(doctor(5, "old@x.com", "OLD")));
        when(doctorRepo.existsByEmailAndIdNot("new@x.com", 5)).thenReturn(false);
        when(doctorRepo.existsByLicenseNumberAndIdNot("TAKEN", 5)).thenReturn(true);

        assertThatThrownBy(() -> service.updateDoctor(5, request("new@x.com", "TAKEN")))
                .isInstanceOf(DuplicateDoctorException.class)
                .hasMessageContaining("License");

        verify(doctorRepo, never()).save(any());
    }

    @Test
    void updateDoctor_notFound_throws() {
        when(doctorRepo.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateDoctor(99, request("a@x.com", "L1")))
                .isInstanceOf(DoctorNotFoundException.class);
    }

    @Test
    void updateDoctor_departmentNotFound_throws() {
        when(doctorRepo.findById(5)).thenReturn(Optional.of(doctor(5, "old@x.com", "OLD")));
        when(doctorRepo.existsByEmailAndIdNot("new@x.com", 5)).thenReturn(false);
        when(doctorRepo.existsByLicenseNumberAndIdNot("NEW", 5)).thenReturn(false);
        when(departmentRepo.findById(1)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateDoctor(5, request("new@x.com", "NEW")))
                .isInstanceOf(DepartmentNotFoundException.class);
    }

    // ---------- delete ----------

    @Test
    void deleteDoctorById_exists_deletes() {
        when(doctorRepo.existsById(5)).thenReturn(true);

        service.deleteDoctorById(5);

        verify(doctorRepo).deleteById(5);
    }

    @Test
    void deleteDoctorById_notFound_throwsAndDeletesNothing() {
        when(doctorRepo.existsById(99)).thenReturn(false);

        assertThatThrownBy(() -> service.deleteDoctorById(99))
                .isInstanceOf(DoctorNotFoundException.class);

        verify(doctorRepo, never()).deleteById(any());
    }

    @Test
    void deleteAllDoctors_callsRepo() {
        service.deleteAllDoctors();

        verify(doctorRepo).deleteAll();
    }

    // ---------- reads ----------

    @Test
    void findDoctorById_found_mapsDepartmentAndFee() {
        Doctor d = doctor(5, "a@x.com", "L1");
        d.setConsultationFee(new BigDecimal("600.00"));
        when(doctorRepo.findById(5)).thenReturn(Optional.of(d));

        DoctorResponseDTO result = service.findDoctorById(5);

        assertThat(result.getId()).isEqualTo(5);
        assertThat(result.getDepartmentName()).isEqualTo("Cardiology");
        assertThat(result.getConsultationFee()).isEqualByComparingTo("600.00");
    }

    @Test
    void findDoctorById_notFound_throws() {
        when(doctorRepo.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findDoctorById(99))
                .isInstanceOf(DoctorNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void findAll_returnsMappedPage() {
        PageRequest pageable = PageRequest.of(0, 5);
        Page<Doctor> page = new PageImpl<>(List.of(doctor(1, "a@x.com", "L1")), pageable, 1);
        when(doctorRepo.findAll(pageable)).thenReturn(page);

        Page<DoctorResponseDTO> result = service.findAll(pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getDepartmentName()).isEqualTo("Cardiology");
    }

    @Test
    void searchDoctors_passesFiltersToRepo() {
        PageRequest pageable = PageRequest.of(0, 5);
        Page<Doctor> page = new PageImpl<>(List.of(doctor(1, "a@x.com", "L1")), pageable, 1);
        when(doctorRepo.searchDoctors("ar", "Cardiology", 5, 20, pageable)).thenReturn(page);

        Page<DoctorResponseDTO> result = service.searchDoctors("ar", "Cardiology", 5, 20, pageable);

        assertThat(result.getContent()).hasSize(1);
        verify(doctorRepo).searchDoctors("ar", "Cardiology", 5, 20, pageable);
    }

    @Test
    void findByNameContainingIgnoreCase_returnsMappedList() {
        when(doctorRepo.findByNameContainingIgnoreCase("arj"))
                .thenReturn(List.of(doctor(1, "a@x.com", "L1")));

        List<DoctorResponseDTO> result = service.findByNameContainingIgnoreCase("arj");

        assertThat(result).hasSize(1);
    }
}