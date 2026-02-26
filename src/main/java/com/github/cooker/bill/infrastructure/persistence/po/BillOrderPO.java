package com.github.cooker.bill.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * 订单/交易明细表持久化对象。
 * 表名 bill_order，包含需求字段：交易卡号、交易时间、交易公司、金额、入账时间、交易渠道、交易类型。
 */
@TableName("bill_order")
public class BillOrderPO {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    private Long billId;
    private String orderNo;
    private String merchantOrderNo;
    private String orderType;
    private Long originalOrderId;
    private String userId;
    private String cardNo;
    private Instant tradeTime;
    private String tradeCompany;
    private BigDecimal amount;
    private Instant accountTime;
    private String tradeChannel;
    private String tradeType;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getBillId() { return billId; }
    public void setBillId(Long billId) { this.billId = billId; }
    public String getOrderNo() { return orderNo; }
    public void setOrderNo(String orderNo) { this.orderNo = orderNo; }
    public String getMerchantOrderNo() { return merchantOrderNo; }
    public void setMerchantOrderNo(String merchantOrderNo) { this.merchantOrderNo = merchantOrderNo; }
    public String getOrderType() { return orderType; }
    public void setOrderType(String orderType) { this.orderType = orderType; }
    public Long getOriginalOrderId() { return originalOrderId; }
    public void setOriginalOrderId(Long originalOrderId) { this.originalOrderId = originalOrderId; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getCardNo() { return cardNo; }
    public void setCardNo(String cardNo) { this.cardNo = cardNo; }
    public Instant getTradeTime() { return tradeTime; }
    public void setTradeTime(Instant tradeTime) { this.tradeTime = tradeTime; }
    public String getTradeCompany() { return tradeCompany; }
    public void setTradeCompany(String tradeCompany) { this.tradeCompany = tradeCompany; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public Instant getAccountTime() { return accountTime; }
    public void setAccountTime(Instant accountTime) { this.accountTime = accountTime; }
    public String getTradeChannel() { return tradeChannel; }
    public void setTradeChannel(String tradeChannel) { this.tradeChannel = tradeChannel; }
    public String getTradeType() { return tradeType; }
    public void setTradeType(String tradeType) { this.tradeType = tradeType; }
}
