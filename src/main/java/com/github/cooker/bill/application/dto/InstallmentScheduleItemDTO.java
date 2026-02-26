package com.github.cooker.bill.application.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

/**
 * 分期还款计划单项。
 * 含期号、应还日、应还金额（本金）、状态、已还金额与结清时间；
 * 分期账单按产品配置计算：利息（本期）、服务费、逾期利息。
 */
public record InstallmentScheduleItemDTO(
    int periodNo,
    LocalDate dueDate,
    BigDecimal amount,
    String status,
    BigDecimal paidAmount,
    Instant paidAt,
    BigDecimal interest,
    BigDecimal serviceFee,
    BigDecimal overdueInterest
) {
    public static InstallmentScheduleItemDTO of(int periodNo, LocalDate dueDate, BigDecimal amount, String status,
            BigDecimal paidAmount, Instant paidAt, BigDecimal interest, BigDecimal serviceFee, BigDecimal overdueInterest) {
        return new InstallmentScheduleItemDTO(periodNo, dueDate, amount, status, paidAmount, paidAt,
            interest != null ? interest : BigDecimal.ZERO,
            serviceFee != null ? serviceFee : BigDecimal.ZERO,
            overdueInterest != null ? overdueInterest : BigDecimal.ZERO);
    }
}
