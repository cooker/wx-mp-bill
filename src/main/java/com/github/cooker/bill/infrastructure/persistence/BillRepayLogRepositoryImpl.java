package com.github.cooker.bill.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.cooker.bill.domain.BillRepayLog;
import com.github.cooker.bill.domain.BillRepayLogRepository;
import com.github.cooker.bill.infrastructure.persistence.mapper.BillRepayLogMapper;
import com.github.cooker.bill.infrastructure.persistence.po.BillRepayLogPO;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

@Repository
public class BillRepayLogRepositoryImpl implements BillRepayLogRepository {

    private final BillRepayLogMapper mapper;

    public BillRepayLogRepositoryImpl(BillRepayLogMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public BillRepayLog save(BillRepayLog log) {
        BillRepayLogPO po = toPO(log);
        mapper.insert(po);
        return toDomain(po);
    }

    @Override
    public List<BillRepayLog> findByBillId(Long billId) {
        LambdaQueryWrapper<BillRepayLogPO> w = new LambdaQueryWrapper<BillRepayLogPO>()
            .eq(BillRepayLogPO::getBillId, billId)
            .orderByDesc(BillRepayLogPO::getRepaidAt);
        return mapper.selectList(w).stream().map(this::toDomain).collect(Collectors.toList());
    }

    private BillRepayLog toDomain(BillRepayLogPO po) {
        return new BillRepayLog(po.getId(), po.getBillId(), po.getUserId(), po.getAmount(), po.getRepaidAt());
    }

    private BillRepayLogPO toPO(BillRepayLog log) {
        BillRepayLogPO po = new BillRepayLogPO();
        po.setId(log.id());
        po.setBillId(log.billId());
        po.setUserId(log.userId());
        po.setAmount(log.amount());
        po.setRepaidAt(log.repaidAt());
        return po;
    }
}
