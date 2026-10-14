package com.example.HospitalManagementSystem;

import com.example.HospitalManagementSystem.DTO.BillPaymentDTO;
import com.example.HospitalManagementSystem.DTO.BillRequestDTO;
import com.example.HospitalManagementSystem.DTO.BillResponseDTO;
import com.example.HospitalManagementSystem.Entity.Appointment;
import com.example.HospitalManagementSystem.Entity.Bill;
import com.example.HospitalManagementSystem.Entity.Doctor;
import com.example.HospitalManagementSystem.Entity.Patient;
import com.example.HospitalManagementSystem.Enum.AppointmentStatus;
import com.example.HospitalManagementSystem.Enum.BillStatus;
import com.example.HospitalManagementSystem.Enum.PaymentMethod;
import com.example.HospitalManagementSystem.Exception.*;
import com.example.HospitalManagementSystem.Repository.AppointmentRepo;
import com.example.HospitalManagementSystem.Repository.BillRepo;
import com.example.HospitalManagementSystem.ServiceImpl.BillServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BillServiceImplTest {

    @Mock
    private BillRepo billRepo;
    @Mock
    private AppointmentRepo appointmentRepo;

    @InjectMocks
    private BillServiceImpl service;

    private Doctor doctor;
    private Patient patient;

    @BeforeEach
    void setUp() {
        doctor = new Doctor();
        doctor.setId(2);
        doctor.setName("Dr. Priya");
        doctor.setConsultationFee(new BigDecimal("800.00"));

        patient = new Patient();
        patient.setId(10);
        patient.setName("Anu");
    }

    // ---------- helpers ----------

    private Appointment appointment(int id, AppointmentStatus status) {
        Appointment a = new Appointment();
        a.setId(id);
        a.setDoctor(doctor);
        a.setPatient(patient);
        a.setStatus(status);
        return a;
    }

    private Bill bill(int id, BillStatus status) {
        Bill b = new Bill();
        b.setId(id);
        b.setAppointment(appointment(3, AppointmentStatus.COMPLETED));
        b.setConsultationFee(new BigDecimal("800.00"));
        b.setMedicineCharges(new BigDecimal("250.50"));
        b.setTotalAmount(new BigDecimal("1050.50"));
        b.setStatus(status);
        return b;
    }

    // ---------- generate ----------

    @Test
    void generate_success_totalIsFeePlusMedicine() {
        when(appointmentRepo.findById(3))
                .thenReturn(Optional.of(appointment(3, AppointmentStatus.COMPLETED)));
        when(billRepo.existsByAppointmentId(3)).thenReturn(false);
        when(billRepo.save(any(Bill.class))).thenAnswer(inv -> inv.getArgument(0));

        BillResponseDTO result =
                service.generate(new BillRequestDTO(3, new BigDecimal("250.50")));

        assertThat(result.getConsultationFee()).isEqualByComparingTo("800.00");
        assertThat(result.getMedicineCharges()).isEqualByComparingTo("250.50");
        assertThat(result.getTotalAmount()).isEqualByComparingTo("1050.50");
        assertThat(result.getStatus()).isEqualTo(BillStatus.PENDING);
        assertThat(result.getPatientName()).isEqualTo("Anu");
        assertThat(result.getDoctorName()).isEqualTo("Dr. Priya");
    }

    @Test
    void generate_medicineChargesNull_treatedAsZero() {
        when(appointmentRepo.findById(3))
                .thenReturn(Optional.of(appointment(3, AppointmentStatus.COMPLETED)));
        when(billRepo.existsByAppointmentId(3)).thenReturn(false);
        when(billRepo.save(any(Bill.class))).thenAnswer(inv -> inv.getArgument(0));

        BillResponseDTO result = service.generate(new BillRequestDTO(3, null));

        assertThat(result.getMedicineCharges()).isEqualByComparingTo("0");
        assertThat(result.getTotalAmount()).isEqualByComparingTo("800.00");
    }

    @Test
    void generate_savesSnapshotOfFee() {
        when(appointmentRepo.findById(3))
                .thenReturn(Optional.of(appointment(3, AppointmentStatus.COMPLETED)));
        when(billRepo.existsByAppointmentId(3)).thenReturn(false);
        when(billRepo.save(any(Bill.class))).thenAnswer(inv -> inv.getArgument(0));

        service.generate(new BillRequestDTO(3, BigDecimal.TEN));

        ArgumentCaptor<Bill> captor = ArgumentCaptor.forClass(Bill.class);
        verify(billRepo).save(captor.capture());
        Bill saved = captor.getValue();

        assertThat(saved.getConsultationFee()).isEqualByComparingTo("800.00");
        assertThat(saved.getTotalAmount()).isEqualByComparingTo("810.00");
        assertThat(saved.getStatus()).isEqualTo(BillStatus.PENDING);
    }

    @Test
    void generate_appointmentNotFound_throws() {
        when(appointmentRepo.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.generate(new BillRequestDTO(99, null)))
                .isInstanceOf(AppointmentNotFoundException.class);

        verify(billRepo, never()).save(any());
    }

    @Test
    void generate_appointmentNotCompleted_throwsInvalidState() {
        when(appointmentRepo.findById(2))
                .thenReturn(Optional.of(appointment(2, AppointmentStatus.SCHEDULED)));

        assertThatThrownBy(() -> service.generate(new BillRequestDTO(2, null)))
                .isInstanceOf(InvalidAppointmentStateException.class)
                .hasMessageContaining("COMPLETED");

        verify(billRepo, never()).save(any());
    }

    @Test
    void generate_billAlreadyExists_throwsDuplicate() {
        when(appointmentRepo.findById(3))
                .thenReturn(Optional.of(appointment(3, AppointmentStatus.COMPLETED)));
        when(billRepo.existsByAppointmentId(3)).thenReturn(true);

        assertThatThrownBy(() -> service.generate(new BillRequestDTO(3, null)))
                .isInstanceOf(DuplicateBillException.class);

        verify(billRepo, never()).save(any());
    }

    @Test
    void generate_feeNotSet_throwsInvalidBillState() {
        doctor.setConsultationFee(null);
        when(appointmentRepo.findById(3))
                .thenReturn(Optional.of(appointment(3, AppointmentStatus.COMPLETED)));
        when(billRepo.existsByAppointmentId(3)).thenReturn(false);

        assertThatThrownBy(() -> service.generate(new BillRequestDTO(3, null)))
                .isInstanceOf(InvalidBillStateException.class)
                .hasMessageContaining("Dr. Priya");

        verify(billRepo, never()).save(any());
    }

    // ---------- pay ----------

    @Test
    void pay_pending_becomesPaid() {
        Bill b = bill(1, BillStatus.PENDING);
        when(billRepo.findById(1)).thenReturn(Optional.of(b));

        BillResponseDTO result = service.pay(1, new BillPaymentDTO(PaymentMethod.UPI));

        assertThat(result.getStatus()).isEqualTo(BillStatus.PAID);
        assertThat(result.getPaymentMethod()).isEqualTo(PaymentMethod.UPI);
        assertThat(result.getPaidAt()).isNotNull();
    }

    @Test
    void pay_alreadyPaid_throwsInvalidState() {
        when(billRepo.findById(1)).thenReturn(Optional.of(bill(1, BillStatus.PAID)));

        assertThatThrownBy(() -> service.pay(1, new BillPaymentDTO(PaymentMethod.CASH)))
                .isInstanceOf(InvalidBillStateException.class)
                .hasMessageContaining("PENDING");
    }

    @Test
    void pay_cancelled_throwsInvalidState() {
        when(billRepo.findById(1)).thenReturn(Optional.of(bill(1, BillStatus.CANCELLED)));

        assertThatThrownBy(() -> service.pay(1, new BillPaymentDTO(PaymentMethod.CARD)))
                .isInstanceOf(InvalidBillStateException.class);
    }

    @Test
    void pay_notFound_throws() {
        when(billRepo.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.pay(99, new BillPaymentDTO(PaymentMethod.CASH)))
                .isInstanceOf(BillNotFoundException.class);
    }

    // ---------- cancel ----------

    @Test
    void cancel_pending_becomesCancelled() {
        when(billRepo.findById(1)).thenReturn(Optional.of(bill(1, BillStatus.PENDING)));

        BillResponseDTO result = service.cancel(1);

        assertThat(result.getStatus()).isEqualTo(BillStatus.CANCELLED);
    }

    @Test
    void cancel_paid_throwsInvalidState() {
        when(billRepo.findById(1)).thenReturn(Optional.of(bill(1, BillStatus.PAID)));

        assertThatThrownBy(() -> service.cancel(1))
                .isInstanceOf(InvalidBillStateException.class);
    }

    // ---------- reads ----------

    @Test
    void findById_found_mapsFields() {
        when(billRepo.findById(1)).thenReturn(Optional.of(bill(1, BillStatus.PENDING)));

        BillResponseDTO result = service.findById(1);

        assertThat(result.getId()).isEqualTo(1);
        assertThat(result.getAppointmentId()).isEqualTo(3);
        assertThat(result.getTotalAmount()).isEqualByComparingTo("1050.50");
    }

    @Test
    void findById_notFound_throws() {
        when(billRepo.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(99))
                .isInstanceOf(BillNotFoundException.class);
    }

    @Test
    void findByAppointment_notFound_throws() {
        when(billRepo.findByAppointmentId(7)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findByAppointment(7))
                .isInstanceOf(BillNotFoundException.class)
                .hasMessageContaining("7");
    }

    @Test
    void findAll_noStatus_usesFindAll() {
        PageRequest pageable = PageRequest.of(0, 5);
        Page<Bill> page = new PageImpl<>(List.of(bill(1, BillStatus.PENDING)), pageable, 1);
        when(billRepo.findAll(pageable)).thenReturn(page);

        Page<BillResponseDTO> result = service.findAll(null, pageable);

        assertThat(result.getContent()).hasSize(1);
        verify(billRepo, never()).findByStatus(any(), any());
    }

    @Test
    void findAll_withStatus_usesFindByStatus() {
        PageRequest pageable = PageRequest.of(0, 5);
        Page<Bill> page = new PageImpl<>(List.of(bill(1, BillStatus.PAID)), pageable, 1);
        when(billRepo.findByStatus(BillStatus.PAID, pageable)).thenReturn(page);

        Page<BillResponseDTO> result = service.findAll(BillStatus.PAID, pageable);

        assertThat(result.getContent().get(0).getStatus()).isEqualTo(BillStatus.PAID);
        verify(billRepo, never()).findAll(any(PageRequest.class));
    }
}