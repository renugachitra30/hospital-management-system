package com.example.HospitalManagementSystem.Repository;

import com.example.HospitalManagementSystem.Entity.Patient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PatientRepo extends JpaRepository<Patient, Integer> {

    // simple filters — derived method names, no @Query needed
    List<Patient> findByNameContainingIgnoreCase(String name);

    List<Patient> findByAgeGreaterThan(int age);

    // combined optional filters — needs @Query, can't be expressed by method name
    @Query("SELECT p FROM Patient p " + "WHERE (:name IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))) " + "AND (:bloodGroup IS NULL OR p.bloodGroup = :bloodGroup) " + "AND (:minAge IS NULL OR p.age >= :minAge) " + "AND (:maxAge IS NULL OR p.age <= :maxAge)")
    Page<Patient> searchPatients(@Param("name") String name, @Param("bloodGroup") String bloodGroup, @Param("minAge") Integer minAge, @Param("maxAge") Integer maxAge, Pageable pageable);
}
