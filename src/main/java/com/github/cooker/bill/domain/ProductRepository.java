package com.github.cooker.bill.domain;

import java.util.List;
import java.util.Optional;

/**
 * 产品仓储接口。
 * 由基础设施层实现（MyBatis-Plus）。
 */
public interface ProductRepository {

    Optional<Product> findById(Long id);

    List<Product> findAll();

    Product save(Product product);
}
