package com.github.cooker.bill.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@TableName("bill_installment_schedule")
public class BillInstallmentSchedulePO {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    private Long billId;
    private Integer periodNo;
    private LocalDate dueDate;
    private BigDecimal plannedAmount;
    private BigDecimal paidAmount;
    private Instant paidAt;
    private BigDecimal interest;
    private BigDecimal serviceFee;
    private BigDecimal overdueInterest;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getBillId() { return billId; }
    public void setBillId(Long billId) { this.billId = billId; }
    public Integer getPeriodNo() { return periodNo; }
    public void setPeriodNo(Integer periodNo) { this.periodNo = periodNo; }
    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
    public BigDecimal getPlannedAmount() { return plannedAmount; }
    public void setPlannedAmount(BigDecimal plannedAmount) { this.plannedAmount = plannedAmount; }
    public BigDecimal getPaidAmount() { return paidAmount; }
    public void setPaidAmount(BigDecimal paidAmount) { this.paidAmount = paidAmount; }
    public Instant getPaidAt() { return paidAt; }
    public void setPaidAt(Instant paidAt) { this.paidAt = paidAt; }
    public BigDecimal getInterest() { return interest; }
    public void setInterest(BigDecimal interest) { this.interest = interest; }
    public BigDecimal getServiceFee() { return serviceFee; }
    public void setServiceFee(BigDecimal serviceFee) { this.serviceFee = serviceFee; }
    public BigDecimal getOverdueInterest() { return overdueInterest; }
    public void setOverdueInterest(BigDecimal overdueInterest) { this.overdueInterest = overdueInterest; }
}
