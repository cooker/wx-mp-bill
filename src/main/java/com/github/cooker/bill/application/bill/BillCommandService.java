package com.github.cooker.bill.application.bill;

import com.github.cooker.bill.application.dto.BillDTO;
import com.github.cooker.bill.application.dto.InstallmentScheduleItemDTO;
import com.github.cooker.bill.domain.Bill;
import com.github.cooker.bill.domain.BillRepository;
import com.github.cooker.bill.domain.BillStatus;
import com.github.cooker.bill.domain.Product;
import com.github.cooker.bill.domain.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

/**
 * 账单配置应用服务：设为分期、修改还款日、分期预览。
 */
@Service
public class BillCommandService {

    private final BillRepository billRepository;
    private final BillQueryService billQueryService;
    private final BillInstallmentScheduleService scheduleService;
    private final ProductRepository productRepository;

    public BillCommandService(BillRepository billRepository, BillQueryService billQueryService,
            BillInstallmentScheduleService scheduleService, ProductRepository productRepository) {
        this.billRepository = billRepository;
        this.billQueryService = billQueryService;
        this.scheduleService = scheduleService;
        this.productRepository = productRepository;
    }

    @Transactional
    public BillDTO setInstallment(Long billId, String billType, Integer installmentCount, Integer installmentDaysPerPeriod, Long productId) {
        Bill bill = billRepository.findById(billId).orElseThrow(() -> new IllegalArgumentException("账单不存在: " + billId));
        if (bill.status() != BillStatus.UNPAID && bill.status() != BillStatus.OVERDUE) {
            throw new IllegalStateException("仅未结清或逾期账单可设为分期，当前状态: " + bill.status());
        }
        String type = (billType != null && !billType.isBlank()) ? billType : "INSTALLMENT";
        Integer periods = "INSTALLMENT".equals(type) ? installmentCount : null;
        Integer periodDays = "INSTALLMENT".equals(type) ? installmentDaysPerPeriod : null;
        Long resolvedProductId = productId != null ? productId : bill.productId();
        if (resolvedProductId != null && ("INSTALLMENT".equals(type)) && (periods == null || periodDays == null)) {
            Optional<Product> productOpt = productRepository.findById(resolvedProductId);
            if (productOpt.isPresent()) {
                Product p = productOpt.get();
                if (periods == null && p.installmentPeriodCount() != null && p.installmentPeriodCount() > 0) {
                    periods = p.installmentPeriodCount();
                }
                if (periodDays == null && p.installmentDaysPerPeriod() != null && p.installmentDaysPerPeriod() > 0) {
                    periodDays = p.installmentDaysPerPeriod();
                }
            }
        }
        if ("INSTALLMENT".equals(type) && (periods == null || periods < 1)) {
            throw new IllegalArgumentException("分期期数必填且大于 0，请选择已配置期数的分期产品或在请求中传入 installmentCount");
        }
        Bill updated = new Bill(
            bill.id(), bill.userId(), resolvedProductId, bill.billMonth(), type, periods, periodDays,
            bill.totalAmount(), bill.paidAmount(), bill.status(), bill.dueDate(), bill.paidAt()
        );
        billRepository.save(updated);
        scheduleService.generateForBill(updated);
        return billQueryService.toDTO(updated);
    }

    public java.util.List<InstallmentScheduleItemDTO> previewInstallment(Long billId, Integer installmentCount, Integer installmentDaysPerPeriod) {
        Bill bill = billRepository.findById(billId).orElseThrow(() -> new IllegalArgumentException("账单不存在: " + billId));
        if (bill.totalAmount() == null || bill.dueDate() == null) {
            throw new IllegalStateException("账单金额或还款日为空，无法预览分期计划");
        }
        int n = (installmentCount != null && installmentCount > 0)
            ? installmentCount
            : (bill.installmentCount() != null && bill.installmentCount() > 0 ? bill.installmentCount() : 0);
        if (n <= 0) {
            throw new IllegalArgumentException("分期期数 installmentCount 必须大于 0");
        }
        java.math.BigDecimal total = bill.totalAmount();
        java.time.LocalDate dueDate = bill.dueDate();
        Integer daysPerPeriod = installmentDaysPerPeriod != null ? installmentDaysPerPeriod : bill.installmentDaysPerPeriod();
        java.math.BigDecimal per = total.divide(java.math.BigDecimal.valueOf(n), 2, java.math.RoundingMode.HALF_UP);
        java.util.List<InstallmentScheduleItemDTO> list = new java.util.ArrayList<>(n);
        for (int k = 1; k <= n; k++) {
            java.time.LocalDate periodDue = (daysPerPeriod != null && daysPerPeriod > 0)
                ? dueDate.plusDays((long) (k - 1) * daysPerPeriod)
                : dueDate.plusMonths(k - 1);
            java.math.BigDecimal amount = (k < n)
                ? per
                : total.subtract(per.multiply(java.math.BigDecimal.valueOf(n - 1))).setScale(2, java.math.RoundingMode.HALF_UP);
            list.add(InstallmentScheduleItemDTO.of(k, periodDue, amount, "PENDING", null, null, java.math.BigDecimal.ZERO, java.math.BigDecimal.ZERO, java.math.BigDecimal.ZERO));
        }
        return list;
    }

    @Transactional
    public BillDTO updateDueDate(Long billId, LocalDate newDueDate) {
        Bill bill = billRepository.findById(billId).orElseThrow(() -> new IllegalArgumentException("账单不存在: " + billId));
        Bill updated = new Bill(
            bill.id(), bill.userId(), bill.productId(), bill.billMonth(), bill.billType(), bill.installmentCount(), bill.installmentDaysPerPeriod(),
            bill.totalAmount(), bill.paidAmount(), bill.status(), newDueDate, bill.paidAt()
        );
        billRepository.save(updated);
        if ("INSTALLMENT".equalsIgnoreCase(updated.billType())) {
            scheduleService.updateDueDatesForBill(updated);
        }
        return billQueryService.toDTO(updated);
    }
}
