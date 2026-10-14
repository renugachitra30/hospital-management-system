package com.example.HospitalManagementSystem.Controller;

import com.example.HospitalManagementSystem.DTO.AppointmentRequestDTO;
import com.example.HospitalManagementSystem.DTO.AppointmentRescheduleDTO;
import com.example.HospitalManagementSystem.DTO.AppointmentResponseDTO;
import com.example.HospitalManagementSystem.Service.AppointmentService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;

    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @PostMapping
    public ResponseEntity<AppointmentResponseDTO> book(@RequestBody @Valid AppointmentRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(appointmentService.book(dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AppointmentResponseDTO> findById(@PathVariable int id) {
        return ResponseEntity.ok(appointmentService.findById(id));
    }

    @GetMapping
    public ResponseEntity<Page<AppointmentResponseDTO>> findAll(Pageable pageable) {
        return ResponseEntity.ok(appointmentService.findAll(pageable));
    }

    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<Page<AppointmentResponseDTO>> findByDoctor(@PathVariable int doctorId, Pageable pageable) {
        return ResponseEntity.ok(appointmentService.findByDoctor(doctorId, pageable));
    }

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<Page<AppointmentResponseDTO>> findByPatient(@PathVariable int patientId, Pageable pageable) {
        return ResponseEntity.ok(appointmentService.findByPatient(patientId, pageable));
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<AppointmentResponseDTO> cancel(@PathVariable int id) {
        return ResponseEntity.ok(appointmentService.cancel(id));
    }

    @PatchMapping("/{id}/complete")
    public ResponseEntity<AppointmentResponseDTO> complete(@PathVariable int id) {
        return ResponseEntity.ok(appointmentService.complete(id));
    }

    @PutMapping("/{id}/reschedule")
    public ResponseEntity<AppointmentResponseDTO> reschedule(@PathVariable int id, @RequestBody @Valid AppointmentRescheduleDTO dto) {
        return ResponseEntity.ok(appointmentService.reschedule(id, dto));
    }
}