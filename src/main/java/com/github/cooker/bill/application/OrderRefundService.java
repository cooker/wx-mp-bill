package com.github.cooker.bill.application;

import com.github.cooker.bill.domain.Bill;
import com.github.cooker.bill.domain.BillRepository;
import com.github.cooker.bill.domain.BillOrder;
import com.github.cooker.bill.domain.BillOrderRepository;
import com.github.cooker.bill.domain.BillStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * 订单退款应用服务。
 * 退款时：1）写入退款单（order_type=REFUND，original_order_id=原单id）；2）动态调整关联账单应还总额。
 * 无论账单是否已结清均可退款；已结清账单退款后按新应还额与已还额重新计算状态（已还≥应还时仍为 PAID）。
 */
@Service
public class OrderRefundService {

    private final BillOrderRepository orderRepository;
    private final BillRepository billRepository;

    public OrderRefundService(BillOrderRepository orderRepository, BillRepository billRepository) {
        this.orderRepository = orderRepository;
        this.billRepository = billRepository;
    }

    /**
     * 对指定订单做退款处理：记录退款单（关联原单），并动态调整关联账单金额。
     */
    @Transactional
    public void refund(Long orderId) {
        BillOrder order = orderRepository.findById(orderId).orElseThrow();
        if ("REFUND".equals(order.orderType())) {
            throw new IllegalStateException("该订单为退款单，不能再次退款");
        }
        if (order.billId() == null) {
            throw new IllegalStateException("订单未关联账单，无法退款");
        }
        Bill bill = billRepository.findById(order.billId()).orElseThrow(() -> new IllegalStateException("未找到关联账单"));
        BigDecimal total = bill.totalAmount() != null ? bill.totalAmount() : BigDecimal.ZERO;
        BigDecimal paid = bill.paidAmount() != null ? bill.paidAmount() : BigDecimal.ZERO;
        BigDecimal orderAmount = order.amount() != null ? order.amount() : BigDecimal.ZERO;
        BigDecimal newTotal = total.subtract(orderAmount).max(BigDecimal.ZERO);
        BigDecimal newPaid = paid.min(newTotal);
        // 应还为 0 或 已还≥应还 视为结清；已结清账单退款后也按此规则
        BillStatus newStatus = (newTotal.compareTo(BigDecimal.ZERO) == 0 || newPaid.compareTo(newTotal) >= 0)
            ? BillStatus.PAID
            : bill.status();
        Instant newPaidAt = newStatus == BillStatus.PAID ? (bill.paidAt() != null ? bill.paidAt() : Instant.now()) : null;
        Bill updated = new Bill(
            bill.id(), bill.userId(), bill.productId(), bill.billMonth(), bill.billType(), bill.installmentCount(), bill.installmentDaysPerPeriod(),
            newTotal, newPaid, newStatus, bill.dueDate(), newPaidAt
        );
        billRepository.save(updated);
        // 记录退款单，关联原单，归属同一张月账单
        String refundOrderNo = "REFUND-" + order.orderNo() + "-" + System.currentTimeMillis();
        BillOrder refundOrder = new BillOrder(
            null, order.billId(), refundOrderNo, null, "REFUND", orderId,
            order.userId(), order.cardNo(), Instant.now(), order.tradeCompany(),
            orderAmount, Instant.now(), order.tradeChannel(), "REFUND"
        );
        orderRepository.save(refundOrder);
    }
}
