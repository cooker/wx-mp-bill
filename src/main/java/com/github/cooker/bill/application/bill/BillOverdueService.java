package com.github.cooker.bill.application.bill;

import com.github.cooker.bill.domain.Bill;
import com.github.cooker.bill.domain.BillRepository;
import com.github.cooker.bill.domain.BillStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * 账单逾期应用服务（供定时任务调用）。
 */
@Service
public class BillOverdueService {

    private final BillRepository billRepository;

    public BillOverdueService(BillRepository billRepository) {
        this.billRepository = billRepository;
    }

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
