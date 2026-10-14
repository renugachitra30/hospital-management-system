package com.example.HospitalManagementSystem.Repository;

import com.example.HospitalManagementSystem.Entity.Prescription;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PrescriptionRepo extends JpaRepository<Prescription, Integer> {

    boolean existsByAppointmentId(Integer appointmentId);

    @EntityGraph(attributePaths = {"items", "appointment.patient", "appointment.doctor"})
    Optional<Prescription> findByAppointmentId(Integer appointmentId);

    @Override
    @EntityGraph(attributePaths = {"items", "appointment.patient", "appointment.doctor"})
    Optional<Prescription> findById(Integer id);
}