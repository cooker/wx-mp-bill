package com.github.cooker.bill.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.cooker.bill.infrastructure.persistence.po.BillInstallmentSchedulePO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface BillInstallmentScheduleMapper extends BaseMapper<BillInstallmentSchedulePO> {
}
