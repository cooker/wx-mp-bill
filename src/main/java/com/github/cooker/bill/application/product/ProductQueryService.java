package com.github.cooker.bill.application.product;

import com.github.cooker.bill.application.dto.ProductDTO;
import com.github.cooker.bill.domain.Product;
import com.github.cooker.bill.domain.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 产品查询应用服务。
 */
@Service
public class ProductQueryService {

    private final ProductRepository productRepository;

    public ProductQueryService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public Optional<ProductDTO> getById(Long id) {
        return productRepository.findById(id).map(ProductQueryService::toDTO);
    }

    public List<ProductDTO> listAll() {
        return productRepository.findAll().stream().map(ProductQueryService::toDTO).collect(Collectors.toList());
    }

    private static ProductDTO toDTO(Product p) {
        return new ProductDTO(p.id(), p.name(), p.overdueDailyRate(), p.billType() != null ? p.billType() : "NORMAL",
            p.installmentDailyRate(), p.installmentOverdueDailyRate(), p.installmentPerPeriodFeeRate(), p.installmentDaysPerPeriod(), p.installmentPeriodCount());
    }
}
