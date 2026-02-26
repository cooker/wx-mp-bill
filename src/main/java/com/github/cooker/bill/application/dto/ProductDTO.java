package com.github.cooker.bill.application.dto;

import java.math.BigDecimal;

/** 产品 DTO。分期产品含默认期数、每期天数、分期日利率、分期逾期日利率、每期服务费本金利率。 */
public record ProductDTO(
    Long id, String name, BigDecimal overdueDailyRate, String billType,
    BigDecimal installmentDailyRate, BigDecimal installmentOverdueDailyRate, BigDecimal installmentPerPeriodFeeRate,
    Integer installmentDaysPerPeriod, Integer installmentPeriodCount
) {}
