package com.github.cooker.bill.application.dto;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * 订单/交易明细 DTO。
 * 含订单号、商户订单号、订单类型、原订单id（退款单时）、交易卡号、交易时间、交易商户、金额等。
 * refunded 表示该笔正常订单是否已有退款单（已退款则不展示退款按钮）。
 */
public record OrderDTO(
    Long id, String orderNo, String merchantOrderNo, String orderType, Long originalOrderId,
    String userId, String cardNo, Instant tradeTime, String tradeCompany, BigDecimal amount,
    Instant accountTime, String tradeChannel, String tradeType,
    Boolean refunded
) {}
