package com.example.HospitalManagementSystem.Repository;

import com.example.HospitalManagementSystem.Entity.Appointment;
import com.example.HospitalManagementSystem.Enum.AppointmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface AppointmentRepo extends JpaRepository<Appointment, Integer> {

    // overlap rule: existing.start < new.end AND existing.end > new.start
    @Query("""
            SELECT COUNT(a) > 0 FROM Appointment a
            WHERE a.doctor.id = :doctorId
              AND a.status = :status
              AND a.startTime < :end AND a.endTime > :start
              AND (:excludeId IS NULL OR a.id <> :excludeId)
            """)
    boolean existsDoctorOverlap(@Param("doctorId") Integer doctorId, @Param("status") AppointmentStatus status, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end, @Param("excludeId") Integer excludeId);

    @Query("""
            SELECT COUNT(a) > 0 FROM Appointment a
            WHERE a.patient.id = :patientId
              AND a.status = :status
              AND a.startTime < :end AND a.endTime > :start
              AND (:excludeId IS NULL OR a.id <> :excludeId)
            """)
    boolean existsPatientOverlap(@Param("patientId") Integer patientId, @Param("status") AppointmentStatus status, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end, @Param("excludeId") Integer excludeId);

    @Override
    @EntityGraph(attributePaths = {"doctor", "patient"})
    Page<Appointment> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {"doctor", "patient"})
    Page<Appointment> findByDoctorId(Integer doctorId, Pageable pageable);

    @EntityGraph(attributePaths = {"doctor", "patient"})
    Page<Appointment> findByPatientId(Integer patientId, Pageable pageable);
}