package com.example.HospitalManagementSystem.ServiceImpl;

import com.example.HospitalManagementSystem.DTO.PatientRequestDTO;
import com.example.HospitalManagementSystem.DTO.PatientResponseDTO;
import com.example.HospitalManagementSystem.Entity.Patient;
import com.example.HospitalManagementSystem.Exception.PatientNotFoundException;
import com.example.HospitalManagementSystem.Repository.PatientRepo;
import com.example.HospitalManagementSystem.Service.PatientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PatientServiceImpl implements PatientService {

    @Autowired
    private PatientRepo patientRepo;

    @Override
    public PatientResponseDTO findPatientById(int id) {
        Patient patient = patientRepo.findById(id).orElseThrow(() -> new PatientNotFoundException("Patient not found: " + id));
        return mapToResponseDTO(patient);
    }

    @Override
    public Page<PatientResponseDTO> findAll(Pageable pageable) {
        Page<Patient> patientsPage = patientRepo.findAll(pageable);
        return patientsPage.map(this::mapToResponseDTO);
    }

    @Override
    public PatientResponseDTO savePatient(PatientRequestDTO patientRequestDTO) {
        Patient patient = mapToEntity(patientRequestDTO);
        Patient saved = patientRepo.save(patient);
        return mapToResponseDTO(saved);
    }

    @Override
    public List<PatientResponseDTO> saveAllPatients(List<PatientRequestDTO> patientRequestDTOs) {
        List<Patient> patients = patientRequestDTOs.stream().map(this::mapToEntity).collect(Collectors.toList());
        List<Patient> saved = patientRepo.saveAll(patients);
        return saved.stream().map(this::mapToResponseDTO).collect(Collectors.toList());
    }

    @Override
    public void deletePatientById(int id) {
        patientRepo.deleteById(id);
    }

    @Override
    public void deleteAllPatients() {
        patientRepo.deleteAll();
    }

    @Override
    public List<PatientResponseDTO> findByNameContainingIgnoreCase(String name) {
        List<Patient> patient = patientRepo.findByNameContainingIgnoreCase(name);
        return patient.stream().map(this::mapToResponseDTO).collect(Collectors.toList());
    }

    @Override
    public List<PatientResponseDTO> findByAgeGreaterThan(int age) {
        List<Patient> patient = patientRepo.findByAgeGreaterThan(age);
        return patient.stream().map(this::mapToResponseDTO).collect(Collectors.toList());
    }

    @Override
    public Page<PatientResponseDTO> searchPatients(String name, String bloodGroup, Integer minAge, Integer maxAge, Pageable pageable) {
        Page<Patient> patientsPage = patientRepo.searchPatients(name, bloodGroup, minAge, maxAge, pageable);
        return patientsPage.map(this::mapToResponseDTO);
    }

    @Override
    public PatientResponseDTO updatePatient(int id, PatientRequestDTO patientRequestDTO) {
        Patient existingPatient = patientRepo.findById(id).orElseThrow(() -> new PatientNotFoundException("Patient not found with id: " + id));
        existingPatient.setName(patientRequestDTO.getName());
        existingPatient.setAge(patientRequestDTO.getAge());
        existingPatient.setGender(patientRequestDTO.getGender());
        existingPatient.setEmail(patientRequestDTO.getEmail());
        existingPatient.setAddress(patientRequestDTO.getAddress());
        existingPatient.setPhone(patientRequestDTO.getPhone());
        existingPatient.setBloodGroup(patientRequestDTO.getBloodGroup());
        Patient updated = patientRepo.save(existingPatient);
        return mapToResponseDTO(updated);
    }

    // ---- mapping helpers ----

    private PatientResponseDTO mapToResponseDTO(Patient patient) {
        PatientResponseDTO dto = new PatientResponseDTO();
        dto.setId(patient.getId());
        dto.setName(patient.getName());
        dto.setAge(patient.getAge());
        dto.setGender(patient.getGender());
        dto.setEmail(patient.getEmail());
        dto.setAddress(patient.getAddress());
        dto.setPhone(patient.getPhone());
        dto.setBloodGroup(patient.getBloodGroup());
        return dto;
    }

    private Patient mapToEntity(PatientRequestDTO dto) {
        Patient patient = new Patient();
        patient.setName(dto.getName());
        patient.setAge(dto.getAge());
        patient.setGender(dto.getGender());
        patient.setEmail(dto.getEmail());
        patient.setAddress(dto.getAddress());
        patient.setPhone(dto.getPhone());
        patient.setBloodGroup(dto.getBloodGroup());
        return patient;
    }
}