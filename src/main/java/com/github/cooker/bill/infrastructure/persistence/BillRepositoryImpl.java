package com.github.cooker.bill.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.cooker.bill.domain.Bill;
import com.github.cooker.bill.domain.BillRepository;
import com.github.cooker.bill.domain.BillStatus;
import com.github.cooker.bill.infrastructure.persistence.mapper.BillMapper;
import com.github.cooker.bill.infrastructure.persistence.po.BillPO;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 账单仓储 MyBatis-Plus 实现。
 * 支持按用户、还款日、状态查询；未结清账单的 total_amount/paid_amount 可更新以实现动态调整。
 */
@Repository
public class BillRepositoryImpl implements BillRepository {

    private final BillMapper mapper;

    public BillRepositoryImpl(BillMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Optional<Bill> findById(Long id) {
        return Optional.ofNullable(mapper.selectById(id)).map(this::toDomain);
    }

    @Override
    public List<Bill> findByUserIdAndDueDateBetween(String userId, LocalDate from, LocalDate to) {
        LambdaQueryWrapper<BillPO> w = new LambdaQueryWrapper<BillPO>()
            .eq(BillPO::getUserId, userId)
            .ge(BillPO::getDueDate, from)
            .le(BillPO::getDueDate, to)
            .in(BillPO::getStatus, BillStatus.UNPAID.name(), BillStatus.OVERDUE.name())
            .orderByAsc(BillPO::getDueDate);
        return mapper.selectList(w).stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<Bill> findByUserId(String userId, BillStatus status, int offset, int limit) {
        LambdaQueryWrapper<BillPO> w = new LambdaQueryWrapper<BillPO>()
            .eq(BillPO::getUserId, userId)
            .eq(status != null, BillPO::getStatus, status != null ? status.name() : null)
            .orderByDesc(BillPO::getDueDate)
            .last("LIMIT " + offset + "," + limit);
        return mapper.selectList(w).stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<Bill> findUnpaidWithDueDateBefore(LocalDate before) {
        LambdaQueryWrapper<BillPO> w = new LambdaQueryWrapper<BillPO>()
            .eq(BillPO::getStatus, BillStatus.UNPAID.name())
            .lt(BillPO::getDueDate, before);
        return mapper.selectList(w).stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public Optional<Bill> findByUserIdAndBillMonth(String userId, String billMonth) {
        LambdaQueryWrapper<BillPO> w = new LambdaQueryWrapper<BillPO>()
            .eq(BillPO::getUserId, userId)
            .eq(BillPO::getBillMonth, billMonth);
        return Optional.ofNullable(mapper.selectOne(w)).map(this::toDomain);
    }

    @Override
    public Optional<Bill> findUnpaidByUserIdAndBillMonth(String userId, String billMonth) {
        LambdaQueryWrapper<BillPO> w = new LambdaQueryWrapper<BillPO>()
            .eq(BillPO::getUserId, userId)
            .eq(BillPO::getBillMonth, billMonth)
            .in(BillPO::getStatus, BillStatus.UNPAID.name(), BillStatus.OVERDUE.name())
            .orderByDesc(BillPO::getId)
            .last("LIMIT 1");
        return mapper.selectList(w).stream().findFirst().map(this::toDomain);
    }

    @Override
    public Bill save(Bill bill) {
        BillPO po = toPO(bill);
        if (bill.id() == null) {
            mapper.insert(po);
            return toDomain(po);
        }
        mapper.updateById(po);
        return findById(po.getId()).orElseThrow();
    }

    private Bill toDomain(BillPO po) {
        return new Bill(
            po.getId(), po.getUserId(), po.getProductId(), po.getBillMonth(),
            po.getBillType() != null ? po.getBillType() : "NORMAL",
            po.getInstallmentCount(), po.getInstallmentDaysPerPeriod(),
            po.getTotalAmount() != null ? po.getTotalAmount() : BigDecimal.ZERO,
            po.getPaidAmount() != null ? po.getPaidAmount() : BigDecimal.ZERO,
            parseStatus(po.getStatus()),
            po.getDueDate(), po.getPaidAt()
        );
    }

    private BillPO toPO(Bill b) {
        BillPO po = new BillPO();
        po.setId(b.id());
        po.setUserId(b.userId());
        po.setProductId(b.productId());
        po.setBillMonth(b.billMonth());
        po.setBillType(b.billType() != null ? b.billType() : "NORMAL");
        po.setInstallmentCount(b.installmentCount());
        po.setInstallmentDaysPerPeriod(b.installmentDaysPerPeriod());
        po.setTotalAmount(b.totalAmount());
        po.setPaidAmount(b.paidAmount());
        po.setStatus(b.status() != null ? b.status().name() : BillStatus.UNPAID.name());
        po.setDueDate(b.dueDate());
        po.setPaidAt(b.paidAt());
        return po;
    }

    private static BillStatus parseStatus(String s) {
        if (s == null || s.isBlank()) return BillStatus.UNPAID;
        try {
            return BillStatus.valueOf(s);
        } catch (IllegalArgumentException e) {
            return BillStatus.UNPAID;
        }
    }
}
