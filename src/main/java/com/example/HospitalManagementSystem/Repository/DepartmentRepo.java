package com.example.HospitalManagementSystem.Repository;

import com.example.HospitalManagementSystem.Entity.Department;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DepartmentRepo extends JpaRepository<Department, Integer> {
    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Integer id);

    Page<Department> findByNameContainingIgnoreCase(String name, Pageable pageable);
}