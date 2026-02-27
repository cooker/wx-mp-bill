package com.github.cooker.bill.application.product;

import com.github.cooker.bill.application.dto.ProductDTO;
import com.github.cooker.bill.domain.Product;
import com.github.cooker.bill.domain.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * 产品配置应用服务。支持新增、更新产品。
 */
@Service
public class ProductCommandService {

    private final ProductRepository productRepository;

    public ProductCommandService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Transactional
    public ProductDTO create(String name, BigDecimal overdueDailyRate, String billType,
            BigDecimal installmentDailyRate, BigDecimal installmentOverdueDailyRate, BigDecimal installmentPerPeriodFeeRate,
            Integer installmentDaysPerPeriod, Integer installmentPeriodCount) {
        Product p = new Product(null, name, overdueDailyRate, billType != null ? billType : "NORMAL",
            installmentDailyRate, installmentOverdueDailyRate, installmentPerPeriodFeeRate, installmentDaysPerPeriod, installmentPeriodCount);
        Product saved = productRepository.save(p);
        return toDTO(saved);
    }

    @Transactional
    public ProductDTO update(Long id, String name, BigDecimal overdueDailyRate, String billType,
            BigDecimal installmentDailyRate, BigDecimal installmentOverdueDailyRate, BigDecimal installmentPerPeriodFeeRate,
            Integer installmentDaysPerPeriod, Integer installmentPeriodCount) {
        Product existing = productRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("产品不存在: " + id));
        Product p = new Product(id, name != null ? name : existing.name(),
            overdueDailyRate != null ? overdueDailyRate : existing.overdueDailyRate(),
            billType != null ? billType : existing.billType(),
            installmentDailyRate != null ? installmentDailyRate : existing.installmentDailyRate(),
            installmentOverdueDailyRate != null ? installmentOverdueDailyRate : existing.installmentOverdueDailyRate(),
            installmentPerPeriodFeeRate != null ? installmentPerPeriodFeeRate : existing.installmentPerPeriodFeeRate(),
            installmentDaysPerPeriod != null ? installmentDaysPerPeriod : existing.installmentDaysPerPeriod(),
            installmentPeriodCount != null ? installmentPeriodCount : existing.installmentPeriodCount());
        Product saved = productRepository.save(p);
        return toDTO(saved);
    }

    private static ProductDTO toDTO(Product p) {
        return new ProductDTO(p.id(), p.name(), p.overdueDailyRate(), p.billType() != null ? p.billType() : "NORMAL",
            p.installmentDailyRate(), p.installmentOverdueDailyRate(), p.installmentPerPeriodFeeRate(), p.installmentDaysPerPeriod(), p.installmentPeriodCount());
    }
}
