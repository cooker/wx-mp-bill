package com.github.cooker.bill.application.bill;

import com.github.cooker.bill.domain.BillOrder;
import com.github.cooker.bill.domain.BillOrderRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 定时任务：账单生成。每日 00:10 执行。
 */
@Component
public class BillGenerateScheduler {

    private final BillOrderRepository orderRepository;
    private final BillGenerateService billGenerateService;

    public BillGenerateScheduler(BillOrderRepository orderRepository, BillGenerateService billGenerateService) {
        this.orderRepository = orderRepository;
        this.billGenerateService = billGenerateService;
    }

    @Scheduled(cron = "${bill.schedule.bill-generate-cron:0 10 0 * * ?}")
    public void generateBills() {
        LocalDate today = LocalDate.now();
        ZonedDateTime zFrom = today.minusMonths(1).atStartOfDay(ZoneId.systemDefault());
        ZonedDateTime zTo = today.minusMonths(1).plusDays(1).atStartOfDay(ZoneId.systemDefault());
        Instant from = zFrom.toInstant();
        Instant to = zTo.toInstant();
        List<BillOrder> orders = orderRepository.findByAccountTimeBetween(from, to);
        List<Long> orderIds = orders.stream().map(BillOrder::id).collect(Collectors.toList());
        billGenerateService.generateBillsForOrdersWithoutBill(orderIds);
    }
}
