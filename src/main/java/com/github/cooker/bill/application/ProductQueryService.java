package com.github.cooker.bill.application;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.github.cooker.bill.application.dto.ProductDTO;
import com.github.cooker.bill.domain.Product;
import com.github.cooker.bill.domain.ProductRepository;

/**
 * 产品查询应用服务。
 * 提供产品列表与详情，供前端或订单创建时选用。
 */
@Service
public class ProductQueryService {

    private final ProductRepository productRepository;

    public ProductQueryService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    /** 按 id 查询产品。 */
    public Optional<ProductDTO> getById(Long id) {
        return productRepository.findById(id).map(ProductQueryService::toDTO);
    }

    /** 查询全部产品。 */
    public List<ProductDTO> listAll() {
        return productRepository.findAll().stream().map(ProductQueryService::toDTO).collect(Collectors.toList());
    }

    private static ProductDTO toDTO(Product p) {
        return new ProductDTO(p.id(), p.name(), p.overdueDailyRate(), p.billType() != null ? p.billType() : "NORMAL",
            p.installmentDailyRate(), p.installmentOverdueDailyRate(), p.installmentPerPeriodFeeRate(), p.installmentDaysPerPeriod(), p.installmentPeriodCount());
    }
}
