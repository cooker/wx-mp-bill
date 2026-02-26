package com.github.cooker.bill.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;
import java.time.Instant;

@TableName("bill_repay_log")
public class BillRepayLogPO {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    private Long billId;
    private String userId;
    private BigDecimal amount;
    private Instant repaidAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getBillId() { return billId; }
    public void setBillId(Long billId) { this.billId = billId; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public Instant getRepaidAt() { return repaidAt; }
    public void setRepaidAt(Instant repaidAt) { this.repaidAt = repaidAt; }
}
