package com.github.cooker.bill.domain.service;

import com.github.cooker.bill.domain.Bill;
import com.github.cooker.bill.domain.BillStatus;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * 逾期利息领域服务（纯计算，无依赖）。
 * 规则：还款日/应还日后超过 3 天才计息（第 4 天起算），未还本金 × 日利率 × 计息天数，保留 2 位小数。
 */
public final class OverdueInterestCalculator {

    private OverdueInterestCalculator() {}

    /**
     * 整单逾期利息：截至 asOf 日，计息天数 = max(0, asOf - 还款日 - 3)，本金 = 应还 - 已还。
     */
    public static BigDecimal computeBillOverdueInterest(Bill bill, LocalDate asOf, BigDecimal dailyRate) {
        if (bill == null || bill.dueDate() == null || asOf == null || dailyRate == null) return BigDecimal.ZERO;
        if (bill.status() == BillStatus.PAID) return BigDecimal.ZERO;
        long daysOverdue = ChronoUnit.DAYS.between(bill.dueDate(), asOf);
        long interestDays = Math.max(0, daysOverdue - 3);
        if (interestDays <= 0) return BigDecimal.ZERO;
        BigDecimal unpaid = (bill.totalAmount() != null ? bill.totalAmount() : BigDecimal.ZERO)
            .subtract(bill.paidAmount() != null ? bill.paidAmount() : BigDecimal.ZERO);
        if (unpaid.compareTo(BigDecimal.ZERO) <= 0) return BigDecimal.ZERO;
        return unpaid.multiply(dailyRate).multiply(BigDecimal.valueOf(interestDays)).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * 单期逾期利息：应还日后超过 3 天起算，未还本金 × 日利率 × 计息天数。
     */
    public static BigDecimal computePeriodOverdueInterest(LocalDate dueDate, BigDecimal plannedAmount,
            BigDecimal paidAmount, LocalDate asOf, BigDecimal dailyRate) {
        if (dueDate == null || asOf == null || !asOf.isAfter(dueDate) || dailyRate == null) return BigDecimal.ZERO;
        long daysOverdue = ChronoUnit.DAYS.between(dueDate, asOf);
        long interestDays = Math.max(0, daysOverdue - 3);
        if (interestDays <= 0) return BigDecimal.ZERO;
        BigDecimal unpaid = (plannedAmount != null ? plannedAmount : BigDecimal.ZERO)
            .subtract(paidAmount != null ? paidAmount : BigDecimal.ZERO);
        if (unpaid.compareTo(BigDecimal.ZERO) <= 0) return BigDecimal.ZERO;
        return unpaid.multiply(dailyRate).multiply(BigDecimal.valueOf(interestDays)).setScale(2, RoundingMode.HALF_UP);
    }
}
