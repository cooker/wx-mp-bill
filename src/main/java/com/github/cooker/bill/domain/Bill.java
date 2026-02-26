package com.github.cooker.bill.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

/**
 * 账单聚合根。
 * 对应表 bill。billType：NORMAL-正常账单，INSTALLMENT-分期账单；分期时 installmentCount 为期数（如 3/6/12）。
 *
 * @param id               主键
 * @param userId           用户标识
 * @param productId        分期时关联的产品
 * @param billMonth        账单月份 yyyy-MM
 * @param billType         账单类型：NORMAL-正常账单，INSTALLMENT-分期账单
 * @param installmentCount 分期期数，仅分期账单有效，可为 null
 * @param installmentDaysPerPeriod 分期时每期天数，为空则按自然月
 * @param totalAmount      应还总额（动态调整）
 * @param paidAmount       已还金额
 * @param status           状态
 * @param dueDate          还款日
 * @param paidAt           结清时间
 */
public record Bill(
    Long id,
    String userId,
    Long productId,
    String billMonth,
    String billType,
    Integer installmentCount,
    Integer installmentDaysPerPeriod,
    BigDecimal totalAmount,
    BigDecimal paidAmount,
    BillStatus status,
    LocalDate dueDate,
    Instant paidAt
) {}
