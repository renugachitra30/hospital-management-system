package com.example.HospitalManagementSystem.Repository;

import com.example.HospitalManagementSystem.Entity.Bill;
import com.example.HospitalManagementSystem.Enum.BillStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BillRepo extends JpaRepository<Bill, Integer> {

    boolean existsByAppointmentId(Integer appointmentId);

    @EntityGraph(attributePaths = {"appointment.patient", "appointment.doctor"})
    Optional<Bill> findByAppointmentId(Integer appointmentId);

    @Override
    @EntityGraph(attributePaths = {"appointment.patient", "appointment.doctor"})
    Optional<Bill> findById(Integer id);

    @Override
    @EntityGraph(attributePaths = {"appointment.patient", "appointment.doctor"})
    Page<Bill> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {"appointment.patient", "appointment.doctor"})
    Page<Bill> findByStatus(BillStatus status, Pageable pageable);
}