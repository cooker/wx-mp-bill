package com.github.cooker.bill.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.cooker.bill.infrastructure.persistence.po.BillPO;
import org.apache.ibatis.annotations.Mapper;

/** 账单表 Mapper。 */
@Mapper
public interface BillMapper extends BaseMapper<BillPO> {
}
