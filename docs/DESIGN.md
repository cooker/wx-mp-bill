# 银行账单系统 — 设计文档

## 1. 需求摘要（来自 TODO）

- **表**：用户表、用户会员卡表、产品表、订单表、账单表
- **定时任务**：账单生成、账单逾期
- **订单表**：订单号、商户订单号、交易卡号、交易时间、交易公司、金额、入账时间、交易渠道、交易类型
- **账单逻辑**：未结清时，退款、部分还款会使账单金额动态调整
- **技术栈**：Spring Boot、MyBatis-Plus、MySQL、JDK 21
- **约束**：代码备注需详细

## 2. 表结构（MySQL）

### 2.1 用户表 `user`

| 列 | 类型 | 说明 |
|----|------|------|
| id | BIGINT PK | 主键 |
| open_id | VARCHAR(64) | 微信 open_id / 第三方唯一标识 |
| mobile | VARCHAR(20) | 手机号 |
| nickname | VARCHAR(64) | 昵称 |
| register_source | VARCHAR(32) | 注册来源，如 WECHAT_MINI/APP/H5 |
| created_at, updated_at | DATETIME(3) | 审计 |

### 2.2 用户会员卡表 `user_member_card`

| 列 | 类型 | 说明 |
|----|------|------|
| id | BIGINT PK | 主键 |
| user_id | BIGINT | 关联 user.id |
| card_no | VARCHAR(32) | 会员卡号/交易卡号 |
| card_type | VARCHAR(32) | 卡类型：DEBIT-借记卡，CREDIT-信用卡(先用后付) |
| amount | DECIMAL(14,2) | 借记卡为账户余额，信用卡为授信额度 |
| status | VARCHAR(32) | ACTIVE/INACTIVE |
| created_at, updated_at | DATETIME(3) | 审计 |

### 2.3 产品表 `product`

| 列 | 类型 | 说明 |
|----|------|------|
| id | BIGINT PK | 主键 |
| name | VARCHAR(64) | 产品名称 |
| overdue_daily_rate | DECIMAL(10,6) | 逾期日利率，如 0.0005 表示 0.05%/天，为空用系统默认 |
| bill_type | VARCHAR(32) | 账单类型：NORMAL-正常账单，INSTALLMENT-分期账单 |
| created_at, updated_at | DATETIME(3) | 审计 |

### 2.4 订单表 `bill_order`（交易明细，N:1 归属月账单）

| 列 | 类型 | 说明 |
|----|------|------|
| id | BIGINT PK | 主键 |
| bill_id | BIGINT | 所属账单 id（月账单，多笔订单归属同一张） |
| order_no | VARCHAR(64) UK | 订单号（系统唯一） |
| merchant_order_no | VARCHAR(64) | 商户订单号 |
| order_type | VARCHAR(32) | NORMAL-正常订单，REFUND-退款单 |
| original_order_id | BIGINT | 原订单id，退款单时关联被退款的订单 |
| user_id | VARCHAR(64) | 用户标识 |
| card_no | VARCHAR(32) | 交易卡号 |
| trade_time | DATETIME(3) | 交易时间 |
| trade_company | VARCHAR(128) | 交易商户 |
| amount | DECIMAL(14,2) | 金额（退款单为退款金额，正数） |
| account_time | DATETIME(3) | 入账时间 |
| trade_channel | VARCHAR(32) | 交易渠道 |
| trade_type | VARCHAR(32) | 交易类型 |
| created_at, updated_at | DATETIME(3) | 审计 |

### 2.5 账单表 `bill`（与订单 1:N，支持正常/分期）

| 列 | 类型 | 说明 |
|----|------|------|
| id | BIGINT PK | 主键 |
| user_id | VARCHAR(64) | 用户 |
| bill_month | VARCHAR(7) | 账单月份 yyyy-MM |
| bill_type | VARCHAR(32) | 账单类型：NORMAL-正常账单，INSTALLMENT-分期账单 |
| installment_count | INT | 分期期数，仅分期账单有效，如 3/6/12 |
| total_amount | DECIMAL(14,2) | 应还总额（动态调整） |
| paid_amount | DECIMAL(14,2) | 已还金额 |
| status | VARCHAR(32) | UNPAID/PAID/OVERDUE |
| due_date | DATE | 还款日 |
| paid_at | DATETIME(3) | 结清时间 |
| created_at, updated_at | DATETIME(3) | 审计 |

**关系**：账单与订单为 **1 : N**（一张月账单对应多笔订单）；订单表通过 `bill_id` 归属到账单。同一用户同月可有多张账单：**当月已结清后若再产生新订单，会新生成一张未结清账单**（按 user_id + bill_month 查未结清账单归属，无则新建）。

**动态调整**：退款时扣减对应订单金额，未结清账单的 total_amount 扣减；部分还款增加 paid_amount，若 paid_amount >= total_amount 则结清。

**逾期利息**：还款日后**超过 3 天**起按日计息（第 4 天起算）。计息本金 = 应还 - 已还，利息 = 本金 × 日利率 × 计息天数；日利率默认 0.05%（可配置 `bill.default-overdue-daily-rate`）。应还合计 = 本金 + 逾期利息，还款时需达到应还合计方可结清。

## 3. 接口与定时任务

- GET /api/products — 产品列表
- GET/POST /api/orders — 订单列表、创建订单（必填 orderNo，可选 merchantOrderNo，含交易卡号等 7 个交易字段）
- POST /api/orders/{id}/refund — 退款（动态调整关联账单）
- GET /api/bills/current、/api/bills/history — 本期应还、历史
- POST /api/bills/{id}/repay — 部分/全额还款（动态更新 paid_amount）
- 定时任务：每日生成到期账单；每日标记逾期

## 4. 风险与对策

- 金额精度：BigDecimal + DECIMAL(14,2)
- 并发：账单更新时按 id 更新，避免重复还款
