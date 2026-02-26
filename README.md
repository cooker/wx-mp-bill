# 银行账单系统 (Bill)

基于 Spring Boot + MySQL 的账单与分期还款系统，支持订单入账、账单生成、分期计划、还款与逾期计息，并提供简易前端用于联调与演示。

## 功能特性

- **产品与订单**：产品表支持正常/分期类型及费率配置；订单表记录交易明细，支持退款单（REFUND）关联原单。
- **账单**：按用户、还款日、账单月份管理；支持**正常账单**与**分期账单**；未结清时退款、部分还款会动态调整应还总额。
- **分期计划**：分期账单可配置期数、每期天数；自动生成还款计划表（每期本金、利息、服务费）；支持修改还款日后整体平移应还日。
- **还款**：部分或全额还款；分期按「本金+利息+服务费+逾期利息」顺序分配至各期；已还 ≥ 应还则账单结清；每笔还款写入还款日志。
- **逾期**：超过还款日 3 天后按日计息（可配置日利率）；定时任务每日将到期未还账单标记为逾期。
- **定时任务**：每日 00:10 按入账日生成账单；每日 01:00 执行逾期状态更新。

## 技术栈

| 层级     | 技术 |
|----------|------|
| 后端     | Java 21、Spring Boot 3.2、MyBatis-Plus、MySQL 8 |
| 前端     | Vue 3、Vite 5 |
| 构建     | Maven、pnpm/npm |

## 项目结构

```
wx-mp-bill/
├── docs/sql/
│   └── schema_full_mysql.sql   # 全量建表脚本（MySQL）
├── frontend/                   # Vue 3 调试前端
│   ├── src/
│   └── package.json
├── src/main/java/com/github/cooker/bill/
│   ├── application/            # 应用服务（查询、还款、订单、账单生成等）
│   ├── domain/                 # 领域模型与仓储接口
│   ├── infrastructure/        # 持久化（MyBatis-Plus Mapper、Repository 实现）
│   └── interfaces/             # REST 控制器
├── src/test/                   # 单元测试（含 H2 内存库）
├── pom.xml
└── README.md
```

## 环境要求

- **JDK 21**
- **Maven 3.8+**
- **MySQL 8**（或兼容版本）
- **Node.js 18+**（仅运行前端时需要）

## 快速开始

### 1. 数据库

创建数据库并执行全量脚本：

```bash
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS bill CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
mysql -u root -p bill < docs/sql/schema_full_mysql.sql
```

### 2. 配置

复制或修改 `src/main/resources/application.yml` 中的数据库连接（默认 `localhost:3306/bill`，密码可通过环境变量 `DB_PASSWORD` 覆盖）：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/bill?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
    username: root
    password: ${DB_PASSWORD:123456}
```

可选配置：

- `bill.default-overdue-daily-rate`：默认逾期日利率（默认 `0.0005`，即 0.05%/天）
- `bill.schedule.bill-generate-cron` / `bill-overdue-cron`：定时任务 cron 表达式

### 3. 启动后端

```bash
./mvnw spring-boot:run
```

服务默认端口 **8080**；健康检查：`GET http://localhost:8080/health`。

### 4. 启动前端（可选）

用于本地联调与操作演示：

```bash
cd frontend
pnpm install   # 或 npm install
pnpm dev       # 或 npm run dev
```

浏览器访问 **http://localhost:5173**。前端通过 Vite 代理将 `/api` 转发到 `http://localhost:8080`。

## API 概览

接口仅使用 **GET** 与 **POST**。

| 方法 | 路径 | 说明 |
|------|------|------|
| GET  | `/api/bills/current` | 本期应还（userId, from, to） |
| GET  | `/api/bills/history` | 历史账单分页（userId, status?, offset, limit） |
| GET  | `/api/bills/{id}` | 账单详情（含分期计划） |
| POST | `/api/bills/{id}/repay` | 还款（body: `{ "amount": 100 }`） |
| POST | `/api/bills/{id}/update` | 修改账单类型/分期参数（body: billType, installmentCount 等） |
| POST | `/api/bills/{id}/due-date` | 修改还款日（body: `{ "dueDate": "yyyy-MM-dd" }`） |
| POST | `/api/bills/{id}/installment-preview` | 分期预览 |
| GET  | `/api/orders` | 订单列表（userId 等） |
| POST | `/api/orders` | 创建订单 |
| POST | `/api/orders/{id}/refund` | 退款 |
| GET  | `/api/products` | 产品列表 |
| GET  | `/api/products/{id}` | 产品详情 |
| POST | `/api/products` | 新增产品 |
| POST | `/api/products/{id}/update` | 更新产品 |
| GET  | `/health` | 健康检查 |

## 测试

```bash
./mvnw test
```

单元测试使用 H2 内存数据库（`src/test/resources/application-test.yml` + `schema-h2.sql`），无需 MySQL。

## 开源协议

本项目采用 [MIT License](LICENSE) 开源，可自由使用、修改与再分发。若仓库中暂无 `LICENSE` 文件，可自行添加 MIT 文本后使用。
