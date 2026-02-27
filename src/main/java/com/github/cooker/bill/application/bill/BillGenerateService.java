package com.github.cooker.bill.application.bill;

import com.github.cooker.bill.domain.Bill;
import com.github.cooker.bill.domain.BillOrder;
import com.github.cooker.bill.domain.BillOrderRepository;
import com.github.cooker.bill.domain.BillRepository;
import com.github.cooker.bill.domain.BillStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.List;

/**
 * 账单生成应用服务（供定时任务调用）。
 */
@Service
public class BillGenerateService {

    private final BillOrderRepository orderRepository;
    private final BillRepository billRepository;

    public BillGenerateService(BillOrderRepository orderRepository, BillRepository billRepository) {
        this.orderRepository = orderRepository;
        this.billRepository = billRepository;
    }

    @Transactional
    public int generateBillsForOrdersWithoutBill(List<Long> orderIds) {
        int count = 0;
        for (Long orderId : orderIds) {
            var orderOpt = orderRepository.findById(orderId);
            if (orderOpt.isEmpty()) continue;
            BillOrder order = orderOpt.get();
            if (order.billId() != null) continue;
            if ("REFUND".equals(order.orderType())) continue;
            Instant account = order.accountTime() != null ? order.accountTime() : Instant.now();
            String billMonth = YearMonth.from(account.atZone(ZoneId.systemDefault())).toString();
            Bill bill = billRepository.findUnpaidByUserIdAndBillMonth(order.userId(), billMonth)
                .orElseGet(() -> {
                    LocalDate dueDate = YearMonth.parse(billMonth).plusMonths(1).atEndOfMonth();
                    return billRepository.save(new Bill(
                        null, order.userId(), null, billMonth, "NORMAL", null, null,
                        BigDecimal.ZERO, BigDecimal.ZERO, BillStatus.UNPAID, dueDate, null
                    ));
                });
            BillOrder updatedOrder = new BillOrder(
                order.id(), bill.id(), order.orderNo(), order.merchantOrderNo(), order.orderType(), order.originalOrderId(),
                order.userId(), order.cardNo(), order.tradeTime(), order.tradeCompany(), order.amount(), order.accountTime(),
                order.tradeChannel(), order.tradeType()
            );
            orderRepository.save(updatedOrder);
            BigDecimal add = order.amount() != null ? order.amount() : BigDecimal.ZERO;
            BigDecimal newTotal = (bill.totalAmount() != null ? bill.totalAmount() : BigDecimal.ZERO).add(add);
            billRepository.save(new Bill(
                bill.id(), bill.userId(), bill.productId(), bill.billMonth(), bill.billType(), bill.installmentCount(), bill.installmentDaysPerPeriod(),
                newTotal, bill.paidAmount(), bill.status(), bill.dueDate(), bill.paidAt()
            ));
            count++;
        }
        return count;
    }
}
