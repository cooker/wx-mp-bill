package com.github.cooker.bill.application.dto;

import com.github.cooker.bill.domain.BillStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

/** 账单 DTO。
 * - 含账单类型（正常/分期）、分期期数、每期天数
 * - totalAmount：账单本金总额（订单/退款累加）
 * - totalDueAmount：应还总额；分期账单为计划明细（本金+利息+服务费+逾期利息）累加，正常账单等于 totalAmount
 * - installmentPerAmount：分期账单时的每期应还金额（本金部分）
 * - overdueInterest：逾期利息（仅逾期后产生，整单或计划汇总）
 * - totalInterest / totalServiceFee：分期账单时计划明细的利息、服务费合计
 * - installmentSchedule：分期账单的还款计划表（含实际还款明细），非分期为空列表
 */
public record BillDTO(
    Long id, String userId, Long productId, String billMonth, String billType, Integer installmentCount, Integer installmentDaysPerPeriod,
    BigDecimal totalAmount, BigDecimal totalDueAmount, BigDecimal paidAmount,
    BillStatus status, LocalDate dueDate, Instant paidAt,
    BigDecimal installmentPerAmount,
    BigDecimal overdueInterest,
    BigDecimal totalInterest,
    BigDecimal totalServiceFee,
    List<InstallmentScheduleItemDTO> installmentSchedule
) {}
