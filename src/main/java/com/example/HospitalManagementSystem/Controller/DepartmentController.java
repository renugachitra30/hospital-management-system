package com.example.HospitalManagementSystem.Controller;

import com.example.HospitalManagementSystem.DTO.DepartmentRequestDTO;
import com.example.HospitalManagementSystem.DTO.DepartmentResponseDTO;
import com.example.HospitalManagementSystem.Service.DepartmentService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/departments")
@Validated
public class DepartmentController {

    private final DepartmentService departmentService;

    public DepartmentController(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<DepartmentResponseDTO> findById(@PathVariable int id) {
        return ResponseEntity.ok(departmentService.findById(id));
    }

    @GetMapping
    public ResponseEntity<Page<DepartmentResponseDTO>> findAll(
            @RequestParam(required = false) String name,
            Pageable pageable) {
        return ResponseEntity.ok(departmentService.findAll(name, pageable));
    }

    @PostMapping
    public ResponseEntity<DepartmentResponseDTO> create(
            @RequestBody @Valid DepartmentRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(departmentService.create(dto));
    }

    @PostMapping("/bulk")
    public ResponseEntity<List<DepartmentResponseDTO>> createAll(
            @RequestBody List<@Valid DepartmentRequestDTO> dtos) {
        return ResponseEntity.status(HttpStatus.CREATED).body(departmentService.createAll(dtos));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DepartmentResponseDTO> update(
            @PathVariable int id,
            @RequestBody @Valid DepartmentRequestDTO dto) {
        return ResponseEntity.ok(departmentService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable int id) {
        departmentService.delete(id);
        return ResponseEntity.noContent().build();
    }
}