package com.github.cooker.bill.application;

import com.github.cooker.bill.domain.Bill;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.github.cooker.bill.domain.BillStatus;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * 逾期利息计算。
 * 规则：还款日过后超过 3 天才计息（即从第 4 天起算），按未还本金 × 日利率 × 计息天数，保留 2 位小数。
 */
@Service
public class OverdueInterestService {

    private final BigDecimal defaultDailyRate;

    public OverdueInterestService(
            @Value("${bill.default-overdue-daily-rate:0.0005}") BigDecimal defaultDailyRate) {
        BigDecimal rate = defaultDailyRate != null ? defaultDailyRate : new BigDecimal("0.0005");
        this.defaultDailyRate = (rate.compareTo(BigDecimal.ZERO) <= 0) ? new BigDecimal("0.0005") : rate;
    }

    /**
     * 计算截至 asOf 日的逾期利息（仅当逾期超过 3 天时大于 0）。
     * 计息天数 = max(0, asOf - 还款日 - 3)，本金 = 应还 - 已还，利息 = 本金 × 日利率 × 天数。
     */
    public BigDecimal computeOverdueInterest(Bill bill, LocalDate asOf) {
        if (bill == null || bill.dueDate() == null || asOf == null) return BigDecimal.ZERO;
        if (bill.status() == BillStatus.PAID) return BigDecimal.ZERO;
        // 计息起始日 = 还款日 + 4（超过 3 天，即第 4 天起）
        long daysOverdue = ChronoUnit.DAYS.between(bill.dueDate(), asOf);
        long interestDays = Math.max(0, daysOverdue - 3);
        if (interestDays <= 0) return BigDecimal.ZERO;

        BigDecimal unpaid = (bill.totalAmount() != null ? bill.totalAmount() : BigDecimal.ZERO)
            .subtract(bill.paidAmount() != null ? bill.paidAmount() : BigDecimal.ZERO);
        if (unpaid.compareTo(BigDecimal.ZERO) <= 0) return BigDecimal.ZERO;

        BigDecimal rate = defaultDailyRate;
        BigDecimal interest = unpaid.multiply(rate).multiply(BigDecimal.valueOf(interestDays));
        return interest.setScale(2, RoundingMode.HALF_UP);
    }

    /** 返回默认逾期日利率（分期计划未配置产品费率时兜底使用）。 */
    public BigDecimal getDefaultDailyRate() {
        return defaultDailyRate;
    }

    /**
     * 单期逾期利息：应还日后超过 3 天起算，未还本金 × 日利率 × 计息天数。
     * 供分期计划分配还款前重算逾期利息、以及查询展示时使用。
     */
    public BigDecimal computePeriodOverdueInterest(LocalDate dueDate, BigDecimal plannedAmount, BigDecimal paidAmount,
            LocalDate asOf, BigDecimal dailyRate) {
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
