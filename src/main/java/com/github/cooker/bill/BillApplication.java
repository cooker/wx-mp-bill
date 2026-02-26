package com.github.cooker.bill;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 银行账单系统启动类。
 * 提供产品、订单（交易明细）、账单管理，以及定时账单生成与逾期标记。
 */
@EnableScheduling
@SpringBootApplication
@MapperScan("com.github.cooker.bill.infrastructure.persistence.mapper")
public class BillApplication {

    public static void main(String[] args) {
        SpringApplication.run(BillApplication.class, args);
    }
}
