package com.github.cooker.bill.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.cooker.bill.domain.BillOrder;
import com.github.cooker.bill.domain.BillOrderRepository;
import com.github.cooker.bill.infrastructure.persistence.mapper.BillOrderMapper;
import com.github.cooker.bill.infrastructure.persistence.po.BillOrderPO;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 订单/交易明细仓储 MyBatis-Plus 实现。
 * 持久化交易卡号、交易时间、交易公司、金额、入账时间、交易渠道、交易类型。
 */
@Repository
public class BillOrderRepositoryImpl implements BillOrderRepository {

    private final BillOrderMapper mapper;

    public BillOrderRepositoryImpl(BillOrderMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Optional<BillOrder> findById(Long id) {
        return Optional.ofNullable(mapper.selectById(id)).map(this::toDomain);
    }

    @Override
    public List<BillOrder> findByUserId(String userId, int offset, int limit) {
        LambdaQueryWrapper<BillOrderPO> w = new LambdaQueryWrapper<BillOrderPO>()
            .eq(BillOrderPO::getUserId, userId)
            .orderByDesc(BillOrderPO::getTradeTime)
            .last("LIMIT " + offset + "," + limit);
        return mapper.selectList(w).stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<BillOrder> findByAccountTimeBetween(java.time.Instant from, java.time.Instant to) {
        LambdaQueryWrapper<BillOrderPO> w = new LambdaQueryWrapper<BillOrderPO>()
            .ge(BillOrderPO::getAccountTime, from)
            .lt(BillOrderPO::getAccountTime, to);
        return mapper.selectList(w).stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<BillOrder> findByOriginalOrderId(Long originalOrderId) {
        LambdaQueryWrapper<BillOrderPO> w = new LambdaQueryWrapper<BillOrderPO>()
            .eq(BillOrderPO::getOriginalOrderId, originalOrderId)
            .eq(BillOrderPO::getOrderType, "REFUND")
            .orderByDesc(BillOrderPO::getTradeTime);
        return mapper.selectList(w).stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<Long> findOriginalOrderIdsThatHaveRefund(Collection<Long> originalOrderIds) {
        if (originalOrderIds == null || originalOrderIds.isEmpty()) return List.of();
        LambdaQueryWrapper<BillOrderPO> w = new LambdaQueryWrapper<BillOrderPO>()
            .eq(BillOrderPO::getOrderType, "REFUND")
            .in(BillOrderPO::getOriginalOrderId, originalOrderIds)
            .select(BillOrderPO::getOriginalOrderId);
        return mapper.selectList(w).stream()
            .map(BillOrderPO::getOriginalOrderId)
            .distinct()
            .collect(Collectors.toList());
    }

    @Override
    public BillOrder save(BillOrder order) {
        BillOrderPO po = toPO(order);
        if (order.id() == null) {
            mapper.insert(po);
            return toDomain(po);
        }
        mapper.updateById(po);
        return findById(po.getId()).orElseThrow();
    }

    private BillOrder toDomain(BillOrderPO po) {
        return new BillOrder(
            po.getId(), po.getBillId(),
            po.getOrderNo(), po.getMerchantOrderNo(),
            po.getOrderType() != null ? po.getOrderType() : "NORMAL",
            po.getOriginalOrderId(),
            po.getUserId(), po.getCardNo(), po.getTradeTime(), po.getTradeCompany(),
            po.getAmount(), po.getAccountTime(), po.getTradeChannel(), po.getTradeType()
        );
    }

    private BillOrderPO toPO(BillOrder o) {
        BillOrderPO po = new BillOrderPO();
        po.setId(o.id());
        po.setBillId(o.billId());
        po.setOrderNo(o.orderNo());
        po.setMerchantOrderNo(o.merchantOrderNo());
        po.setOrderType(o.orderType());
        po.setOriginalOrderId(o.originalOrderId());
        po.setUserId(o.userId());
        po.setCardNo(o.cardNo());
        po.setTradeTime(o.tradeTime());
        po.setTradeCompany(o.tradeCompany());
        po.setAmount(o.amount());
        po.setAccountTime(o.accountTime());
        po.setTradeChannel(o.tradeChannel());
        po.setTradeType(o.tradeType());
        return po;
    }
}
