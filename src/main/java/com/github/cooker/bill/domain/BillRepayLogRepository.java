package com.github.cooker.bill.domain;

import java.util.List;

/**
 * 还款日志仓储。
 */
public interface BillRepayLogRepository {

    BillRepayLog save(BillRepayLog log);

    List<BillRepayLog> findByBillId(Long billId);
}
