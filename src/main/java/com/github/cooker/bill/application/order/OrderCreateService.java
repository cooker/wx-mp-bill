package com.github.cooker.bill.application.order;

import com.github.cooker.bill.application.bill.BillInstallmentScheduleService;
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

/**
 * 订单创建应用服务。创建交易明细并归属到对应月账单。
 */
@Service
public class OrderCreateService {

    private final BillOrderRepository orderRepository;
    private final BillRepository billRepository;
    private final BillInstallmentScheduleService scheduleService;

    public OrderCreateService(BillOrderRepository orderRepository, BillRepository billRepository,
            BillInstallmentScheduleService scheduleService) {
        this.orderRepository = orderRepository;
        this.billRepository = billRepository;
        this.scheduleService = scheduleService;
    }

    @Transactional
    public BillOrder create(String orderNo, String merchantOrderNo, String userId, String cardNo, Instant tradeTime, String tradeCompany,
                            BigDecimal amount, Instant accountTime, String tradeChannel, String tradeType,
                            String billType, Integer installmentCount, Integer installmentDaysPerPeriod) {
        Instant account = accountTime != null ? accountTime : tradeTime;
        String billMonth = toBillMonth(account);
        String newBillType = (billType != null && !billType.isBlank()) ? billType : "NORMAL";
        Integer newInstallmentCount = "INSTALLMENT".equals(newBillType) ? installmentCount : null;
        Integer newPeriodDays = "INSTALLMENT".equals(newBillType) ? installmentDaysPerPeriod : null;
        Bill bill = billRepository.findUnpaidByUserIdAndBillMonth(userId, billMonth)
            .orElseGet(() -> createNewBill(userId, billMonth, newBillType, newInstallmentCount, newPeriodDays));
        boolean newBill = (bill.id() == null);
        if (bill.id() == null) {
            bill = billRepository.save(bill);
        }
        BillOrder order = new BillOrder(
            null, bill.id(), orderNo, merchantOrderNo, "NORMAL", null, userId, cardNo, tradeTime, tradeCompany, amount, account, tradeChannel, tradeType
        );
        BillOrder saved = orderRepository.save(order);
        BigDecimal newTotal = (bill.totalAmount() != null ? bill.totalAmount() : BigDecimal.ZERO).add(amount != null ? amount : BigDecimal.ZERO);
        Bill updated = new Bill(
            bill.id(), bill.userId(), bill.productId(), bill.billMonth(), bill.billType(), bill.installmentCount(), bill.installmentDaysPerPeriod(),
            newTotal, bill.paidAmount(), bill.status(), bill.dueDate(), bill.paidAt()
        );
        billRepository.save(updated);
        if (newBill && "INSTALLMENT".equalsIgnoreCase(updated.billType()) && updated.installmentCount() != null && updated.installmentCount() > 0) {
            scheduleService.generateForBill(updated);
        }
        return saved;
    }

    private static String toBillMonth(Instant instant) {
        return YearMonth.from(instant.atZone(ZoneId.systemDefault())).toString();
    }

    private static Bill createNewBill(String userId, String billMonth, String billType, Integer installmentCount, Integer installmentDaysPerPeriod) {
        LocalDate dueDate = YearMonth.parse(billMonth).plusMonths(1).atEndOfMonth();
        String type = (billType != null && !billType.isBlank()) ? billType : "NORMAL";
        Integer periods = "INSTALLMENT".equals(type) ? installmentCount : null;
        Integer periodDays = "INSTALLMENT".equals(type) ? installmentDaysPerPeriod : null;
        return new Bill(
            null, userId, null, billMonth, type, periods, periodDays,
            BigDecimal.ZERO, BigDecimal.ZERO, BillStatus.UNPAID, dueDate, null
        );
    }
}
