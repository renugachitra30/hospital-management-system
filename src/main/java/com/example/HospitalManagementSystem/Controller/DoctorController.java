package com.example.HospitalManagementSystem.Controller;

import com.example.HospitalManagementSystem.DTO.DoctorRequestDTO;
import com.example.HospitalManagementSystem.DTO.DoctorResponseDTO;
import com.example.HospitalManagementSystem.Service.DoctorService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/doctors")
@Validated
public class DoctorController {

    private final DoctorService doctorService;

    public DoctorController(DoctorService doctorService) {
        this.doctorService = doctorService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<DoctorResponseDTO> findDoctorById(@PathVariable int id) {
        DoctorResponseDTO doctor = doctorService.findDoctorById(id);
        return ResponseEntity.ok(doctor);
    }

    @GetMapping
    public ResponseEntity<Page<DoctorResponseDTO>> findAllDoctors(Pageable pageable) {
        Page<DoctorResponseDTO> doctors = doctorService.findAll(pageable);
        return ResponseEntity.ok(doctors);
    }

    @GetMapping("/search/name")
    public ResponseEntity<List<DoctorResponseDTO>> findDoctorByName(@RequestParam String name) {
        List<DoctorResponseDTO> doctors = doctorService.findByNameContainingIgnoreCase(name);
        return ResponseEntity.ok(doctors);
    }

    @GetMapping("/search/doctors")
    public ResponseEntity<Page<DoctorResponseDTO>> searchDoctors(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) Integer minExperience,
            @RequestParam(required = false) Integer maxExperience,
            Pageable pageable) {
        Page<DoctorResponseDTO> doctors =
                doctorService.searchDoctors(
                        name, department, minExperience, maxExperience, pageable);
        return ResponseEntity.ok(doctors);
    }

    @PostMapping
    public ResponseEntity<DoctorResponseDTO> createDoctor(@RequestBody @Valid DoctorRequestDTO doctorRequestDTO) {
        DoctorResponseDTO savedDoctor = doctorService.saveDoctor(doctorRequestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedDoctor);
    }

    @PostMapping("/bulk")
    public ResponseEntity<List<DoctorResponseDTO>> saveAllDoctors(@RequestBody List<@Valid DoctorRequestDTO> doctorRequestDTO) {
        List<DoctorResponseDTO> savedDoctors = doctorService.saveAllDoctors(doctorRequestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedDoctors);
    }

    @PutMapping("/{id}")
    public ResponseEntity<DoctorResponseDTO> updateDoctor(
            @PathVariable int id,
            @RequestBody @Valid DoctorRequestDTO doctorRequestDTO) {
        DoctorResponseDTO update = doctorService.updateDoctor(id, doctorRequestDTO);
        return ResponseEntity.ok(update);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDoctorById(@PathVariable int id) {
        doctorService.deleteDoctorById(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteAllDoctors() {
        doctorService.deleteAllDoctors();
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
