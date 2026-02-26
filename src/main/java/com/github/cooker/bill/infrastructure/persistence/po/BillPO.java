package com.github.cooker.bill.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

/**
 * 账单表持久化对象。
 * 表名 bill。total_amount、paid_amount 在未结清时可由退款、部分还款动态调整。
 */
@TableName("bill")
public class BillPO {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    private String userId;
    private Long productId;
    private String billMonth;
    private String billType;
    private Integer installmentCount;
    private Integer installmentDaysPerPeriod;
    private BigDecimal totalAmount;
    private BigDecimal paidAmount;
    private String status;
    private LocalDate dueDate;
    private Instant paidAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public String getBillMonth() { return billMonth; }
    public void setBillMonth(String billMonth) { this.billMonth = billMonth; }
    public String getBillType() { return billType; }
    public void setBillType(String billType) { this.billType = billType; }
    public Integer getInstallmentCount() { return installmentCount; }
    public void setInstallmentCount(Integer installmentCount) { this.installmentCount = installmentCount; }
    public Integer getInstallmentDaysPerPeriod() { return installmentDaysPerPeriod; }
    public void setInstallmentDaysPerPeriod(Integer installmentDaysPerPeriod) { this.installmentDaysPerPeriod = installmentDaysPerPeriod; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public BigDecimal getPaidAmount() { return paidAmount; }
    public void setPaidAmount(BigDecimal paidAmount) { this.paidAmount = paidAmount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
    public Instant getPaidAt() { return paidAt; }
    public void setPaidAt(Instant paidAt) { this.paidAt = paidAt; }
}
