package com.github.cooker.bill.application;

import com.github.cooker.bill.application.dto.BillDTO;
import com.github.cooker.bill.application.dto.InstallmentScheduleItemDTO;
import com.github.cooker.bill.application.dto.PageResult;
import com.github.cooker.bill.domain.Bill;
import com.github.cooker.bill.domain.BillInstallmentSchedule;
import com.github.cooker.bill.domain.BillInstallmentScheduleRepository;
import com.github.cooker.bill.domain.BillRepository;
import com.github.cooker.bill.domain.BillStatus;
import com.github.cooker.bill.domain.Product;
import com.github.cooker.bill.domain.ProductRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 账单查询应用服务。
 * 提供本期应还、历史分页；账单金额会随退款、部分还款动态调整。逾期超过 3 天时 DTO 含逾期利息。
 */
@Slf4j
@Service
public class BillQueryService {

    private final BillRepository billRepository;
    private final OverdueInterestService overdueInterestService;
    private final BillInstallmentScheduleRepository scheduleRepository;
    private final ProductRepository productRepository;

    public BillQueryService(BillRepository billRepository, OverdueInterestService overdueInterestService,
            BillInstallmentScheduleRepository scheduleRepository, ProductRepository productRepository) {
        this.billRepository = billRepository;
        this.overdueInterestService = overdueInterestService;
        this.scheduleRepository = scheduleRepository;
        this.productRepository = productRepository;
    }

    public Optional<BillDTO> getById(Long id) {
        return billRepository.findById(id).map(this::toDTO);
    }

    /** 本期应还：按用户与还款日范围查询未结清/逾期账单。 */
    public List<BillDTO> listCurrentByUserId(String userId, LocalDate from, LocalDate to) {
        return billRepository.findByUserIdAndDueDateBetween(userId, from, to)
            .stream().map(this::toDTO).collect(Collectors.toList());
    }

    /** 历史账单分页，可按状态筛选。 */
    public PageResult<BillDTO> listHistoryByUserId(String userId, BillStatus status, int offset, int limit) {
        int size = limit <= 0 ? 20 : Math.min(limit, 100);
        List<Bill> list = billRepository.findByUserId(userId, status, offset, size);
        List<BillDTO> items = list.stream().map(this::toDTO).collect(Collectors.toList());
        return new PageResult<>(items, offset, size, list.size() == size);
    }

    /** 将领域对象转为 DTO，并计算分期每期金额、逾期利息及还款计划表。 */
    public BillDTO toDTO(Bill b) {
        BigDecimal installmentPerAmount = BigDecimal.ZERO;
        List<InstallmentScheduleItemDTO> schedule = Collections.emptyList();
        if ("INSTALLMENT".equalsIgnoreCase(b.billType())
            && b.installmentCount() != null && b.installmentCount() > 0
            && b.totalAmount() != null && b.dueDate() != null) {
            int n = b.installmentCount();
            installmentPerAmount = b.totalAmount()
                .divide(BigDecimal.valueOf(n), 2, RoundingMode.HALF_UP);
            Optional<Product> productOpt = b.productId() != null ? productRepository.findById(b.productId()) : Optional.empty();
            List<BillInstallmentSchedule> fromDb = scheduleRepository.findByBillId(b.id());
            if (!fromDb.isEmpty()) {
                schedule = fromDb.stream()
                    .map(row -> toScheduleItem(row, productOpt, b.installmentDaysPerPeriod(), LocalDate.now()))
                    .collect(Collectors.toList());
            } else {
                schedule = buildInstallmentSchedule(b.dueDate(), b.installmentDaysPerPeriod(), b.totalAmount(), b.paidAmount(), n, installmentPerAmount, productOpt);
            }
        }
        BigDecimal totalDueAmount = b.totalAmount() != null ? b.totalAmount() : BigDecimal.ZERO;
        BigDecimal totalInterest = BigDecimal.ZERO;
        BigDecimal totalServiceFee = BigDecimal.ZERO;
        BigDecimal billOverdueInterest = BigDecimal.ZERO;
        if (!schedule.isEmpty()) {
            totalDueAmount = schedule.stream()
                .map(item -> (item.amount() != null ? item.amount() : BigDecimal.ZERO)
                    .add(item.interest() != null ? item.interest() : BigDecimal.ZERO)
                    .add(item.serviceFee() != null ? item.serviceFee() : BigDecimal.ZERO)
                    .add(item.overdueInterest() != null ? item.overdueInterest() : BigDecimal.ZERO))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            totalInterest = schedule.stream()
                .map(item -> item.interest() != null ? item.interest() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            totalServiceFee = schedule.stream()
                .map(item -> item.serviceFee() != null ? item.serviceFee() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            billOverdueInterest = schedule.stream()
                .map(item -> item.overdueInterest() != null ? item.overdueInterest() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        } else {
            billOverdueInterest = overdueInterestService.computeOverdueInterest(b, LocalDate.now());
        }
        return new BillDTO(
            b.id(), b.userId(), b.productId(), b.billMonth(),
            b.billType() != null ? b.billType() : "NORMAL",
            b.installmentCount(), b.installmentDaysPerPeriod(),
            b.totalAmount(), totalDueAmount, b.paidAmount(),
            b.status(), b.dueDate(), b.paidAt(),
            installmentPerAmount,
            billOverdueInterest != null ? billOverdueInterest : BigDecimal.ZERO,
            totalInterest, totalServiceFee,
            schedule
        );
    }

    /**
     * 将计划表行转为 DTO：利息、服务费优先用表内已存值，若为 0 且有关联产品则按产品费率重算以保证展示；逾期利息按当前日重算并回写表。
     */
    private InstallmentScheduleItemDTO toScheduleItem(BillInstallmentSchedule row, Optional<Product> productOpt,
            Integer daysPerPeriod, LocalDate asOf) {
        BigDecimal interest = row.interest() != null && row.interest().compareTo(BigDecimal.ZERO) > 0 ? row.interest() : BigDecimal.ZERO;
        BigDecimal serviceFee = row.serviceFee() != null && row.serviceFee().compareTo(BigDecimal.ZERO) > 0 ? row.serviceFee() : BigDecimal.ZERO;
        BigDecimal overdueInterest = BigDecimal.ZERO;
        if (productOpt.isPresent()) {
            Product p = productOpt.get();
            int periodDays = (daysPerPeriod != null && daysPerPeriod > 0) ? daysPerPeriod : 30;
            if (interest.compareTo(BigDecimal.ZERO) == 0 && p.installmentDailyRate() != null && p.installmentDailyRate().compareTo(BigDecimal.ZERO) > 0) {
                interest = row.plannedAmount().multiply(p.installmentDailyRate()).multiply(BigDecimal.valueOf(periodDays))
                    .setScale(2, RoundingMode.HALF_UP);
            }
            if (serviceFee.compareTo(BigDecimal.ZERO) == 0 && p.installmentPerPeriodFeeRate() != null && p.installmentPerPeriodFeeRate().compareTo(BigDecimal.ZERO) > 0) {
                serviceFee = row.plannedAmount().multiply(p.installmentPerPeriodFeeRate()).setScale(2, RoundingMode.HALF_UP);
            }
            BigDecimal overdueRate = (p.installmentOverdueDailyRate() != null && p.installmentOverdueDailyRate().compareTo(BigDecimal.ZERO) > 0)
                ? p.installmentOverdueDailyRate() : overdueInterestService.getDefaultDailyRate();
            overdueInterest = overdueInterestService.computePeriodOverdueInterest(row.dueDate(), row.plannedAmount(), row.paidAmount(), asOf, overdueRate);
            // 仅当 paidAmount==0 时回写，避免已还本金的期被重算为 0 后覆盖历史逾期利息。若需“未查账单就还款”也能带出逾期利息，需在还款前先回写（或单独任务）。
            if (BigDecimal.ZERO.equals(row.paidAmount())) {
                if (row.id() != null) {
                    log.info("update overdue interest for bill installment schedule: {}", row);
                    scheduleRepository.updateOverdueInterest(row.id(), overdueInterest);
                }
            } else {
                overdueInterest = row.overdueInterest();
            }
        } else {
            BigDecimal defaultRate = overdueInterestService.getDefaultDailyRate();
            if (defaultRate != null && defaultRate.compareTo(BigDecimal.ZERO) > 0) {
                overdueInterest = overdueInterestService.computePeriodOverdueInterest(row.dueDate(), row.plannedAmount(), row.paidAmount(), asOf, defaultRate);
                if (row.id() != null) {
                    scheduleRepository.updateOverdueInterest(row.id(), overdueInterest);
                }
            }
        }
        // 本期应还总额 = 本金+利息+服务费+逾期利息；已还 >= 应还总额时才标识为已还
        BigDecimal periodTotalDue = row.plannedAmount().add(interest).add(serviceFee).add(overdueInterest);
        String status = row.paidAmount().compareTo(periodTotalDue) >= 0 ? "PAID" : "PENDING";
        return InstallmentScheduleItemDTO.of(row.periodNo(), row.dueDate(), row.plannedAmount(), status,
            row.paidAmount(), row.paidAt(), interest, serviceFee, overdueInterest);
    }

    /**
     * 生成分期还款计划表：第 k 期应还日按每期天数（daysPerPeriod）或自然月；前 N-1 期均摊，最后一期用剩余金额；已还按期累计判断。
     * 若有产品配置则计算每期利息、服务费、逾期利息。
     */
    private List<InstallmentScheduleItemDTO> buildInstallmentSchedule(
        LocalDate dueDate, Integer daysPerPeriod, BigDecimal totalAmount, BigDecimal paidAmount,
        int periodCount, BigDecimal perPeriodAmount, Optional<Product> productOpt
    ) {
        List<InstallmentScheduleItemDTO> list = new ArrayList<>(periodCount);
        BigDecimal cumulativeDue = BigDecimal.ZERO;
        BigDecimal paid = paidAmount != null ? paidAmount : BigDecimal.ZERO;
        int periodDays = (daysPerPeriod != null && daysPerPeriod > 0) ? daysPerPeriod : 30;
        LocalDate asOf = LocalDate.now();
        for (int k = 1; k <= periodCount; k++) {
            LocalDate periodDueDate = (daysPerPeriod != null && daysPerPeriod > 0)
                ? dueDate.plusDays((long) (k - 1) * daysPerPeriod)
                : dueDate.plusMonths(k - 1);
            BigDecimal amount = (k < periodCount)
                ? perPeriodAmount
                : totalAmount.subtract(perPeriodAmount.multiply(BigDecimal.valueOf(periodCount - 1)))
                    .setScale(2, RoundingMode.HALF_UP);
            cumulativeDue = cumulativeDue.add(amount);
            String status = paid.compareTo(cumulativeDue) >= 0 ? "PAID" : "PENDING";
            BigDecimal periodPaid = "PAID".equals(status) ? amount : BigDecimal.ZERO;
            BigDecimal interest = BigDecimal.ZERO;
            BigDecimal serviceFee = BigDecimal.ZERO;
            BigDecimal overdueInterest = BigDecimal.ZERO;
            if (productOpt.isPresent()) {
                Product p = productOpt.get();
                if (p.installmentDailyRate() != null && p.installmentDailyRate().compareTo(BigDecimal.ZERO) > 0) {
                    interest = amount.multiply(p.installmentDailyRate()).multiply(BigDecimal.valueOf(periodDays)).setScale(2, RoundingMode.HALF_UP);
                }
                if (p.installmentPerPeriodFeeRate() != null && p.installmentPerPeriodFeeRate().compareTo(BigDecimal.ZERO) > 0) {
                    serviceFee = amount.multiply(p.installmentPerPeriodFeeRate()).setScale(2, RoundingMode.HALF_UP);
                }
                if (p.installmentOverdueDailyRate() != null && p.installmentOverdueDailyRate().compareTo(BigDecimal.ZERO) > 0) {
                    overdueInterest = overdueInterestService.computePeriodOverdueInterest(periodDueDate, amount, periodPaid, asOf, p.installmentOverdueDailyRate());
                }
            }
            list.add(InstallmentScheduleItemDTO.of(k, periodDueDate, amount, status, "PAID".equals(status) ? amount : null, null, interest, serviceFee, overdueInterest));
        }
        return list;
    }

}
