package com.github.cooker.bill.domain;

import java.math.BigDecimal;

/**
 * 产品聚合根（值对象）。
 * 对应表 product。分期产品可配置默认期数、每期天数、分期日利率、分期逾期日利率、每期服务费本金利率。
 *
 * @param id                          主键
 * @param name                        产品名称
 * @param overdueDailyRate            逾期日利率，如 0.0005 表示 0.05%/天
 * @param billType                    账单类型：NORMAL-正常账单，INSTALLMENT-分期账单
 * @param installmentDailyRate        分期日利率
 * @param installmentOverdueDailyRate 分期逾期日利率
 * @param installmentPerPeriodFeeRate  每期服务费本金利率，如 0.0001 表示按本金 0.01%/期
 * @param installmentDaysPerPeriod    每期天数，如 30 表示每 30 天为一期
 * @param installmentPeriodCount     分期默认期数，如 3/6/12，仅分期产品有效
 */
public record Product(
    Long id, String name, BigDecimal overdueDailyRate, String billType,
    BigDecimal installmentDailyRate, BigDecimal installmentOverdueDailyRate, BigDecimal installmentPerPeriodFeeRate,
    Integer installmentDaysPerPeriod, Integer installmentPeriodCount
) {}
