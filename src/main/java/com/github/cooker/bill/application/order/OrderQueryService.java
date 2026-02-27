package com.github.cooker.bill.application.order;

import com.github.cooker.bill.application.dto.OrderDTO;
import com.github.cooker.bill.application.dto.PageResult;
import com.github.cooker.bill.domain.BillOrder;
import com.github.cooker.bill.domain.BillOrderRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 订单/交易明细查询应用服务。
 */
@Service
public class OrderQueryService {

    private final BillOrderRepository orderRepository;

    public OrderQueryService(BillOrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public Optional<OrderDTO> getById(Long id) {
        return orderRepository.findById(id).map(o -> {
            boolean refunded = "NORMAL".equals(o.orderType())
                && !orderRepository.findByOriginalOrderId(o.id()).isEmpty();
            return toDTO(o, refunded);
        });
    }

    public PageResult<OrderDTO> listByUserId(String userId, int offset, int limit) {
        int size = limit <= 0 ? 20 : Math.min(limit, 100);
        List<BillOrder> list = orderRepository.findByUserId(userId, offset, size);
        List<Long> normalIds = list.stream()
            .filter(o -> "NORMAL".equals(o.orderType()))
            .map(BillOrder::id)
            .collect(Collectors.toList());
        Set<Long> refundedIds = normalIds.isEmpty() ? Set.of()
            : orderRepository.findOriginalOrderIdsThatHaveRefund(normalIds).stream().collect(Collectors.toSet());
        List<OrderDTO> items = list.stream()
            .map(o -> toDTO(o, refundedIds.contains(o.id())))
            .collect(Collectors.toList());
        return new PageResult<>(items, offset, size, list.size() == size);
    }

    private OrderDTO toDTO(BillOrder o, boolean refunded) {
        return new OrderDTO(
            o.id(), o.orderNo(), o.merchantOrderNo(), o.orderType(), o.originalOrderId(),
            o.userId(), o.cardNo(), o.tradeTime(), o.tradeCompany(), o.amount(),
            o.accountTime(), o.tradeChannel(), o.tradeType(),
            Boolean.valueOf(refunded)
        );
    }
}
