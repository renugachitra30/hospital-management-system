package com.example.HospitalManagementSystem.Controller;

import com.example.HospitalManagementSystem.DTO.PatientRequestDTO;
import com.example.HospitalManagementSystem.DTO.PatientResponseDTO;
import com.example.HospitalManagementSystem.Service.PatientService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/patients")
@Validated
public class PatientController {

    @Autowired
    private PatientService patientService;

    @GetMapping("/{id}")
    public ResponseEntity<PatientResponseDTO> findPatientById(@PathVariable int id) {
        PatientResponseDTO patient = patientService.findPatientById(id);
        return ResponseEntity.ok(patient);
    }

    @GetMapping
    public ResponseEntity<Page<PatientResponseDTO>> findAllPatients(Pageable pageable) {
        Page<PatientResponseDTO> patients = patientService.findAll(pageable);
        return ResponseEntity.ok(patients);
    }

    @GetMapping("/search/name")
    public ResponseEntity<List<PatientResponseDTO>> findPatientByName(@RequestParam String name) {
        List<PatientResponseDTO> patients = patientService.findByNameContainingIgnoreCase(name);
        return ResponseEntity.ok(patients);
    }

    @GetMapping("/search/age") //GET /patients/search/age?age=40
    public ResponseEntity<List<PatientResponseDTO>> findByAgeGreaterThan(@RequestParam int age) {
        List<PatientResponseDTO> patients = patientService.findByAgeGreaterThan(age);
        return ResponseEntity.ok(patients);
    }

    @GetMapping("/search/patients")
    public ResponseEntity<Page<PatientResponseDTO>> searchPatients(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String bloodGroup,
            @RequestParam(required = false) Integer minAge,
            @RequestParam(required = false) Integer maxAge,
            Pageable pageable) {
        Page<PatientResponseDTO> patients =
                patientService.searchPatients(
                        name, bloodGroup, minAge, maxAge, pageable);
        return ResponseEntity.ok(patients);
    }

    @PostMapping
    public ResponseEntity<PatientResponseDTO> createPatient(@RequestBody @Valid PatientRequestDTO patientRequestDTO) {
        PatientResponseDTO savedPatient = patientService.savePatient(patientRequestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedPatient);
    }

    @PostMapping("/bulk")
    public ResponseEntity<List<PatientResponseDTO>> saveAllPatients(@RequestBody @Valid List<PatientRequestDTO> patientRequestDTOs) {
        List<PatientResponseDTO> savedPatients = patientService.saveAllPatients(patientRequestDTOs);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedPatients);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PatientResponseDTO> updatePatient(
            @PathVariable int id,
            @RequestBody @Valid PatientRequestDTO patientRequestDTO) {
        PatientResponseDTO update = patientService.updatePatient(id, patientRequestDTO);
        return ResponseEntity.ok(update);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePatientById(@PathVariable int id) {
        patientService.deletePatientById(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteAllPatients() {
        patientService.deleteAllPatients();
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
