package com.github.cooker.bill.interfaces;

import com.github.cooker.bill.application.order.OrderCreateService;
import com.github.cooker.bill.application.order.OrderQueryService;
import com.github.cooker.bill.application.order.OrderRefundService;
import com.github.cooker.bill.application.dto.OrderDTO;
import com.github.cooker.bill.application.dto.PageResult;
import com.github.cooker.bill.domain.BillOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

/**
 * 订单/交易明细接口。
 * 提供列表、详情、创建（含交易卡号、交易时间、交易公司、金额、入账时间、交易渠道、交易类型）、退款。
 */
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private static final Logger log = LoggerFactory.getLogger(OrderController.class);

    private final OrderQueryService orderQueryService;
    private final OrderCreateService orderCreateService;
    private final OrderRefundService orderRefundService;

    public OrderController(OrderQueryService orderQueryService, OrderCreateService orderCreateService,
                           OrderRefundService orderRefundService) {
        this.orderQueryService = orderQueryService;
        this.orderCreateService = orderCreateService;
        this.orderRefundService = orderRefundService;
    }

    @GetMapping
    public ResponseEntity<PageResult<OrderDTO>> list(
            @RequestParam String userId,
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "20") int limit) {
        log.info("GET /api/orders userId={} offset={} limit={}", userId, offset, limit);
        PageResult<OrderDTO> page = orderQueryService.listByUserId(userId, offset, limit);
        log.debug("GET /api/orders 返回 {} 条", page.items().size());
        return ResponseEntity.ok(page);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderDTO> getById(@PathVariable Long id) {
        log.info("GET /api/orders/{} 查询订单", id);
        ResponseEntity<OrderDTO> result = orderQueryService.getById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
        if (result.getStatusCode().is4xxClientError()) {
            log.info("GET /api/orders/{} 订单不存在", id);
        }
        return result;
    }

    /** 创建订单（交易明细），并生成关联未结清账单。可选 billType、installmentCount，仅对新建账单生效。 */
    @PostMapping
    public ResponseEntity<OrderDTO> create(@RequestBody Map<String, Object> body) {
        String orderNo = (String) body.get("orderNo");
        if (orderNo == null || orderNo.isBlank()) {
            throw new IllegalArgumentException("订单号 orderNo 必填");
        }
        String userId = (String) body.get("userId");
        String cardNo = (String) body.get("cardNo");
        BigDecimal amount = new BigDecimal(body.get("amount").toString());
        log.info("POST /api/orders 创建订单 orderNo={} userId={} amount={}", orderNo, userId, amount);
        String merchantOrderNo = (String) body.get("merchantOrderNo");
        Instant tradeTime = body.get("tradeTime") != null ? Instant.parse(body.get("tradeTime").toString()) : Instant.now();
        String tradeCompany = (String) body.get("tradeCompany");
        Instant accountTime = body.get("accountTime") != null ? Instant.parse(body.get("accountTime").toString()) : tradeTime;
        String tradeChannel = (String) body.get("tradeChannel");
        String tradeType = (String) body.get("tradeType");
        String billType = (String) body.get("billType");
        Integer installmentCount = body.get("installmentCount") != null ? Integer.valueOf(body.get("installmentCount").toString()) : null;
        Integer installmentDaysPerPeriod = body.get("installmentDaysPerPeriod") != null ? Integer.valueOf(body.get("installmentDaysPerPeriod").toString()) : null;
        BillOrder order = orderCreateService.create(orderNo, merchantOrderNo, userId, cardNo, tradeTime, tradeCompany, amount, accountTime, tradeChannel, tradeType, billType, installmentCount, installmentDaysPerPeriod);
        log.info("POST /api/orders 创建订单成功 orderId={} billId={}", order.id(), order.billId());
        return ResponseEntity.ok(toDTO(order));
    }

    /** 退款：动态调整关联账单应还总额。返回 204 无内容，便于前端不解析 JSON。 */
    @PostMapping("/{id}/refund")
    public ResponseEntity<Void> refund(@PathVariable Long id) {
        log.info("POST /api/orders/{}/refund 退款 orderId={}", id, id);
        orderRefundService.refund(id);
        log.info("POST /api/orders/{}/refund 退款成功", id);
        return ResponseEntity.noContent().build();
    }

    private OrderDTO toDTO(BillOrder o) {
        return new OrderDTO(
            o.id(), o.orderNo(), o.merchantOrderNo(), o.orderType(), o.originalOrderId(),
            o.userId(), o.cardNo(), o.tradeTime(), o.tradeCompany(), o.amount(),
            o.accountTime(), o.tradeChannel(), o.tradeType(),
            false
        );
    }
}
