package com.github.cooker.bill.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.cooker.bill.infrastructure.persistence.po.BillRepayLogPO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface BillRepayLogMapper extends BaseMapper<BillRepayLogPO> {
}
