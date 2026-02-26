package com.github.cooker.bill.application;

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
 * 分期账单：还款金额（应还总额）= 还款计划合计（利息+服务费+应还金额+逾期利息），已还 >= 应还则结清。
 * 正常账单：应还 = 本金 + 逾期利息，已还 >= 应还则结清。
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

    /**
     * 还款（部分或全额）。
     * <p>
     * 规则概要：
     * <ul>
     *   <li>分期账单：应还总额 = 各期（本金+利息+服务费+逾期利息）之和；已还金额 ≥ 应还总额则账单结清。</li>
     *   <li>正常账单：应还 = 本金 + 逾期利息（超过还款日 3 天后起算）；已还 ≥ 应还则结清。</li>
     * </ul>
     *
     * @param billId 账单 ID
     * @param amount 本次还款金额（元）
     * @return 更新后的账单
     * @throws IllegalStateException 若账单状态不是未结清(UNPAID)或逾期(OVERDUE)
     */
    @Transactional
    public Bill repay(Long billId, BigDecimal amount) {
        // 1. 加载账单并校验状态：仅未结清、逾期可还款
        Bill bill = billRepository.findById(billId).orElseThrow();
        if (bill.status() != BillStatus.UNPAID && bill.status() != BillStatus.OVERDUE) {
            throw new IllegalStateException("仅未结清或逾期账单可还款，当前状态: " + bill.status());
        }

        BigDecimal paid = bill.paidAmount() != null ? bill.paidAmount() : BigDecimal.ZERO;
        BigDecimal total = bill.totalAmount() != null ? bill.totalAmount() : BigDecimal.ZERO;

        // 2. 计算账单应还总额 effectiveTotal
        BigDecimal effectiveTotal;
        if ("INSTALLMENT".equalsIgnoreCase(bill.billType())) {
            List<BillInstallmentSchedule> rows = scheduleRepository.findByBillId(billId);
            if (!rows.isEmpty()) {
                // 分期且有计划表：应还 = 各期（本金+利息+服务费+逾期利息）之和
                effectiveTotal = rows.stream()
                    .map(r -> (r.plannedAmount() != null ? r.plannedAmount() : BigDecimal.ZERO)
                        .add(r.interest() != null ? r.interest() : BigDecimal.ZERO)
                        .add(r.serviceFee() != null ? r.serviceFee() : BigDecimal.ZERO)
                        .add(r.overdueInterest() != null ? r.overdueInterest() : BigDecimal.ZERO))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            } else {
                // 分期但无计划表：按本金 + 逾期利息
                BigDecimal interest = overdueInterestService.computeOverdueInterest(bill, LocalDate.now());
                effectiveTotal = total.add(interest != null ? interest : BigDecimal.ZERO);
            }
        } else {
            // 正常账单：应还 = 本金 + 逾期利息
            BigDecimal interest = overdueInterestService.computeOverdueInterest(bill, LocalDate.now());
            effectiveTotal = total.add(interest != null ? interest : BigDecimal.ZERO);
        }

        BigDecimal newPaid = paid.add(amount);
        Instant now = Instant.now();

        // 3. 本次还款后已还 ≥ 应还：账单结清
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

        // 4. 部分还款：更新已还金额，不改变账单状态，不设结清时间
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
