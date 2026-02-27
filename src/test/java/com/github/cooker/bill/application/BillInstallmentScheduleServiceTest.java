package com.github.cooker.bill.application;

import com.github.cooker.bill.application.bill.BillInstallmentScheduleService;
import com.github.cooker.bill.domain.BillInstallmentSchedule;
import com.github.cooker.bill.domain.BillInstallmentScheduleRepository;
import com.github.cooker.bill.domain.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 分期还款计划服务单元测试：验证逾期计划在还款时不会把已有的逾期利息覆盖为 0。
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("BillInstallmentScheduleService 单元测试")
class BillInstallmentScheduleServiceTest {

    @Mock
    private BillInstallmentScheduleRepository scheduleRepository;

    @Mock
    private ProductRepository productRepository;

    private BillInstallmentScheduleService scheduleService;

    @BeforeEach
    void setUp() {
        scheduleService = new BillInstallmentScheduleService(scheduleRepository, productRepository);
    }

    @Test
    @DisplayName("分期账单逾期后还款，不会把已有的逾期利息更新为 0")
    void repay_overdueSchedule_shouldNotResetOverdueInterestToZero() {
        Long billId = 8L;

        // 第一期开已经逾期且历史上算出过逾期利息 1.23 元
        BillInstallmentSchedule overdueSchedule = new BillInstallmentSchedule(
            100L, billId, 1,
            LocalDate.of(2026, 2, 1),
            new BigDecimal("33.33"),
            new BigDecimal("33.33"), // 本金已还满
            Instant.parse("2026-02-26T06:08:53.047Z"),
            new BigDecimal("0.50"),
            new BigDecimal("0.50"),
            new BigDecimal("1.23")   // 历史逾期利息
        );

        when(scheduleRepository.findByBillId(billId)).thenReturn(List.of(overdueSchedule));

        // 执行一次还款（金额随意，只要会走到该期循环即可）
        scheduleService.allocatePayment(billId, new BigDecimal("10.00"), Instant.now());

        // 核心断言：不会在还款过程中去更新/覆盖该期的逾期利息
        verify(scheduleRepository, never()).updateOverdueInterest(eq(100L), any(BigDecimal.class));
    }
}

