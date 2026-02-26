package com.github.cooker.bill.domain;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 账单仓储接口。
 * 支持按用户、还款日、状态查询；未结清账单可更新 total_amount/paid_amount 实现动态调整。
 */
public interface BillRepository {

    Optional<Bill> findById(Long id);

    /** 按用户与还款日范围查未结清/逾期账单（本期应还） */
    List<Bill> findByUserIdAndDueDateBetween(String userId, LocalDate from, LocalDate to);

    /** 按用户与状态分页查历史 */
    List<Bill> findByUserId(String userId, BillStatus status, int offset, int limit);

    /** 还款日早于指定日且状态为 UNPAID 的账单（用于逾期定时任务） */
    List<Bill> findUnpaidWithDueDateBefore(LocalDate before);

    /** 按用户与账单月份查月账单（可能有多张，如已结清后又产生新订单） */
    Optional<Bill> findByUserIdAndBillMonth(String userId, String billMonth);

    /** 按用户与账单月份查未结清账单（UNPAID/OVERDUE）；用于创建订单时归属：有则累加，无则新建一张 */
    Optional<Bill> findUnpaidByUserIdAndBillMonth(String userId, String billMonth);

    Bill save(Bill bill);
}
