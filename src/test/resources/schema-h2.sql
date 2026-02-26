CREATE TABLE IF NOT EXISTS user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    open_id VARCHAR(64),
    mobile VARCHAR(20),
    nickname VARCHAR(64),
    register_source VARCHAR(32)
);

CREATE TABLE IF NOT EXISTS user_member_card (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    card_no VARCHAR(32) NOT NULL,
    card_type VARCHAR(32) NOT NULL DEFAULT 'DEBIT',
    amount DECIMAL(14,2),
    status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE'
);

CREATE TABLE IF NOT EXISTS product (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(64) NOT NULL,
    overdue_daily_rate DECIMAL(10,6),
    bill_type VARCHAR(32) NOT NULL DEFAULT 'NORMAL',
    installment_daily_rate DECIMAL(10,6),
    installment_overdue_daily_rate DECIMAL(10,6),
    installment_per_period_fee_rate DECIMAL(10,6),
    installment_days_per_period INT
);

CREATE TABLE IF NOT EXISTS bill_order (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    bill_id BIGINT,
    order_no VARCHAR(64) NOT NULL,
    merchant_order_no VARCHAR(64),
    order_type VARCHAR(32) NOT NULL DEFAULT 'NORMAL',
    original_order_id BIGINT,
    user_id VARCHAR(64) NOT NULL,
    card_no VARCHAR(32) NOT NULL,
    trade_time TIMESTAMP NOT NULL,
    trade_company VARCHAR(128),
    amount DECIMAL(14,2) NOT NULL,
    account_time TIMESTAMP,
    trade_channel VARCHAR(32),
    trade_type VARCHAR(32)
);
CREATE UNIQUE INDEX IF NOT EXISTS uk_order_no ON bill_order(order_no);
CREATE INDEX IF NOT EXISTS idx_bill_id ON bill_order(bill_id);

CREATE TABLE IF NOT EXISTS bill (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id VARCHAR(64) NOT NULL,
    product_id BIGINT,
    bill_month VARCHAR(7) NOT NULL,
    bill_type VARCHAR(32) NOT NULL DEFAULT 'NORMAL',
    installment_count INT,
    installment_days_per_period INT,
    total_amount DECIMAL(14,2) NOT NULL,
    paid_amount DECIMAL(14,2) NOT NULL DEFAULT 0,
    status VARCHAR(32) NOT NULL DEFAULT 'UNPAID',
    due_date DATE NOT NULL,
    paid_at TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_user_bill_month ON bill(user_id, bill_month);

CREATE TABLE IF NOT EXISTS bill_installment_schedule (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    bill_id BIGINT NOT NULL,
    period_no INT NOT NULL,
    due_date DATE NOT NULL,
    planned_amount DECIMAL(14,2) NOT NULL,
    paid_amount DECIMAL(14,2) NOT NULL DEFAULT 0,
    paid_at TIMESTAMP
);
CREATE UNIQUE INDEX IF NOT EXISTS uk_bill_period ON bill_installment_schedule(bill_id, period_no);
CREATE INDEX IF NOT EXISTS idx_schedule_bill_id ON bill_installment_schedule(bill_id);
