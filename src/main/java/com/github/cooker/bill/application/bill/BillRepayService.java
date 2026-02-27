package com.github.cooker.bill.application.bill;

import com.github.cooker.bill.domain.Bill;
import com.github.cooker.bill.domain.BillInstallmentSchedule;
import com.github.cooker.bill.domain.BillInstallmentScheduleRepository;
import com.github.cooker.bill.domain.BillRepayLog;
import com.github.cooker.bill.domain.BillRepayLogRepository;
import com.github.cooker.bill.domain.BillRepository;
import com.github.cooker.bill.domain.BillStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

/**
 * 账单还款应用服务。
 * 分期账单：应还总额 = 各期（本金+利息+服务费+逾期利息）之和；正常账单：应还 = 本金 + 逾期利息。
 */
@Service
public class BillRepayService {

    private final BillRepository billRepository;
    private final OverdueInterestService overdueInterestService;
    private final BillInstallmentScheduleService scheduleService;
    private final BillInstallmentScheduleRepository scheduleRepository;
    private final BillRepayLogRepository repayLogRepository;

    public BillRepayService(BillRepository billRepository, OverdueInterestService overdueInterestService,
            BillInstallmentScheduleService scheduleService, BillInstallmentScheduleRepository scheduleRepository,
            BillRepayLogRepository repayLogRepository) {
        this.billRepository = billRepository;
        this.overdueInterestService = overdueInterestService;
        this.scheduleService = scheduleService;
        this.scheduleRepository = scheduleRepository;
        this.repayLogRepository = repayLogRepository;
    }

    @Transactional
    public Bill repay(Long billId, BigDecimal amount) {
        Bill bill = billRepository.findById(billId).orElseThrow();
        if (bill.status() != BillStatus.UNPAID && bill.status() != BillStatus.OVERDUE) {
            throw new IllegalStateException("仅未结清或逾期账单可还款，当前状态: " + bill.status());
        }
        BigDecimal paid = bill.paidAmount() != null ? bill.paidAmount() : BigDecimal.ZERO;
        BigDecimal total = bill.totalAmount() != null ? bill.totalAmount() : BigDecimal.ZERO;
        BigDecimal effectiveTotal;
        if ("INSTALLMENT".equalsIgnoreCase(bill.billType())) {
            List<BillInstallmentSchedule> rows = scheduleRepository.findByBillId(billId);
            if (!rows.isEmpty()) {
                effectiveTotal = rows.stream()
                    .map(r -> (r.plannedAmount() != null ? r.plannedAmount() : BigDecimal.ZERO)
                        .add(r.interest() != null ? r.interest() : BigDecimal.ZERO)
                        .add(r.serviceFee() != null ? r.serviceFee() : BigDecimal.ZERO)
                        .add(r.overdueInterest() != null ? r.overdueInterest() : BigDecimal.ZERO))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            } else {
                BigDecimal interest = overdueInterestService.computeOverdueInterest(bill, LocalDate.now());
                effectiveTotal = total.add(interest != null ? interest : BigDecimal.ZERO);
            }
        } else {
            BigDecimal interest = overdueInterestService.computeOverdueInterest(bill, LocalDate.now());
            effectiveTotal = total.add(interest != null ? interest : BigDecimal.ZERO);
        }
        BigDecimal newPaid = paid.add(amount);
        Instant now = Instant.now();
        if (newPaid.compareTo(effectiveTotal) >= 0) {
            Bill updated = new Bill(
                bill.id(), bill.userId(), bill.productId(), bill.billMonth(), bill.billType(), bill.installmentCount(), bill.installmentDaysPerPeriod(),
                total, newPaid, BillStatus.PAID, bill.dueDate(), now
            );
            Bill saved = billRepository.save(updated);
            if ("INSTALLMENT".equalsIgnoreCase(bill.billType())) {
                scheduleService.allocatePayment(billId, amount, now);
            }
            repayLogRepository.save(BillRepayLog.of(billId, bill.userId(), amount, now));
            return saved;
        }
        Bill updated = new Bill(
            bill.id(), bill.userId(), bill.productId(), bill.billMonth(), bill.billType(), bill.installmentCount(), bill.installmentDaysPerPeriod(),
            total, newPaid, bill.status(), bill.dueDate(), null
        );
        Bill saved = billRepository.save(updated);
        if ("INSTALLMENT".equalsIgnoreCase(bill.billType())) {
            scheduleService.allocatePayment(billId, amount, now);
        }
        repayLogRepository.save(BillRepayLog.of(billId, bill.userId(), amount, now));
        return saved;
    }
}
