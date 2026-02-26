package com.github.cooker.bill.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.github.cooker.bill.domain.BillInstallmentSchedule;
import com.github.cooker.bill.domain.BillInstallmentScheduleRepository;
import com.github.cooker.bill.infrastructure.persistence.mapper.BillInstallmentScheduleMapper;
import com.github.cooker.bill.infrastructure.persistence.po.BillInstallmentSchedulePO;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class BillInstallmentScheduleRepositoryImpl implements BillInstallmentScheduleRepository {

    private final BillInstallmentScheduleMapper mapper;

    public BillInstallmentScheduleRepositoryImpl(BillInstallmentScheduleMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public List<BillInstallmentSchedule> findByBillId(Long billId) {
        LambdaQueryWrapper<BillInstallmentSchedulePO> w = new LambdaQueryWrapper<BillInstallmentSchedulePO>()
            .eq(BillInstallmentSchedulePO::getBillId, billId)
            .orderByAsc(BillInstallmentSchedulePO::getPeriodNo);
        return mapper.selectList(w).stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public void deleteByBillId(Long billId) {
        mapper.delete(new LambdaQueryWrapper<BillInstallmentSchedulePO>().eq(BillInstallmentSchedulePO::getBillId, billId));
    }

    @Override
    public void saveAll(List<BillInstallmentSchedule> rows) {
        for (BillInstallmentSchedule row : rows) {
            BillInstallmentSchedulePO po = toPO(row);
            mapper.insert(po);
        }
    }

    @Override
    public void updatePaidAmount(Long id, BigDecimal paidAmount, Instant paidAt) {
        LambdaUpdateWrapper<BillInstallmentSchedulePO> w = new LambdaUpdateWrapper<BillInstallmentSchedulePO>()
            .eq(BillInstallmentSchedulePO::getId, id)
            .set(BillInstallmentSchedulePO::getPaidAmount, paidAmount)
            .set(BillInstallmentSchedulePO::getPaidAt, paidAt);
        mapper.update(null, w);
    }

    @Override
    public void updateDueDate(Long id, LocalDate dueDate) {
        LambdaUpdateWrapper<BillInstallmentSchedulePO> w = new LambdaUpdateWrapper<BillInstallmentSchedulePO>()
            .eq(BillInstallmentSchedulePO::getId, id)
            .set(BillInstallmentSchedulePO::getDueDate, dueDate);
        mapper.update(null, w);
    }

    @Override
    public void updateOverdueInterest(Long id, BigDecimal overdueInterest) {
        if (id == null) return;
        LambdaUpdateWrapper<BillInstallmentSchedulePO> w = new LambdaUpdateWrapper<BillInstallmentSchedulePO>()
            .eq(BillInstallmentSchedulePO::getId, id)
            .set(BillInstallmentSchedulePO::getOverdueInterest, overdueInterest != null ? overdueInterest : BigDecimal.ZERO);
        mapper.update(null, w);
    }

    private BillInstallmentSchedule toDomain(BillInstallmentSchedulePO po) {
        return new BillInstallmentSchedule(
            po.getId(), po.getBillId(), po.getPeriodNo() != null ? po.getPeriodNo() : 0,
            po.getDueDate(), po.getPlannedAmount(), po.getPaidAmount(), po.getPaidAt(),
            po.getInterest() != null ? po.getInterest() : BigDecimal.ZERO,
            po.getServiceFee() != null ? po.getServiceFee() : BigDecimal.ZERO,
            po.getOverdueInterest() != null ? po.getOverdueInterest() : BigDecimal.ZERO
        );
    }

    private BillInstallmentSchedulePO toPO(BillInstallmentSchedule row) {
        BillInstallmentSchedulePO po = new BillInstallmentSchedulePO();
        po.setId(row.id());
        po.setBillId(row.billId());
        po.setPeriodNo(row.periodNo());
        po.setDueDate(row.dueDate());
        po.setPlannedAmount(row.plannedAmount());
        po.setPaidAmount(row.paidAmount());
        po.setPaidAt(row.paidAt());
        po.setInterest(row.interest());
        po.setServiceFee(row.serviceFee());
        po.setOverdueInterest(row.overdueInterest());
        return po;
    }
}
