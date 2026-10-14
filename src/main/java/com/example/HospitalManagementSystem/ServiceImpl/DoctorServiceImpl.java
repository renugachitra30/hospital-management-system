package com.example.HospitalManagementSystem.ServiceImpl;

import com.example.HospitalManagementSystem.DTO.DoctorRequestDTO;
import com.example.HospitalManagementSystem.DTO.DoctorResponseDTO;
import com.example.HospitalManagementSystem.Entity.Department;
import com.example.HospitalManagementSystem.Entity.Doctor;
import com.example.HospitalManagementSystem.Exception.DepartmentNotFoundException;
import com.example.HospitalManagementSystem.Exception.DoctorNotFoundException;
import com.example.HospitalManagementSystem.Exception.DuplicateDoctorException;
import com.example.HospitalManagementSystem.Repository.DepartmentRepo;
import com.example.HospitalManagementSystem.Repository.DoctorRepo;
import com.example.HospitalManagementSystem.Service.DoctorService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class DoctorServiceImpl implements DoctorService {

    private final DoctorRepo doctorRepo;
    private final DepartmentRepo departmentRepo;

    public DoctorServiceImpl(DoctorRepo doctorRepo, DepartmentRepo departmentRepo) {
        this.doctorRepo = doctorRepo;
        this.departmentRepo = departmentRepo;
    }

    @Override
    public DoctorResponseDTO findDoctorById(int id) {
        return mapToResponseDTO(findOrThrow(id));
    }

    @Override
    public Page<DoctorResponseDTO> findAll(Pageable pageable) {
        return doctorRepo.findAll(pageable).map(this::mapToResponseDTO);
    }

    @Override
    public List<DoctorResponseDTO> findByNameContainingIgnoreCase(String name) {
        return doctorRepo.findByNameContainingIgnoreCase(name)
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Page<DoctorResponseDTO> searchDoctors(
            String name, String department,
            Integer minExperience, Integer maxExperience,
            Pageable pageable) {
        return doctorRepo.searchDoctors(
                        name, department, minExperience, maxExperience, pageable)
                .map(this::mapToResponseDTO);
    }

    @Override
    @Transactional
    public DoctorResponseDTO saveDoctor(DoctorRequestDTO dto) {
        validateNoDuplicatesForCreate(dto);
        Department department = findDepartment(dto.getDepartmentId());
        return mapToResponseDTO(doctorRepo.save(mapToEntity(dto, department)));
    }

    @Override
    @Transactional
    public List<DoctorResponseDTO> saveAllDoctors(List<DoctorRequestDTO> dtos) {
        Set<String> licenses = new HashSet<>();
        Set<String> emails = new HashSet<>();

        for (DoctorRequestDTO dto : dtos) {
            // duplicates inside the same request
            if (!licenses.add(dto.getLicenseNumber())) {
                throw new DuplicateDoctorException(
                        "Duplicate license number in request: " + dto.getLicenseNumber());
            }
            if (!emails.add(dto.getEmail())) {
                throw new DuplicateDoctorException(
                        "Duplicate email in request: " + dto.getEmail());
            }
            // duplicates against the database
            validateNoDuplicatesForCreate(dto);
        }

        // load each department only once
        Map<Integer, Department> departments = new HashMap<>();

        List<Doctor> doctors = dtos.stream()
                .map(dto -> mapToEntity(dto,
                        departments.computeIfAbsent(dto.getDepartmentId(), this::findDepartment)))
                .collect(Collectors.toList());

        return doctorRepo.saveAll(doctors).stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public DoctorResponseDTO updateDoctor(int id, DoctorRequestDTO dto) {
        Doctor existing = findOrThrow(id);

        // exclude the doctor being updated
        if (doctorRepo.existsByEmailAndIdNot(dto.getEmail(), id)) {
            throw new DuplicateDoctorException(
                    "Email already exists: " + dto.getEmail());
        }
        if (doctorRepo.existsByLicenseNumberAndIdNot(dto.getLicenseNumber(), id)) {
            throw new DuplicateDoctorException(
                    "License number already exists: " + dto.getLicenseNumber());
        }

        Department department = findDepartment(dto.getDepartmentId());
        copyFields(dto, existing, department);
        return mapToResponseDTO(doctorRepo.save(existing));
    }

    @Override
    @Transactional
    public void deleteDoctorById(int id) {
        if (!doctorRepo.existsById(id)) {
            throw new DoctorNotFoundException("Doctor not found: " + id);
        }
        doctorRepo.deleteById(id);
    }

    @Override
    @Transactional
    public void deleteAllDoctors() {
        doctorRepo.deleteAll();
    }

    // ---------- helpers ----------

    private Doctor findOrThrow(int id) {
        return doctorRepo.findById(id)
                .orElseThrow(() -> new DoctorNotFoundException("Doctor not found: " + id));
    }

    private Department findDepartment(Integer id) {
        return departmentRepo.findById(id)
                .orElseThrow(() -> new DepartmentNotFoundException("Department not found: " + id));
    }

    private void validateNoDuplicatesForCreate(DoctorRequestDTO dto) {
        if (doctorRepo.existsByLicenseNumber(dto.getLicenseNumber())) {
            throw new DuplicateDoctorException(
                    "License number already exists: " + dto.getLicenseNumber());
        }
        if (doctorRepo.existsByEmail(dto.getEmail())) {
            throw new DuplicateDoctorException(
                    "Email already exists: " + dto.getEmail());
        }
    }

    private void copyFields(DoctorRequestDTO dto, Doctor doctor, Department department) {
        doctor.setName(dto.getName());
        doctor.setAge(dto.getAge());
        doctor.setGender(dto.getGender());
        doctor.setDepartment(department);
        doctor.setEmail(dto.getEmail());
        doctor.setPhone(dto.getPhone());
        doctor.setAddress(dto.getAddress());
        doctor.setExperience(dto.getExperience());
        doctor.setLicenseNumber(dto.getLicenseNumber());
        doctor.setSalary(dto.getSalary());
        doctor.setConsultationFee(dto.getConsultationFee());
    }

    private Doctor mapToEntity(DoctorRequestDTO dto, Department department) {
        Doctor doctor = new Doctor();
        copyFields(dto, doctor, department);
        return doctor;
    }

    private DoctorResponseDTO mapToResponseDTO(Doctor d) {
        DoctorResponseDTO dto = new DoctorResponseDTO();
        dto.setId(d.getId());
        dto.setName(d.getName());
        dto.setAge(d.getAge());
        dto.setGender(d.getGender());
        dto.setDepartmentId(d.getDepartment().getId());
        dto.setDepartmentName(d.getDepartment().getName());
        dto.setEmail(d.getEmail());
        dto.setPhone(d.getPhone());
        dto.setAddress(d.getAddress());
        dto.setExperience(d.getExperience());
        dto.setLicenseNumber(d.getLicenseNumber());
        dto.setSalary(d.getSalary());
        dto.setConsultationFee(d.getConsultationFee());
        return dto;
    }
}