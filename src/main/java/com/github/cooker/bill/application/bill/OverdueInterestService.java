package com.github.cooker.bill.application.bill;

import com.github.cooker.bill.domain.Bill;
import com.github.cooker.bill.domain.service.OverdueInterestCalculator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 逾期利息应用服务：提供配置化默认日利率，并委托领域服务 {@link OverdueInterestCalculator} 做纯计算。
 */
@Service
public class OverdueInterestService {

    private final BigDecimal defaultDailyRate;

    public OverdueInterestService(
            @Value("${bill.default-overdue-daily-rate:0.0005}") BigDecimal defaultDailyRate) {
        BigDecimal rate = defaultDailyRate != null ? defaultDailyRate : new BigDecimal("0.0005");
        this.defaultDailyRate = (rate.compareTo(BigDecimal.ZERO) <= 0) ? new BigDecimal("0.0005") : rate;
    }

    /** 整单逾期利息（使用默认日利率）。 */
    public BigDecimal computeOverdueInterest(Bill bill, LocalDate asOf) {
        return OverdueInterestCalculator.computeBillOverdueInterest(bill, asOf, defaultDailyRate);
    }

    /** 默认逾期日利率（分期计划未配置产品费率时兜底）。 */
    public BigDecimal getDefaultDailyRate() {
        return defaultDailyRate;
    }

    /** 单期逾期利息；dailyRate 可由产品配置或默认利率传入。 */
    public BigDecimal computePeriodOverdueInterest(LocalDate dueDate, BigDecimal plannedAmount, BigDecimal paidAmount,
            LocalDate asOf, BigDecimal dailyRate) {
        return OverdueInterestCalculator.computePeriodOverdueInterest(dueDate, plannedAmount, paidAmount, asOf, dailyRate);
    }
}
