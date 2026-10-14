package com.example.HospitalManagementSystem.Controller;

import com.example.HospitalManagementSystem.DTO.PrescriptionRequestDTO;
import com.example.HospitalManagementSystem.DTO.PrescriptionResponseDTO;
import com.example.HospitalManagementSystem.DTO.PrescriptionUpdateDTO;
import com.example.HospitalManagementSystem.Service.PrescriptionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/prescriptions")
public class PrescriptionController {

    private final PrescriptionService prescriptionService;

    public PrescriptionController(PrescriptionService prescriptionService) {
        this.prescriptionService = prescriptionService;
    }

    @PostMapping
    public ResponseEntity<PrescriptionResponseDTO> create(
            @RequestBody @Valid PrescriptionRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(prescriptionService.create(dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PrescriptionResponseDTO> findById(@PathVariable int id) {
        return ResponseEntity.ok(prescriptionService.findById(id));
    }

    @GetMapping("/appointment/{appointmentId}")
    public ResponseEntity<PrescriptionResponseDTO> findByAppointment(
            @PathVariable int appointmentId) {
        return ResponseEntity.ok(prescriptionService.findByAppointment(appointmentId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PrescriptionResponseDTO> update(
            @PathVariable int id,
            @RequestBody @Valid PrescriptionUpdateDTO dto) {
        return ResponseEntity.ok(prescriptionService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable int id) {
        prescriptionService.delete(id);
        return ResponseEntity.noContent().build();
    }
}