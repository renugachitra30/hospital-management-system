package com.example.HospitalManagementSystem;

import com.example.HospitalManagementSystem.DTO.DepartmentRequestDTO;
import com.example.HospitalManagementSystem.DTO.DepartmentResponseDTO;
import com.example.HospitalManagementSystem.Entity.Department;
import com.example.HospitalManagementSystem.Exception.DepartmentInUseException;
import com.example.HospitalManagementSystem.Exception.DepartmentNotFoundException;
import com.example.HospitalManagementSystem.Exception.DuplicateDepartmentException;
import com.example.HospitalManagementSystem.Repository.DepartmentRepo;
import com.example.HospitalManagementSystem.Repository.DoctorRepo;
import com.example.HospitalManagementSystem.ServiceImpl.DepartmentServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
class DepartmentServiceImplTest {

    @Mock
    private DepartmentRepo departmentRepo;
    @Mock
    private DoctorRepo doctorRepo;

    @InjectMocks
    private DepartmentServiceImpl service;

    // ---------- helpers ----------

    private DepartmentRequestDTO request(String name) {
        return new DepartmentRequestDTO(name, "Heart care", "Block A", "044-1234");
    }

    private Department department(int id, String name) {
        Department d = new Department();
        d.setId(id);
        d.setName(name);
        d.setDescription("Heart care");
        d.setLocation("Block A");
        d.setPhone("044-1234");
        return d;
    }

    // ---------- create ----------

    @Test
    void create_success_returnsSavedDepartment() {
        when(departmentRepo.existsByNameIgnoreCase("Cardiology")).thenReturn(false);
        when(departmentRepo.save(any(Department.class))).thenAnswer(inv -> inv.getArgument(0));

        DepartmentResponseDTO result = service.create(request("Cardiology"));

        assertThat(result.getName()).isEqualTo("Cardiology");
        assertThat(result.getLocation()).isEqualTo("Block A");
    }

    @Test
    void create_trimsName() {
        when(departmentRepo.existsByNameIgnoreCase("Cardiology")).thenReturn(false);
        when(departmentRepo.save(any(Department.class))).thenAnswer(inv -> inv.getArgument(0));

        service.create(request("  Cardiology  "));

        ArgumentCaptor<Department> captor = ArgumentCaptor.forClass(Department.class);
        verify(departmentRepo).save(captor.capture());
        assertThat(captor.getValue().getName()).isEqualTo("Cardiology");
    }

    @Test
    void create_duplicateName_throws() {
        when(departmentRepo.existsByNameIgnoreCase("Cardiology")).thenReturn(true);

        assertThatThrownBy(() -> service.create(request("Cardiology")))
                .isInstanceOf(DuplicateDepartmentException.class)
                .hasMessageContaining("Cardiology");

        verify(departmentRepo, never()).save(any());
    }

    // ---------- createAll ----------

    @Test
    void createAll_success_savesAll() {
        when(departmentRepo.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));

        List<DepartmentResponseDTO> result =
                service.createAll(List.of(request("Cardiology"), request("Neurology")));

        assertThat(result).hasSize(2);
        verify(departmentRepo).saveAll(anyList());
    }

    @Test
    void createAll_duplicateInsideRequest_ignoresCase_throwsBeforeSaving() {
        List<DepartmentRequestDTO> dtos = List.of(request("Cardiology"), request("cardiology"));

        assertThatThrownBy(() -> service.createAll(dtos))
                .isInstanceOf(DuplicateDepartmentException.class)
                .hasMessageContaining("Duplicate department in request");

        verify(departmentRepo, never()).saveAll(anyList());
    }

    @Test
    void createAll_duplicateAgainstDatabase_throws() {
        when(departmentRepo.existsByNameIgnoreCase("Cardiology")).thenReturn(true);

        assertThatThrownBy(() -> service.createAll(List.of(request("Cardiology"))))
                .isInstanceOf(DuplicateDepartmentException.class);

        verify(departmentRepo, never()).saveAll(anyList());
    }

    // ---------- update ----------

    @Test
    void update_success_copiesFields() {
        Department existing = department(1, "Cardiology");
        when(departmentRepo.findById(1)).thenReturn(Optional.of(existing));
        when(departmentRepo.existsByNameIgnoreCaseAndIdNot("Heart", 1)).thenReturn(false);
        when(departmentRepo.save(existing)).thenReturn(existing);

        DepartmentResponseDTO result = service.update(1, request("Heart"));

        assertThat(result.getName()).isEqualTo("Heart");
        assertThat(existing.getName()).isEqualTo("Heart");
    }

    @Test
    void update_nameTakenByAnotherDepartment_throws() {
        when(departmentRepo.findById(1)).thenReturn(Optional.of(department(1, "Cardiology")));
        when(departmentRepo.existsByNameIgnoreCaseAndIdNot("Neurology", 1)).thenReturn(true);

        assertThatThrownBy(() -> service.update(1, request("Neurology")))
                .isInstanceOf(DuplicateDepartmentException.class);

        verify(departmentRepo, never()).save(any());
    }

    @Test
    void update_notFound_throws() {
        when(departmentRepo.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(99, request("X")))
                .isInstanceOf(DepartmentNotFoundException.class);
    }

    // ---------- delete ----------

    @Test
    void delete_noDoctors_deletes() {
        when(departmentRepo.existsById(1)).thenReturn(true);
        when(doctorRepo.existsByDepartmentId(1)).thenReturn(false);

        service.delete(1);

        verify(departmentRepo).deleteById(1);
    }

    @Test
    void delete_notFound_throws() {
        when(departmentRepo.existsById(99)).thenReturn(false);

        assertThatThrownBy(() -> service.delete(99))
                .isInstanceOf(DepartmentNotFoundException.class);

        verify(departmentRepo, never()).deleteById(any());
    }

    @Test
    void delete_doctorsStillAssigned_throwsInUse() {
        when(departmentRepo.existsById(1)).thenReturn(true);
        when(doctorRepo.existsByDepartmentId(1)).thenReturn(true);

        assertThatThrownBy(() -> service.delete(1))
                .isInstanceOf(DepartmentInUseException.class);

        verify(departmentRepo, never()).deleteById(any());
    }

    // ---------- reads ----------

    @Test
    void findById_found_mapsFields() {
        when(departmentRepo.findById(1)).thenReturn(Optional.of(department(1, "Cardiology")));

        DepartmentResponseDTO result = service.findById(1);

        assertThat(result.getId()).isEqualTo(1);
        assertThat(result.getName()).isEqualTo("Cardiology");
        assertThat(result.getPhone()).isEqualTo("044-1234");
    }

    @Test
    void findById_notFound_throws() {
        when(departmentRepo.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(99))
                .isInstanceOf(DepartmentNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void findAll_nullName_usesFindAll() {
        PageRequest pageable = PageRequest.of(0, 5);
        Page<Department> page = new PageImpl<>(List.of(department(1, "Cardiology")), pageable, 1);
        when(departmentRepo.findAll(pageable)).thenReturn(page);

        Page<DepartmentResponseDTO> result = service.findAll(null, pageable);

        assertThat(result.getContent()).hasSize(1);
        verify(departmentRepo, never()).findByNameContainingIgnoreCase(any(), any());
    }

    @Test
    void findAll_blankName_usesFindAll() {
        PageRequest pageable = PageRequest.of(0, 5);
        when(departmentRepo.findAll(pageable)).thenReturn(Page.empty(pageable));

        service.findAll("   ", pageable);

        verify(departmentRepo).findAll(pageable);
    }

    @Test
    void findAll_withName_usesTrimmedSearch() {
        PageRequest pageable = PageRequest.of(0, 5);
        Page<Department> page = new PageImpl<>(List.of(department(2, "Neurology")), pageable, 1);
        when(departmentRepo.findByNameContainingIgnoreCase("neuro", pageable)).thenReturn(page);

        Page<DepartmentResponseDTO> result = service.findAll("  neuro ", pageable);

        assertThat(result.getContent().get(0).getName()).isEqualTo("Neurology");
        verify(departmentRepo, never()).findAll(any(PageRequest.class));
    }
}