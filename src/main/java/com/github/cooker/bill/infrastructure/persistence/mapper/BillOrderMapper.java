package com.github.cooker.bill.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.cooker.bill.infrastructure.persistence.po.BillOrderPO;
import org.apache.ibatis.annotations.Mapper;

/** 订单/交易明细表 Mapper。 */
@Mapper
public interface BillOrderMapper extends BaseMapper<BillOrderPO> {
}
