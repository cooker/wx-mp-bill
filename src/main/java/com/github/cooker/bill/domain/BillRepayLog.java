package com.github.cooker.bill.domain;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * 还款日志（值对象）。
 * 对应表 bill_repay_log。
 */
public record BillRepayLog(
    Long id,
    Long billId,
    String userId,
    BigDecimal amount,
    Instant repaidAt
) {
    public static BillRepayLog of(Long billId, String userId, BigDecimal amount, Instant repaidAt) {
        return new BillRepayLog(null, billId, userId, amount, repaidAt);
    }
}
