package com.github.cooker.bill.application.bill;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * 定时任务：账单逾期。每日 01:00 执行。
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
