package com.github.cooker.bill.application;

import com.github.cooker.bill.domain.Bill;
import com.github.cooker.bill.domain.BillRepository;
import com.github.cooker.bill.domain.BillStatus;
import com.github.cooker.bill.domain.BillInstallmentScheduleRepository;
import com.github.cooker.bill.domain.BillRepayLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** 账单还款服务单元测试：部分还款与结清逻辑（测试中逾期利息 mock 为 0）。 */
@ExtendWith(MockitoExtension.class)
@DisplayName("BillRepayService 单元测试")
class BillRepayServiceTest {

    @Mock
    private BillRepository billRepository;

    @Mock
    private OverdueInterestService overdueInterestService;

    @Mock
    private BillInstallmentScheduleService scheduleService;

    @Mock
    private BillInstallmentScheduleRepository scheduleRepository;

    @Mock
    private BillRepayLogRepository repayLogRepository;

    private BillRepayService billRepayService;

    @BeforeEach
    void setUp() {
        billRepayService = new BillRepayService(
            billRepository, overdueInterestService, scheduleService, scheduleRepository, repayLogRepository
        );
    }

    @Test
    @DisplayName("部分还款后已还金额增加，状态仍为 UNPAID")
    void partialRepay_increasesPaidAmount() {
        when(overdueInterestService.computeOverdueInterest(any(), any())).thenReturn(BigDecimal.ZERO);
        Bill bill = new Bill(1L, "u1", null, "2025-06", "NORMAL", null, null, new BigDecimal("1000"), BigDecimal.ZERO, BillStatus.UNPAID, LocalDate.of(2025, 6, 1), null);
        when(billRepository.findById(1L)).thenReturn(Optional.of(bill));
        when(billRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Bill result = billRepayService.repay(1L, new BigDecimal("300"));

        assertThat(result.paidAmount()).isEqualByComparingTo(new BigDecimal("300"));
        assertThat(result.status()).isEqualTo(BillStatus.UNPAID);
        verify(billRepository).save(any());
    }

    @Test
    @DisplayName("还款金额达到应还则结清")
    void fullRepay_marksPaid() {
        when(overdueInterestService.computeOverdueInterest(any(), any())).thenReturn(BigDecimal.ZERO);
        Bill bill = new Bill(1L, "u1", null, "2025-06", "NORMAL", null, null, new BigDecimal("1000"), BigDecimal.ZERO, BillStatus.UNPAID, LocalDate.of(2025, 6, 1), null);
        when(billRepository.findById(1L)).thenReturn(Optional.of(bill));
        when(billRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Bill result = billRepayService.repay(1L, new BigDecimal("1000"));

        assertThat(result.status()).isEqualTo(BillStatus.PAID);
        assertThat(result.paidAt()).isNotNull();
    }

    @Test
    @DisplayName("已结清账单不可再还款")
    void repay_whenPaid_throws() {
        Bill bill = new Bill(1L, "u1", null, "2025-06", "NORMAL", null, null, new BigDecimal("1000"), new BigDecimal("1000"), BillStatus.PAID, LocalDate.of(2025, 6, 1), java.time.Instant.now());
        when(billRepository.findById(1L)).thenReturn(Optional.of(bill));

        assertThatThrownBy(() -> billRepayService.repay(1L, BigDecimal.ONE))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("仅未结清");
    }
}
