package com.example.HospitalManagementSystem.Repository;

import com.example.HospitalManagementSystem.Entity.Doctor;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DoctorRepo extends JpaRepository<Doctor, Integer> {

    // fetch department together with doctors (avoids N+1)
    @Override
    @EntityGraph(attributePaths = "department")
    Page<Doctor> findAll(Pageable pageable);

    @EntityGraph(attributePaths = "department")
    List<Doctor> findByNameContainingIgnoreCase(String name);

    boolean existsByLicenseNumber(String licenseNumber);

    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, Integer id);

    boolean existsByLicenseNumberAndIdNot(String licenseNumber, Integer id);

    boolean existsByDepartmentId(Integer departmentId);

    @EntityGraph(attributePaths = "department")
    @Query("""
            SELECT d FROM Doctor d
            WHERE (:name IS NULL
                   OR LOWER(d.name) LIKE LOWER(CONCAT('%', :name, '%')))
              AND (:department IS NULL
                   OR LOWER(d.department.name) = LOWER(:department))
              AND (:minExperience IS NULL OR d.experience >= :minExperience)
              AND (:maxExperience IS NULL OR d.experience <= :maxExperience)
            """)
    Page<Doctor> searchDoctors(@Param("name") String name, @Param("department") String department, @Param("minExperience") Integer minExperience, @Param("maxExperience") Integer maxExperience, Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT d FROM Doctor d WHERE d.id = :id")
    Optional<Doctor> findByIdForUpdate(@Param("id") Integer id);
}