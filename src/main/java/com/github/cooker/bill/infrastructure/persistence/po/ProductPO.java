package com.github.cooker.bill.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;

/**
 * 产品表持久化对象。
 * 表名 product。含分期日利率、分期逾期日利率、每期服务费本金利率。
 */
@TableName("product")
public class ProductPO {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    private String name;
    private BigDecimal overdueDailyRate;
    private String billType;
    private BigDecimal installmentDailyRate;
    private BigDecimal installmentOverdueDailyRate;
    private BigDecimal installmentPerPeriodFeeRate;
    private Integer installmentDaysPerPeriod;
    private Integer installmentPeriodCount;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public BigDecimal getOverdueDailyRate() { return overdueDailyRate; }
    public void setOverdueDailyRate(BigDecimal overdueDailyRate) { this.overdueDailyRate = overdueDailyRate; }
    public String getBillType() { return billType; }
    public void setBillType(String billType) { this.billType = billType; }
    public BigDecimal getInstallmentDailyRate() { return installmentDailyRate; }
    public void setInstallmentDailyRate(BigDecimal installmentDailyRate) { this.installmentDailyRate = installmentDailyRate; }
    public BigDecimal getInstallmentOverdueDailyRate() { return installmentOverdueDailyRate; }
    public void setInstallmentOverdueDailyRate(BigDecimal installmentOverdueDailyRate) { this.installmentOverdueDailyRate = installmentOverdueDailyRate; }
    public BigDecimal getInstallmentPerPeriodFeeRate() { return installmentPerPeriodFeeRate; }
    public void setInstallmentPerPeriodFeeRate(BigDecimal installmentPerPeriodFeeRate) { this.installmentPerPeriodFeeRate = installmentPerPeriodFeeRate; }
    public Integer getInstallmentDaysPerPeriod() { return installmentDaysPerPeriod; }
    public void setInstallmentDaysPerPeriod(Integer installmentDaysPerPeriod) { this.installmentDaysPerPeriod = installmentDaysPerPeriod; }
    public Integer getInstallmentPeriodCount() { return installmentPeriodCount; }
    public void setInstallmentPeriodCount(Integer installmentPeriodCount) { this.installmentPeriodCount = installmentPeriodCount; }
}
