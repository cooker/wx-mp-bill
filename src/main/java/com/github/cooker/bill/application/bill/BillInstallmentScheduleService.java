package com.github.cooker.bill.application.bill;

import com.github.cooker.bill.domain.Bill;
import com.github.cooker.bill.domain.BillInstallmentSchedule;
import com.github.cooker.bill.domain.BillInstallmentScheduleRepository;
import com.github.cooker.bill.domain.Product;
import com.github.cooker.bill.domain.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 分期还款计划表服务：生成计划（含利息、服务费、逾期利息落库）、还款时分配至各期并更新已还明细。
 */
@Service
public class BillInstallmentScheduleService {

    private final BillInstallmentScheduleRepository scheduleRepository;
    private final ProductRepository productRepository;

    public BillInstallmentScheduleService(BillInstallmentScheduleRepository scheduleRepository,
            ProductRepository productRepository) {
        this.scheduleRepository = scheduleRepository;
        this.productRepository = productRepository;
    }

    /**
     * 为分期账单生成或覆盖还款计划表（按应还日、应还金额生成 N 行，利息、服务费按产品计算并落库，逾期利息初值为 0）。
     */
    @Transactional
    public void generateForBill(Bill bill) {
        if (bill == null || bill.id() == null || !"INSTALLMENT".equalsIgnoreCase(bill.billType())
            || bill.installmentCount() == null || bill.installmentCount() <= 0
            || bill.totalAmount() == null || bill.dueDate() == null) {
            return;
        }
        int n = bill.installmentCount();
        BigDecimal total = bill.totalAmount();
        LocalDate dueDate = bill.dueDate();
        Integer daysPerPeriod = bill.installmentDaysPerPeriod();
        int periodDays = (daysPerPeriod != null && daysPerPeriod > 0) ? daysPerPeriod : 30;
        BigDecimal perPeriod = total.divide(BigDecimal.valueOf(n), 2, RoundingMode.HALF_UP);
        Optional<Product> productOpt = bill.productId() != null ? productRepository.findById(bill.productId()) : Optional.empty();
        scheduleRepository.deleteByBillId(bill.id());
        List<BillInstallmentSchedule> rows = new ArrayList<>(n);
        for (int k = 1; k <= n; k++) {
            LocalDate periodDue = (daysPerPeriod != null && daysPerPeriod > 0)
                ? dueDate.plusDays((long) (k - 1) * daysPerPeriod)
                : dueDate.plusMonths(k - 1);
            BigDecimal planned = (k < n)
                ? perPeriod
                : total.subtract(perPeriod.multiply(BigDecimal.valueOf(n - 1))).setScale(2, RoundingMode.HALF_UP);
            BigDecimal interest = BigDecimal.ZERO;
            BigDecimal serviceFee = BigDecimal.ZERO;
            if (productOpt.isPresent()) {
                var p = productOpt.get();
                if (p.installmentDailyRate() != null && p.installmentDailyRate().compareTo(BigDecimal.ZERO) > 0) {
                    interest = planned.multiply(p.installmentDailyRate()).multiply(BigDecimal.valueOf(periodDays))
                        .setScale(2, RoundingMode.HALF_UP);
                }
                if (p.installmentPerPeriodFeeRate() != null && p.installmentPerPeriodFeeRate().compareTo(BigDecimal.ZERO) > 0) {
                    serviceFee = planned.multiply(p.installmentPerPeriodFeeRate()).setScale(2, RoundingMode.HALF_UP);
                }
            }
            rows.add(BillInstallmentSchedule.of(bill.id(), k, periodDue, planned, BigDecimal.ZERO, null,
                interest, serviceFee, BigDecimal.ZERO));
        }
        scheduleRepository.saveAll(rows);
    }

    /**
     * 将本次还款金额按期号顺序分配至计划表，并更新每期的 paid_amount、paid_at。
     * 本期应还总额 = 本金 + 利息 + 服务费 + 逾期利息；仅当已还金额 ≥ 本期应还总额时才标记该期为已还。
     */
    @Transactional
    public void allocatePayment(Long billId, BigDecimal amount, Instant paidAt) {
        if (billId == null || amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }
        List<BillInstallmentSchedule> rows = scheduleRepository.findByBillId(billId);
        if (rows.isEmpty()) {
            return;
        }
        BigDecimal remaining = amount;
        for (BillInstallmentSchedule row : rows) {
            if (remaining.compareTo(BigDecimal.ZERO) <= 0) {
                break;
            }
            BigDecimal interest = row.interest() != null ? row.interest() : BigDecimal.ZERO;
            BigDecimal serviceFee = row.serviceFee() != null ? row.serviceFee() : BigDecimal.ZERO;
            BigDecimal overdueInterest = row.overdueInterest() != null ? row.overdueInterest() : BigDecimal.ZERO;
            BigDecimal periodTotalDue = row.plannedAmount()
                .add(interest).add(serviceFee).add(overdueInterest);
            BigDecimal need = periodTotalDue.subtract(row.paidAmount());
            if (need.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }
            BigDecimal add = remaining.min(need).setScale(2, RoundingMode.HALF_UP);
            BigDecimal newPaid = row.paidAmount().add(add);
            remaining = remaining.subtract(add);
            Instant setPaidAt = newPaid.compareTo(periodTotalDue) >= 0 ? paidAt : null;
            scheduleRepository.updatePaidAmount(row.id(), newPaid, setPaidAt);
        }
    }

    /**
     * 修改分期账单的还款日后，按新的起始还款日重新计算各期应还日（保留每期金额与已还明细）。
     */
    @Transactional
    public void updateDueDatesForBill(Bill bill) {
        if (bill == null || bill.id() == null || !"INSTALLMENT".equalsIgnoreCase(bill.billType())
            || bill.installmentCount() == null || bill.installmentCount() <= 0
            || bill.dueDate() == null) {
            return;
        }
        List<BillInstallmentSchedule> rows = scheduleRepository.findByBillId(bill.id());
        if (rows.isEmpty()) {
            generateForBill(bill);
            return;
        }
        Integer daysPerPeriod = bill.installmentDaysPerPeriod();
        LocalDate baseDue = bill.dueDate();
        for (BillInstallmentSchedule row : rows) {
            int k = row.periodNo();
            LocalDate newDue = (daysPerPeriod != null && daysPerPeriod > 0)
                ? baseDue.plusDays((long) (k - 1) * daysPerPeriod)
                : baseDue.plusMonths(k - 1);
            scheduleRepository.updateDueDate(row.id(), newDue);
        }
    }
}
