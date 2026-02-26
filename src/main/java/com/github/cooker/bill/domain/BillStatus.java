package com.github.cooker.bill.domain;

/**
 * 账单状态枚举。
 * 未结清(UNPAID)、已结清(PAID)、逾期(OVERDUE)。
 * 未结清时退款、部分还款会触发账单金额动态调整。
 */
public enum BillStatus {
    /** 未结清，可接受部分还款或退款调整 */
    UNPAID,
    /** 已结清 */
    PAID,
    /** 逾期未还 */
    OVERDUE
}
