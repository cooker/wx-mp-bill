package com.github.cooker.bill.interfaces;

import com.github.cooker.bill.application.bill.BillCommandService;
import com.github.cooker.bill.application.bill.BillQueryService;
import com.github.cooker.bill.application.bill.BillRepayService;
import com.github.cooker.bill.application.dto.BillDTO;
import com.github.cooker.bill.application.dto.InstallmentScheduleItemDTO;
import com.github.cooker.bill.application.dto.PageResult;
import com.github.cooker.bill.domain.Bill;
import com.github.cooker.bill.domain.BillStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 账单接口。
 * 本期应还、历史分页、还款（部分/全额）。未结清时退款、部分还款会动态调整账单金额。
 */
@RestController
@RequestMapping("/api/bills")
public class BillController {

    private static final Logger log = LoggerFactory.getLogger(BillController.class);

    private final BillQueryService billQueryService;
    private final BillRepayService billRepayService;
    private final BillCommandService billCommandService;

    public BillController(BillQueryService billQueryService, BillRepayService billRepayService, BillCommandService billCommandService) {
        this.billQueryService = billQueryService;
        this.billRepayService = billRepayService;
        this.billCommandService = billCommandService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<BillDTO> getById(@PathVariable Long id) {
        log.info("GET /api/bills/{} 查询账单", id);
        ResponseEntity<BillDTO> result = billQueryService.getById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
        if (result.getStatusCode().is4xxClientError()) {
            log.info("GET /api/bills/{} 账单不存在", id);
        }
        return result;
    }

    /** 本期应还：按用户与还款日范围查询。 */
    @GetMapping("/current")
    public ResponseEntity<List<BillDTO>> listCurrent(
            @RequestParam String userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        log.info("GET /api/bills/current userId={} from={} to={}", userId, from, to);
        List<BillDTO> list = billQueryService.listCurrentByUserId(userId, from, to);
        log.debug("GET /api/bills/current 返回 {} 条", list.size());
        return ResponseEntity.ok(list);
    }

    @GetMapping("/history")
    public ResponseEntity<PageResult<BillDTO>> listHistory(
            @RequestParam String userId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "20") int limit) {
        BillStatus s = parseStatus(status);
        log.info("GET /api/bills/history userId={} status={} offset={} limit={}", userId, status, offset, limit);
        PageResult<BillDTO> page = billQueryService.listHistoryByUserId(userId, s, offset, limit);
        log.debug("GET /api/bills/history 返回 {} 条", page.items().size());
        return ResponseEntity.ok(page);
    }

    /** 还款（部分或全额），动态更新已还金额；应还含逾期超过 3 天的利息，达应还则结清。 */
    @PostMapping("/{id}/repay")
    public ResponseEntity<BillDTO> repay(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        BigDecimal amount = new BigDecimal(body.getOrDefault("amount", "0").toString());
        log.info("POST /api/bills/{}/repay billId={} amount={}", id, id, amount);
        Bill bill = billRepayService.repay(id, amount);
        log.info("POST /api/bills/{}/repay 还款成功 billId={} paidAmount={}", id, id, bill.paidAmount());
        return ResponseEntity.ok(billQueryService.toDTO(bill));
    }

    /** 将未结清账单设为分期。body: billType(可选), installmentCount(期数), installmentDaysPerPeriod(每期天数，可选), productId(分期产品) */
    @PostMapping("/{id}/update")
    public ResponseEntity<BillDTO> updateBillType(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        String billType = (String) body.get("billType");
        Integer installmentCount = body.get("installmentCount") != null ? Integer.valueOf(body.get("installmentCount").toString()) : null;
        Integer installmentDaysPerPeriod = body.get("installmentDaysPerPeriod") != null ? Integer.valueOf(body.get("installmentDaysPerPeriod").toString()) : null;
        Long productId = body.get("productId") != null ? Long.valueOf(body.get("productId").toString()) : null;
        log.info("POST /api/bills/{}/update 设为分期 billId={} installmentCount={} productId={}", id, id, installmentCount, productId);
        BillDTO dto = billCommandService.setInstallment(id, billType, installmentCount, installmentDaysPerPeriod, productId);
        log.info("POST /api/bills/{}/update 设为分期成功", id);
        return ResponseEntity.ok(dto);
    }

    /** 修改账单还款日。body: dueDate(yyyy-MM-dd) */
    @PostMapping("/{id}/due-date")
    public ResponseEntity<BillDTO> updateDueDate(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Object v = body.get("dueDate");
        if (v == null) {
            throw new IllegalArgumentException("dueDate 必填，格式 yyyy-MM-dd");
        }
        LocalDate dueDate = LocalDate.parse(v.toString());
        log.info("POST /api/bills/{}/due-date billId={} dueDate={}", id, id, dueDate);
        BillDTO dto = billCommandService.updateDueDate(id, dueDate);
        log.info("POST /api/bills/{}/due-date 修改还款日成功", id);
        return ResponseEntity.ok(dto);
    }

    /** 分期预览：仅计算拆分计划，不落库。body: installmentCount, installmentDaysPerPeriod(可选) */
    @PostMapping("/{id}/installment-preview")
    public ResponseEntity<java.util.List<InstallmentScheduleItemDTO>> previewInstallment(
            @PathVariable Long id, @RequestBody Map<String, Object> body) {
        Integer installmentCount = body.get("installmentCount") != null
            ? Integer.valueOf(body.get("installmentCount").toString()) : null;
        Integer installmentDaysPerPeriod = body.get("installmentDaysPerPeriod") != null
            ? Integer.valueOf(body.get("installmentDaysPerPeriod").toString()) : null;
        log.info("POST /api/bills/{}/installment-preview billId={} installmentCount={} installmentDaysPerPeriod={}", id, id, installmentCount, installmentDaysPerPeriod);
        java.util.List<InstallmentScheduleItemDTO> schedule =
            billCommandService.previewInstallment(id, installmentCount, installmentDaysPerPeriod);
        log.debug("POST /api/bills/{}/installment-preview 返回 {} 期计划", id, schedule.size());
        return ResponseEntity.ok(schedule);
    }

    private static BillStatus parseStatus(String s) {
        if (s == null || s.isBlank()) return null;
        try {
            return BillStatus.valueOf(s);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
