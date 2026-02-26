package com.github.cooker.bill.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

/**
 * 分期还款计划单项（值对象）。
 * 对应表 bill_installment_schedule 的一行；利息、服务费、逾期利息落库保存。
 */
public record BillInstallmentSchedule(
    Long id,
    Long billId,
    int periodNo,
    LocalDate dueDate,
    BigDecimal plannedAmount,
    BigDecimal paidAmount,
    Instant paidAt,
    BigDecimal interest,
    BigDecimal serviceFee,
    BigDecimal overdueInterest
) {
    public static BillInstallmentSchedule of(Long billId, int periodNo, LocalDate dueDate,
            BigDecimal plannedAmount, BigDecimal paidAmount, Instant paidAt,
            BigDecimal interest, BigDecimal serviceFee, BigDecimal overdueInterest) {
        return new BillInstallmentSchedule(null, billId, periodNo, dueDate, plannedAmount,
            paidAmount != null ? paidAmount : BigDecimal.ZERO, paidAt,
            interest != null ? interest : BigDecimal.ZERO,
            serviceFee != null ? serviceFee : BigDecimal.ZERO,
            overdueInterest != null ? overdueInterest : BigDecimal.ZERO);
    }
}
