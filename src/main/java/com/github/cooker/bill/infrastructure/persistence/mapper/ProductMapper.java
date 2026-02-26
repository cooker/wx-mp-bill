package com.github.cooker.bill.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.cooker.bill.infrastructure.persistence.po.ProductPO;
import org.apache.ibatis.annotations.Mapper;

/** 产品表 Mapper，继承 MyBatis-Plus BaseMapper。 */
@Mapper
public interface ProductMapper extends BaseMapper<ProductPO> {
}
