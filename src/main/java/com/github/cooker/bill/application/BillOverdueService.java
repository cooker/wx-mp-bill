package com.github.cooker.bill.application;

import com.github.cooker.bill.domain.Bill;
import com.github.cooker.bill.domain.BillRepository;
import com.github.cooker.bill.domain.BillStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * 账单逾期应用服务（供定时任务调用）。
 * 将还款日早于指定日期且状态为 UNPAID 的账单标记为 OVERDUE。
 */
@Service
public class BillOverdueService {

    private final BillRepository billRepository;

    public BillOverdueService(BillRepository billRepository) {
        this.billRepository = billRepository;
    }

    /** 将指定日期之前到期且未结清的账单全部标记为逾期。 */
    @Transactional
    public int markAllOverdueAsOf(LocalDate asOf) {
        List<Bill> list = billRepository.findUnpaidWithDueDateBefore(asOf);
        for (Bill b : list) {
            Bill updated = new Bill(
                b.id(), b.userId(), b.productId(), b.billMonth(), b.billType(), b.installmentCount(), b.installmentDaysPerPeriod(),
                b.totalAmount(), b.paidAmount(), BillStatus.OVERDUE, b.dueDate(), b.paidAt()
            );
            billRepository.save(updated);
        }
        return list.size();
    }
}
