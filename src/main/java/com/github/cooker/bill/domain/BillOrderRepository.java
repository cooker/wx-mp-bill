package com.github.cooker.bill.domain;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * 订单/交易明细仓储接口。
 * 支持按用户、时间范围查询，以及保存（含交易卡号、交易时间、交易公司、金额、入账时间、交易渠道、交易类型）。
 */
public interface BillOrderRepository {

    Optional<BillOrder> findById(Long id);

    List<BillOrder> findByUserId(String userId, int offset, int limit);

    /** 入账时间在 [from, to) 内的订单（用于定时生成账单：入账日+1月=今日的订单）。 */
    List<BillOrder> findByAccountTimeBetween(Instant from, Instant to);

    /** 根据原订单id查询退款单列表（order_type=REFUND 且 original_order_id=id）。 */
    List<BillOrder> findByOriginalOrderId(Long originalOrderId);

    /** 返回已有退款单的原订单 id 集合（用于标记订单是否已退款）。 */
    List<Long> findOriginalOrderIdsThatHaveRefund(Collection<Long> originalOrderIds);

    BillOrder save(BillOrder order);
}
