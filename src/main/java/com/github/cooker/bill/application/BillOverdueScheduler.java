package com.github.cooker.bill.application;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * 定时任务：账单逾期。
 * 每日 01:00 执行，将还款日早于今日且状态为 UNPAID 的账单标记为 OVERDUE。
 */
@Component
public class BillOverdueScheduler {

    private final BillOverdueService billOverdueService;

    public BillOverdueScheduler(BillOverdueService billOverdueService) {
        this.billOverdueService = billOverdueService;
    }

    @Scheduled(cron = "${bill.schedule.bill-overdue-cron:0 0 1 * * ?}")
    public void markOverdueBills() {
        billOverdueService.markAllOverdueAsOf(LocalDate.now());
    }
}
