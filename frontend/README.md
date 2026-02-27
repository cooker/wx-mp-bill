# 账单系统 - 本地调试前端

Vue 3 + Vite，按**功能**与**样式**拆分，便于维护。

## 目录结构

```
frontend/
├── index.html
├── package.json
├── vite.config.js
├── src/
│   ├── main.js              # 入口，挂载 App 并引入全局样式
│   ├── App.vue               # 根组件：顶栏、Tab、三个视图
│   ├── api/                  # 按领域拆分的接口
│   │   ├── request.js        # 封装 fetch
│   │   ├── products.js
│   │   ├── orders.js
│   │   ├── bills.js
│   │   └── index.js
│   ├── components/           # 按功能拆分的组件
│   │   ├── AppHeader.vue
│   │   ├── AppTabs.vue
│   │   ├── product/
│   │   │   ├── ProductForm.vue
│   │   │   └── ProductTable.vue
│   │   ├── order/
│   │   │   ├── OrderForm.vue
│   │   │   └── OrderTable.vue
│   │   └── bill/
│   │       ├── BillTable.vue
│   │       └── ScheduleModal.vue
│   ├── views/                # 页面级视图（组合组件 + 调用 API）
│   │   ├── ProductsView.vue
│   │   ├── OrdersView.vue
│   │   └── BillsView.vue
│   ├── styles/               # 样式按职责拆分
│   │   ├── variables.css     # CSS 变量（颜色、圆角等）
│   │   ├── base.css          # 重置、body
│   │   ├── common.css        # 按钮、表单、表格、通用类
│   │   └── index.css         # 汇总入口
│   └── utils/
│       └── format.js         # 时间、金额等格式化
```

## 环境与运行

- Node.js 18+
- 后端已启动在 **http://localhost:8080**
- 推荐使用 **pnpm**（项目含 `pnpm-lock.yaml`）；使用 npm 若遇依赖冲突可加 `--legacy-peer-deps` 或改用 pnpm。

```bash
cd frontend
pnpm install
pnpm run dev
```

浏览器访问 **http://localhost:5173**。Vite 代理将 `/api`、`/health` 转发到 8080。

## 说明

- **当前用户**：顶部输入 `userId`（如 `u1`），订单、账单按该用户查询。
- **产品**：列表、新增、编辑；分期产品在账单页用于「设为分期」。
- **订单**：创建订单、按用户刷新列表、退款。
- **账单**：本期应还、历史账单、还款、修改还款日、设为分期、查看还款计划。
