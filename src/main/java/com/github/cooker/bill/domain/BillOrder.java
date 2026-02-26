package com.github.cooker.bill.domain;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * 订单/交易明细聚合根。
 * 对应表 bill_order。与账单为 N:1（多笔订单归属一张月账单），通过 billId 关联；退款单通过 orderType=REFUND 与 originalOrderId 关联原单。
 *
 * @param id               主键
 * @param billId           所属账单 id（月账单）
 * @param orderNo          订单号（系统唯一）
 * @param merchantOrderNo  商户订单号
 * @param orderType        NORMAL-正常订单，REFUND-退款单
 * @param originalOrderId  原订单id，退款单时关联被退款的订单
 * @param userId           用户标识
 * @param cardNo           交易卡号
 * @param tradeTime        交易时间
 * @param tradeCompany     交易商户
 * @param amount           金额（退款单为退款金额，正数）
 * @param accountTime      入账时间
 * @param tradeChannel     交易渠道
 * @param tradeType        交易类型
 */
public record BillOrder(
    Long id,
    Long billId,
    String orderNo,
    String merchantOrderNo,
    String orderType,
    Long originalOrderId,
    String userId,
    String cardNo,
    Instant tradeTime,
    String tradeCompany,
    BigDecimal amount,
    Instant accountTime,
    String tradeChannel,
    String tradeType
) {}
