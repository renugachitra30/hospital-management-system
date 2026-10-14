package com.example.HospitalManagementSystem.ServiceImpl;

import com.example.HospitalManagementSystem.DTO.DepartmentRequestDTO;
import com.example.HospitalManagementSystem.DTO.DepartmentResponseDTO;
import com.example.HospitalManagementSystem.Entity.Department;
import com.example.HospitalManagementSystem.Exception.DepartmentInUseException;
import com.example.HospitalManagementSystem.Exception.DepartmentNotFoundException;
import com.example.HospitalManagementSystem.Exception.DuplicateDepartmentException;
import com.example.HospitalManagementSystem.Repository.DepartmentRepo;
import com.example.HospitalManagementSystem.Repository.DoctorRepo;
import com.example.HospitalManagementSystem.Service.DepartmentService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepo departmentRepo;
    private final DoctorRepo doctorRepo;

    public DepartmentServiceImpl(DepartmentRepo departmentRepo, DoctorRepo doctorRepo) {
        this.departmentRepo = departmentRepo;
        this.doctorRepo = doctorRepo;
    }

    @Override
    public DepartmentResponseDTO findById(int id) {
        return toResponse(findOrThrow(id));
    }

    @Override
    public Page<DepartmentResponseDTO> findAll(String name, Pageable pageable) {
        Page<Department> page = (name == null || name.isBlank())
                ? departmentRepo.findAll(pageable)
                : departmentRepo.findByNameContainingIgnoreCase(name.trim(), pageable);
        return page.map(this::toResponse);
    }

    @Override
    @Transactional
    public DepartmentResponseDTO create(DepartmentRequestDTO dto) {
        String name = dto.getName().trim();
        if (departmentRepo.existsByNameIgnoreCase(name)) {
            throw new DuplicateDepartmentException("Department already exists: " + name);
        }
        Department department = new Department();
        copyFields(dto, department);
        return toResponse(departmentRepo.save(department));
    }

    @Override
    @Transactional
    public List<DepartmentResponseDTO> createAll(List<DepartmentRequestDTO> dtos) {
        Set<String> seen = new HashSet<>();
        List<Department> departments = new ArrayList<>();

        for (DepartmentRequestDTO dto : dtos) {
            String name = dto.getName().trim();
            if (!seen.add(name.toLowerCase())) {
                throw new DuplicateDepartmentException("Duplicate department in request: " + name);
            }
            if (departmentRepo.existsByNameIgnoreCase(name)) {
                throw new DuplicateDepartmentException("Department already exists: " + name);
            }
            Department d = new Department();
            copyFields(dto, d);
            departments.add(d);
        }

        return departmentRepo.saveAll(departments).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public DepartmentResponseDTO update(int id, DepartmentRequestDTO dto) {
        Department department = findOrThrow(id);
        String name = dto.getName().trim();
        if (departmentRepo.existsByNameIgnoreCaseAndIdNot(name, id)) {
            throw new DuplicateDepartmentException("Department already exists: " + name);
        }
        copyFields(dto, department);
        return toResponse(departmentRepo.save(department));
    }

    @Override
    @Transactional
    public void delete(int id) {
        if (!departmentRepo.existsById(id)) {
            throw new DepartmentNotFoundException("Department not found: " + id);
        }
        if (doctorRepo.existsByDepartmentId(id)) {
            throw new DepartmentInUseException(
                    "Cannot delete department: doctors are still assigned to it");
        }
        departmentRepo.deleteById(id);
    }

    // ---------- helpers ----------

    private Department findOrThrow(int id) {
        return departmentRepo.findById(id)
                .orElseThrow(() -> new DepartmentNotFoundException("Department not found: " + id));
    }

    private void copyFields(DepartmentRequestDTO dto, Department d) {
        d.setName(dto.getName().trim());
        d.setDescription(dto.getDescription());
        d.setLocation(dto.getLocation());
        d.setPhone(dto.getPhone());
    }

    private DepartmentResponseDTO toResponse(Department d) {
        return new DepartmentResponseDTO(
                d.getId(), d.getName(), d.getDescription(), d.getLocation(), d.getPhone());
    }
}