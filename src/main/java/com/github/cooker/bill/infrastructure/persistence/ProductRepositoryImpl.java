package com.github.cooker.bill.infrastructure.persistence;

import com.github.cooker.bill.domain.Product;
import com.github.cooker.bill.domain.ProductRepository;
import com.github.cooker.bill.infrastructure.persistence.mapper.ProductMapper;
import com.github.cooker.bill.infrastructure.persistence.po.ProductPO;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 产品仓储 MyBatis-Plus 实现。
 */
@Repository
public class ProductRepositoryImpl implements ProductRepository {

    private final ProductMapper mapper;

    public ProductRepositoryImpl(ProductMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Optional<Product> findById(Long id) {
        ProductPO po = mapper.selectById(id);
        return Optional.ofNullable(po).map(this::toDomain);
    }

    @Override
    public List<Product> findAll() {
        return mapper.selectList(null).stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public Product save(Product product) {
        ProductPO po = toPO(product);
        if (product.id() == null) {
            mapper.insert(po);
            return toDomain(po);
        }
        mapper.updateById(po);
        return findById(po.getId()).orElseThrow();
    }

    private Product toDomain(ProductPO po) {
        return new Product(po.getId(), po.getName(), po.getOverdueDailyRate(),
            po.getBillType() != null ? po.getBillType() : "NORMAL",
            po.getInstallmentDailyRate(), po.getInstallmentOverdueDailyRate(), po.getInstallmentPerPeriodFeeRate(),
            po.getInstallmentDaysPerPeriod(), po.getInstallmentPeriodCount());
    }

    private ProductPO toPO(Product p) {
        ProductPO po = new ProductPO();
        po.setId(p.id());
        po.setName(p.name());
        po.setOverdueDailyRate(p.overdueDailyRate());
        po.setBillType(p.billType() != null ? p.billType() : "NORMAL");
        po.setInstallmentDailyRate(p.installmentDailyRate());
        po.setInstallmentOverdueDailyRate(p.installmentOverdueDailyRate());
        po.setInstallmentPerPeriodFeeRate(p.installmentPerPeriodFeeRate());
        po.setInstallmentDaysPerPeriod(p.installmentDaysPerPeriod());
        po.setInstallmentPeriodCount(p.installmentPeriodCount());
        return po;
    }
}
