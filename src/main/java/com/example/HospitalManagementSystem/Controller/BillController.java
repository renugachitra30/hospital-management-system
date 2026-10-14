package com.example.HospitalManagementSystem.Controller;

import com.example.HospitalManagementSystem.DTO.BillPaymentDTO;
import com.example.HospitalManagementSystem.DTO.BillRequestDTO;
import com.example.HospitalManagementSystem.DTO.BillResponseDTO;
import com.example.HospitalManagementSystem.Enum.BillStatus;
import com.example.HospitalManagementSystem.Service.BillService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/bills")
public class BillController {

    private final BillService billService;

    public BillController(BillService billService) {
        this.billService = billService;
    }

    @PostMapping
    public ResponseEntity<BillResponseDTO> generate(@RequestBody @Valid BillRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(billService.generate(dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BillResponseDTO> findById(@PathVariable int id) {
        return ResponseEntity.ok(billService.findById(id));
    }

    @GetMapping("/appointment/{appointmentId}")
    public ResponseEntity<BillResponseDTO> findByAppointment(@PathVariable int appointmentId) {
        return ResponseEntity.ok(billService.findByAppointment(appointmentId));
    }

    // GET /bills?status=PENDING&page=0&size=10
    @GetMapping
    public ResponseEntity<Page<BillResponseDTO>> findAll(@RequestParam(required = false) BillStatus status, Pageable pageable) {
        return ResponseEntity.ok(billService.findAll(status, pageable));
    }

    @PatchMapping("/{id}/pay")
    public ResponseEntity<BillResponseDTO> pay(@PathVariable int id, @RequestBody @Valid BillPaymentDTO dto) {
        return ResponseEntity.ok(billService.pay(id, dto));
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<BillResponseDTO> cancel(@PathVariable int id) {
        return ResponseEntity.ok(billService.cancel(id));
    }
}