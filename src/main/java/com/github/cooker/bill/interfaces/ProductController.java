package com.github.cooker.bill.interfaces;

import com.github.cooker.bill.application.product.ProductCommandService;
import com.github.cooker.bill.application.product.ProductQueryService;
import com.github.cooker.bill.application.dto.ProductDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/** 产品接口：列表、详情、新增、更新（调试/配置用）。 */
@RestController
@RequestMapping("/api/products")
public class ProductController {

    private static final Logger log = LoggerFactory.getLogger(ProductController.class);

    private final ProductQueryService productQueryService;
    private final ProductCommandService productCommandService;

    public ProductController(ProductQueryService productQueryService, ProductCommandService productCommandService) {
        this.productQueryService = productQueryService;
        this.productCommandService = productCommandService;
    }

    @GetMapping
    public ResponseEntity<List<ProductDTO>> list() {
        log.info("GET /api/products 产品列表");
        List<ProductDTO> list = productQueryService.listAll();
        log.debug("GET /api/products 返回 {} 个产品", list.size());
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDTO> getById(@PathVariable Long id) {
        log.info("GET /api/products/{} 查询产品", id);
        ResponseEntity<ProductDTO> result = productQueryService.getById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
        if (result.getStatusCode().is4xxClientError()) {
            log.info("GET /api/products/{} 产品不存在", id);
        }
        return result;
    }

    private static BigDecimal toBigDecimal(Object v) {
        return v != null ? new BigDecimal(v.toString()) : null;
    }

    private static Integer toInteger(Object v) {
        if (v == null) return null;
        if (v instanceof Number) return ((Number) v).intValue();
        return Integer.valueOf(v.toString());
    }

    /** 新增产品。body: name, overdueDailyRate(可选), billType(可选), installmentDailyRate(可选), installmentOverdueDailyRate(可选), installmentPerPeriodFeeRate(可选), installmentDaysPerPeriod(可选) */
    @PostMapping
    public ResponseEntity<ProductDTO> create(@RequestBody Map<String, Object> body) {
        String name = (String) body.get("name");
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("产品名称 name 必填");
        }
        log.info("POST /api/products 新增产品 name={}", name);
        BigDecimal rate = toBigDecimal(body.get("overdueDailyRate"));
        String billType = (String) body.get("billType");
        BigDecimal installmentDailyRate = toBigDecimal(body.get("installmentDailyRate"));
        BigDecimal installmentOverdueDailyRate = toBigDecimal(body.get("installmentOverdueDailyRate"));
        BigDecimal installmentPerPeriodFeeRate = toBigDecimal(body.get("installmentPerPeriodFeeRate"));
        Integer installmentDaysPerPeriod = toInteger(body.get("installmentDaysPerPeriod"));
        Integer installmentPeriodCount = toInteger(body.get("installmentPeriodCount"));
        ProductDTO dto = productCommandService.create(name, rate, billType, installmentDailyRate, installmentOverdueDailyRate, installmentPerPeriodFeeRate, installmentDaysPerPeriod, installmentPeriodCount);
        log.info("POST /api/products 新增产品成功 productId={}", dto.id());
        return ResponseEntity.ok(dto);
    }

    /** 更新产品。body: name(可选), overdueDailyRate(可选), billType(可选), installmentDailyRate(可选), installmentOverdueDailyRate(可选), installmentPerPeriodFeeRate(可选), installmentDaysPerPeriod(可选), installmentPeriodCount(可选) */
    @PostMapping("/{id}/update")
    public ResponseEntity<ProductDTO> update(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        log.info("POST /api/products/{}/update 更新产品 productId={}", id, id);
        String name = (String) body.get("name");
        BigDecimal rate = toBigDecimal(body.get("overdueDailyRate"));
        String billType = (String) body.get("billType");
        BigDecimal installmentDailyRate = toBigDecimal(body.get("installmentDailyRate"));
        BigDecimal installmentOverdueDailyRate = toBigDecimal(body.get("installmentOverdueDailyRate"));
        BigDecimal installmentPerPeriodFeeRate = toBigDecimal(body.get("installmentPerPeriodFeeRate"));
        Integer installmentDaysPerPeriod = toInteger(body.get("installmentDaysPerPeriod"));
        Integer installmentPeriodCount = toInteger(body.get("installmentPeriodCount"));
        ProductDTO dto = productCommandService.update(id, name, rate, billType, installmentDailyRate, installmentOverdueDailyRate, installmentPerPeriodFeeRate, installmentDaysPerPeriod, installmentPeriodCount);
        log.info("POST /api/products/{}/update 更新产品成功", id);
        return ResponseEntity.ok(dto);
    }
}
