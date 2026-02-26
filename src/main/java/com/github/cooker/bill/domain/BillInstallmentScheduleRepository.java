package com.github.cooker.bill.domain;

import java.time.LocalDate;
import java.util.List;

/**
 * 分期还款计划表仓储。
 */
public interface BillInstallmentScheduleRepository {

    List<BillInstallmentSchedule> findByBillId(Long billId);

    /** 删除该账单下全部计划行（再配合 saveAll 实现整表替换）。 */
    void deleteByBillId(Long billId);

    void saveAll(List<BillInstallmentSchedule> rows);

    /** 更新单行的已还金额与结清时间（用于还款分配）。 */
    void updatePaidAmount(Long id, java.math.BigDecimal paidAmount, java.time.Instant paidAt);

    /** 更新单行的应还日（用于修改账单还款日后整体平移计划）。 */
    void updateDueDate(Long id, LocalDate dueDate);

    /** 更新单行的逾期利息（查询时按当前日重算后可回写）。 */
    void updateOverdueInterest(Long id, java.math.BigDecimal overdueInterest);
}
