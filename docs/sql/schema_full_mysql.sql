-- 全量初始化脚本（MySQL）：用户 / 卡 / 产品 / 订单 / 账单
-- 适用于全新环境一次性建表，等价于依次执行 V1~V5。
SET NAMES utf8mb4;

-- 用户表
CREATE TABLE IF NOT EXISTS user (
    id              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    open_id         VARCHAR(64)     NULL     COMMENT '微信 open_id / 第三方唯一标识',
    mobile          VARCHAR(20)     NULL     COMMENT '手机号',
    nickname        VARCHAR(64)     NULL     COMMENT '昵称',
    register_source VARCHAR(32)     NULL     COMMENT '注册来源，如 WECHAT_MINI/APP/H5',
    created_at      DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at      DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    KEY idx_open_id (open_id),
    KEY idx_mobile (mobile),
    KEY idx_register_source (register_source)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 用户会员卡表
CREATE TABLE IF NOT EXISTS user_member_card (
    id          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_id     BIGINT UNSIGNED NOT NULL COMMENT '关联 user.id',
    card_no     VARCHAR(32)     NOT NULL COMMENT '会员卡号/交易卡号',
    card_type   VARCHAR(32)     NOT NULL DEFAULT 'DEBIT' COMMENT '卡类型：DEBIT-借记卡，CREDIT-信用卡(先用后付)',
    amount      DECIMAL(14,2)   NULL     COMMENT '借记卡为账户余额，信用卡为授信额度',
    status      VARCHAR(32)     NOT NULL DEFAULT 'ACTIVE' COMMENT 'ACTIVE/INACTIVE',
    created_at  DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at  DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    UNIQUE KEY uk_user_card (user_id, card_no),
    KEY idx_user_id (user_id),
    KEY idx_card_no (card_no),
    KEY idx_card_type (card_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户会员卡表';

-- 产品表（已包含分期相关配置）
CREATE TABLE IF NOT EXISTS product (
    id                              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    name                            VARCHAR(64)     NOT NULL COMMENT '产品名称',
    overdue_daily_rate              DECIMAL(10,6)   NULL     COMMENT '逾期日利率，如 0.0005 表示 0.05%/天，为空则用系统默认',
    bill_type                       VARCHAR(32)     NOT NULL DEFAULT 'NORMAL' COMMENT '账单类型：NORMAL-正常账单，INSTALLMENT-分期账单',
    installment_daily_rate          DECIMAL(10,6)   NULL     COMMENT '分期日利率，如 0.0003 表示 0.03%/天',
    installment_overdue_daily_rate  DECIMAL(10,6)   NULL     COMMENT '分期逾期日利率，如 0.0005 表示 0.05%/天',
    installment_per_period_fee_rate DECIMAL(10,6)   NULL     COMMENT '每期服务费本金利率，如 0.0001 表示按本金 0.01%/期',
    installment_days_per_period     INT             NULL     COMMENT '每期天数，如 30 表示每 30 天为一期',
    installment_period_count        INT             NULL     COMMENT '分期默认期数，如 3/6/12，仅分期产品有效',
    created_at                      DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at                      DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='产品表';

-- 订单/交易明细表（已包含 bill_id）
CREATE TABLE IF NOT EXISTS bill_order (
    id                BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    bill_id           BIGINT UNSIGNED NULL     COMMENT '所属账单',
    order_no          VARCHAR(64)     NOT NULL COMMENT '订单号（系统唯一）',
    merchant_order_no VARCHAR(64)     NULL     COMMENT '商户订单号',
    order_type        VARCHAR(32)     NOT NULL DEFAULT 'NORMAL' COMMENT 'NORMAL-正常订单，REFUND-退款单',
    original_order_id BIGINT UNSIGNED NULL     COMMENT '原订单id，退款单时必填，关联被退款的订单',
    user_id           VARCHAR(64)     NOT NULL COMMENT '用户标识',
    card_no           VARCHAR(32)     NOT NULL COMMENT '交易卡号',
    trade_time        DATETIME(3)     NOT NULL COMMENT '交易时间',
    trade_company     VARCHAR(128)    NULL     COMMENT '交易商户',
    amount            DECIMAL(14,2)   NOT NULL COMMENT '金额（退款单为退款金额，正数）',
    account_time      DATETIME(3)     NULL     COMMENT '入账时间',
    trade_channel     VARCHAR(32)     NULL     COMMENT '交易渠道',
    trade_type        VARCHAR(32)     NULL     COMMENT '交易类型',
    created_at        DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at        DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    UNIQUE KEY uk_order_no (order_no),
    KEY idx_merchant_order_no (merchant_order_no),
    KEY idx_user_id (user_id),
    KEY idx_trade_time (trade_time),
    KEY idx_original_order_id (original_order_id),
    KEY idx_order_type (order_type),
    KEY idx_bill_id (bill_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单/交易明细表';

-- 账单表（已包含账单月份、类型、分期期数）
CREATE TABLE IF NOT EXISTS bill (
    id                BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_id           VARCHAR(64)     NOT NULL COMMENT '用户标识',
    product_id        BIGINT UNSIGNED NULL COMMENT '分期时关联的产品',
    bill_month        VARCHAR(7)      NOT NULL COMMENT '账单月份 yyyy-MM',
    bill_type         VARCHAR(32)     NOT NULL DEFAULT 'NORMAL' COMMENT '账单类型：NORMAL-正常账单，INSTALLMENT-分期账单',
    installment_count INT             NULL COMMENT '分期期数，仅分期账单有效，如 3/6/12',
    installment_days_per_period INT   NULL COMMENT '分期时每期天数，为空则按自然月',
    total_amount      DECIMAL(14,2)   NOT NULL COMMENT '应还总额（退款等会动态调整）',
    paid_amount       DECIMAL(14,2)   NOT NULL DEFAULT 0 COMMENT '已还金额',
    status            VARCHAR(32)     NOT NULL DEFAULT 'UNPAID' COMMENT 'UNPAID/PAID/OVERDUE',
    due_date          DATE            NOT NULL COMMENT '还款日',
    paid_at           DATETIME(3)     NULL     COMMENT '结清时间',
    created_at        DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at        DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    KEY idx_user_due (user_id, due_date),
    KEY idx_status_due (status, due_date),
    KEY idx_user_bill_month (user_id, bill_month)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='账单表';

-- 分期还款计划表（记录每期应还与实际还款明细，含利息、服务费、逾期利息）
CREATE TABLE IF NOT EXISTS bill_installment_schedule (
    id              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    bill_id         BIGINT UNSIGNED NOT NULL COMMENT '关联 bill.id',
    period_no       INT             NOT NULL COMMENT '期号 1~N',
    due_date        DATE            NOT NULL COMMENT '应还日',
    planned_amount  DECIMAL(14,2)   NOT NULL COMMENT '应还金额（元）',
    paid_amount     DECIMAL(14,2)   NOT NULL DEFAULT 0 COMMENT '已还金额（元）',
    paid_at         DATETIME(3)     NULL     COMMENT '该期结清时间',
    interest        DECIMAL(14,2)   NOT NULL DEFAULT 0 COMMENT '本期利息（元）',
    service_fee     DECIMAL(14,2)   NOT NULL DEFAULT 0 COMMENT '本期服务费（元）',
    overdue_interest DECIMAL(14,2)   NOT NULL DEFAULT 0 COMMENT '本期逾期利息（元）',
    created_at      DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at      DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    UNIQUE KEY uk_bill_period (bill_id, period_no),
    KEY idx_bill_id (bill_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='分期还款计划表';

