package com.example.HospitalManagementSystem.Entity;

import com.example.HospitalManagementSystem.Enum.BillStatus;
import com.example.HospitalManagementSystem.Enum.PaymentMethod;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "bills")
@Getter
@Setter
@NoArgsConstructor
public class Bill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // one appointment -> at most one bill
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "appointment_id", nullable = false, unique = true)
    private Appointment appointment;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal consultationFee;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal medicineCharges;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BillStatus status = BillStatus.PENDING;

    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;      // null until paid

    private LocalDateTime paidAt;             // null until paid

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Version
    private Long version;                     // optimistic locking

    @PrePersist
    void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}