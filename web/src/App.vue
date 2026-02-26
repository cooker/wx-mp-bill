<template>
  <div class="app">
    <header class="header">
      <h1>账单系统 · 本地调试</h1>
      <div class="user-bar">
        <label>当前用户</label>
        <input v-model="userId" type="text" placeholder="输入 userId 如 u1" />
        <span class="hint">订单/账单列表均按此用户查询</span>
      </div>
    </header>

    <nav class="tabs">
      <button :class="{ active: tab === 'products' }" @click="tab = 'products'">产品</button>
      <button :class="{ active: tab === 'orders' }" @click="tab = 'orders'">订单</button>
      <button :class="{ active: tab === 'bills' }" @click="tab = 'bills'">账单</button>
    </nav>

    <main class="main">
      <!-- 产品配置 -->
      <section v-show="tab === 'products'" class="panel">
        <h2>产品配置</h2>
        <div class="form-card">
          <h3>{{ editingProductId ? '编辑产品' : '新增产品' }}</h3>
          <div class="form-row">
            <input v-model="productForm.name" placeholder="产品名称 *" />
            <input v-model="productForm.overdueDailyRate" type="number" step="0.000001" placeholder="逾期日利率，如 0.0005" />
            <select v-model="productForm.billType">
              <option value="NORMAL">正常账单</option>
              <option value="INSTALLMENT">分期账单</option>
            </select>
          </div>
          <div v-if="productForm.billType === 'INSTALLMENT'" class="form-row form-row-installment">
            <span class="form-label">分期配置</span>
            <input v-model="productForm.installmentPeriodCount" type="number" min="1" placeholder="分期期数" title="如 3/6/12" />
            <input v-model="productForm.installmentDaysPerPeriod" type="number" min="1" placeholder="每期天数" title="如 30" />
            <input v-model="productForm.installmentDailyRate" type="number" step="0.000001" placeholder="分期日利率" title="如 0.0003" />
            <input v-model="productForm.installmentOverdueDailyRate" type="number" step="0.000001" placeholder="分期逾期日利率" title="如 0.0005" />
            <input v-model="productForm.installmentPerPeriodFeeRate" type="number" step="0.000001" placeholder="每期服务费本金利率" title="如 0.0001" />
          </div>
          <button class="btn primary" @click="saveProduct" :disabled="loading">{{ editingProductId ? '保存' : '新增' }}</button>
          <button v-if="editingProductId" class="btn" @click="cancelEditProduct">取消</button>
        </div>
        <div class="table-actions">
          <button class="btn" @click="loadProducts" :disabled="loading">刷新列表</button>
        </div>
        <p v-if="productErr" class="error">{{ productErr }}</p>
        <div v-else-if="products.length" class="table-wrap">
          <table>
            <thead>
              <tr>
                <th>ID</th>
                <th>名称</th>
                <th>逾期日利率</th>
                <th>账单类型</th>
                <th>分期日利率</th>
                <th>分期逾期日利率</th>
                <th>每期服务费本金利率</th>
                <th>分期期数</th>
                <th>每期天数</th>
                <th>操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="p in products" :key="p.id">
                <td>{{ p.id }}</td>
                <td>{{ p.name }}</td>
                <td>{{ p.overdueDailyRate ?? '-' }}</td>
                <td>{{ p.billType === 'INSTALLMENT' ? '分期账单' : '正常账单' }}</td>
                <td>{{ p.installmentDailyRate != null ? p.installmentDailyRate : '-' }}</td>
                <td>{{ p.installmentOverdueDailyRate != null ? p.installmentOverdueDailyRate : '-' }}</td>
                <td>{{ p.installmentPerPeriodFeeRate != null ? p.installmentPerPeriodFeeRate : '-' }}</td>
                <td>{{ p.installmentPeriodCount != null ? p.installmentPeriodCount : '-' }}</td>
                <td>{{ p.installmentDaysPerPeriod != null ? p.installmentDaysPerPeriod : '-' }}</td>
                <td>
                  <button class="btn small" @click="editProduct(p)" :disabled="loading">编辑</button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
        <p v-else class="muted">暂无数据，请新增产品</p>
      </section>

      <!-- 订单 -->
      <section v-show="tab === 'orders'" class="panel">
        <h2>订单</h2>
        <div class="form-card">
          <h3>创建订单</h3>
          <div class="form-row">
            <input v-model="form.orderNo" placeholder="订单号 *" />
            <input v-model="form.merchantOrderNo" placeholder="商户订单号" />
          </div>
          <div class="form-row">
            <input v-model="form.cardNo" placeholder="交易卡号 *" />
            <input v-model="form.tradeCompany" placeholder="交易商户" />
          </div>
          <div class="form-row">
            <input v-model="form.amount" type="number" step="0.01" placeholder="金额 *" />
            <input v-model="form.tradeChannel" placeholder="交易渠道" />
            <input v-model="form.tradeType" placeholder="交易类型" />
          </div>
          <div class="form-row">
            <label class="form-label">账单类型（仅对新建账单生效）</label>
            <select v-model="form.billType">
              <option value="NORMAL">正常</option>
              <option value="INSTALLMENT">分期</option>
            </select>
            <template v-if="form.billType === 'INSTALLMENT'">
              <select v-model="form.installmentCount">
                <option :value="3">3 期</option>
                <option :value="6">6 期</option>
                <option :value="12">12 期</option>
              </select>
              <input v-model.number="form.installmentDaysPerPeriod" type="number" min="1" placeholder="每期天数" class="repay-input" title="如 30" />
            </template>
          </div>
          <button class="btn primary" @click="createOrder" :disabled="loading">创建</button>
        </div>
        <div class="table-actions">
          <button class="btn" @click="loadOrders" :disabled="loading">刷新订单列表</button>
        </div>
        <p v-if="orderErr" class="error">{{ orderErr }}</p>
        <div v-else-if="orders.length" class="table-wrap">
          <table>
            <thead>
              <tr>
                <th>ID</th>
                <th>订单号</th>
                <th>类型</th>
                <th>金额</th>
                <th>交易时间</th>
                <th>操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="o in orders" :key="o.id">
                <td>{{ o.id }}</td>
                <td>{{ o.orderNo }}</td>
                <td>{{ o.orderType }}</td>
                <td>{{ o.amount }}</td>
                <td>{{ formatTime(o.tradeTime) }}</td>
                <td>
                  <button
                    v-if="o.orderType === 'NORMAL' && !o.refunded"
                    class="btn small danger"
                    @click="refundOrder(o.id)"
                    :disabled="loading"
                  >
                    退款
                  </button>
                  <span v-else-if="o.orderType === 'NORMAL' && o.refunded" class="muted">已退款</span>
                  <span v-else class="muted">退款单</span>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
        <p v-else class="muted">暂无订单（请先选择/输入 userId 并创建订单）</p>
      </section>

      <!-- 账单 -->
      <section v-show="tab === 'bills'" class="panel">
        <h2>账单</h2>
        <div class="bill-actions">
          <button class="btn" @click="loadCurrentBills" :disabled="loading">本期应还</button>
          <button class="btn" @click="loadHistoryBills" :disabled="loading">历史账单</button>
        </div>
        <p v-if="billErr" class="error">{{ billErr }}</p>
        <div v-else-if="bills.length" class="table-wrap">
          <table class="bills-table">
            <colgroup>
              <col style="width: 3rem" />
              <col style="width: 5rem" />
              <col style="width: 220px" />
              <col style="width: 5rem" />
              <col style="width: 5.5rem" />
              <col style="width: 4.5rem" />
              <col style="width: 4.5rem" />
              <col style="width: 5rem" />
              <col style="width: 4.5rem" />
              <col style="width: 4.5rem" />
              <col style="width: 10rem" />
              <col style="width: 12rem" />
              <col style="width: 12rem" />
            </colgroup>
            <thead>
              <tr>
                <th>ID</th>
                <th>月份</th>
                <th>类型</th>
                <th>每期金额</th>
                <th>应还总额</th>
                <th>利息</th>
                <th>服务费</th>
                <th title="逾期后产生">逾期利息</th>
                <th>已还</th>
                <th>状态</th>
                <th>还款日</th>
                <th>计划</th>
                <th>操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="b in bills" :key="b.id">
                <td>{{ b.id }}</td>
                <td>{{ b.billMonth }}</td>
                <td class="cell-type">
                  <template v-if="b.billType === 'INSTALLMENT' && b.installmentCount">{{ b.installmentCount }} 期<template v-if="b.installmentDaysPerPeriod"> / {{ b.installmentDaysPerPeriod }} 天</template></template>
                  <template v-else-if="(b.status === 'UNPAID' || b.status === 'OVERDUE') && b.billType !== 'INSTALLMENT'">
                    <div class="installment-inline">
                      <select v-model="installmentProductIds[b.id]" class="repay-input">
                        <option value="">选择分期产品</option>
                        <option v-for="p in products.filter(pp => pp.billType === 'INSTALLMENT')" :key="p.id" :value="p.id">
                          {{ p.name }}(#{{ p.id }})<template v-if="p.installmentPeriodCount"> {{ p.installmentPeriodCount }}期</template><template v-if="p.installmentDaysPerPeriod">/{{ p.installmentDaysPerPeriod }}天</template>
                        </option>
                      </select>
                      <button
                        class="btn small"
                        @click="setBillInstallment(b.id, installmentProductIds[b.id])"
                        :disabled="loading || !installmentProductIds[b.id]"
                      >分期</button>
                    </div>
                  </template>
                  <span v-else>正常</span>
                </td>
                <td class="amount">{{ formatAmount(b.installmentPerAmount) }}</td>
                <td class="amount">{{ formatAmount(b.totalDueAmount ?? b.totalAmount) }}</td>
                <td class="amount">{{ b.billType === 'INSTALLMENT' ? '¥' + formatScheduleAmount(b.totalInterest) : '-' }}</td>
                <td class="amount">{{ b.billType === 'INSTALLMENT' ? '¥' + formatScheduleAmount(b.totalServiceFee) : '-' }}</td>
                <td class="amount">{{ b.overdueInterest != null && Number(b.overdueInterest) > 0 ? '¥' + formatScheduleAmount(b.overdueInterest) : '-' }}</td>
                <td class="amount">{{ formatAmount(b.paidAmount) }}</td>
                <td>
                  <span
                    v-if="b.status === 'UNPAID'"
                    class="status-badge status-unpaid"
                  >待还</span>
                  <span
                    v-else-if="b.status === 'OVERDUE'"
                    class="status-badge status-overdue"
                  >逾期</span>
                  <span
                    v-else
                    class="status-badge status-paid"
                  >已结清</span>
                </td>
                <td>
                  <input
                    :id="`due-input-${b.id}`"
                    type="date"
                    :value="b.dueDate"
                    class="repay-input"
                    style="width: 100%"
                  />
                </td>
                <td class="cell-actions cell-buttons">
                  <span class="btn-group">
                    <button
                      v-if="b.billType === 'INSTALLMENT' && b.installmentSchedule?.length"
                      class="btn small"
                      @click="showSchedule(b)"
                    >查看计划</button>
                    <button
                      class="btn small"
                      @click="updateBillDueDate(b)"
                      :disabled="loading"
                    >保存还款日</button>
                  </span>
                </td>
                <td class="cell-actions cell-repay">
                  <template v-if="b.status === 'UNPAID' || b.status === 'OVERDUE'">
                    <span class="repay-group">
                      <input
                        v-model="repayAmounts[b.id]"
                        type="number"
                        step="0.01"
                        placeholder="金额"
                        class="repay-input repay-amount"
                      />
                      <button
                        class="btn small primary"
                        @click="repayBill(b.id, repayAmounts[b.id])"
                        :disabled="loading"
                      >还款</button>
                    </span>
                  </template>
                  <span v-else class="muted">已结清</span>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
        <p v-else class="muted">暂无账单数据（先查本期应还或历史）</p>

        <!-- 还款计划弹层 -->
        <Teleport to="body">
          <Transition name="schedule-modal">
            <div v-if="scheduleBill" class="schedule-overlay" @click.self="scheduleBill = null">
              <div class="schedule-modal">
                <div class="schedule-modal-header">
                  <h3>还款计划 · 账单 #{{ scheduleBill.id }}</h3>
                  <button type="button" class="schedule-close" aria-label="关闭" @click="scheduleBill = null">×</button>
                </div>
                <div class="schedule-summary">
                  <span>账单月 {{ scheduleBill.billMonth }}</span>
                  <span>共 {{ scheduleBill.installmentCount }} 期</span>
                  <span>应还总额 <strong>¥{{ formatAmount(scheduleBill.totalDueAmount ?? scheduleBill.totalAmount) }}</strong></span>
                  <span>已还 <strong>¥{{ scheduleBill.paidAmount }}</strong></span>
                </div>
                <div class="schedule-table-wrap">
                  <table class="schedule-table">
                    <thead>
                      <tr>
                        <th>期数</th>
                        <th>应还日</th>
                        <th>本金</th>
                        <th>利息</th>
                        <th>服务费</th>
                        <th>逾期利息</th>
                        <th>应还金额</th>
                        <th>已还金额</th>
                        <th>还款日</th>
                        <th>状态</th>
                      </tr>
                    </thead>
                    <tbody>
                      <tr v-for="s in scheduleBill.installmentSchedule" :key="s.periodNo" :class="{ 'row-paid': s.status === 'PAID' }">
                        <td>第 {{ s.periodNo }} 期</td>
                        <td>{{ s.dueDate }}</td>
                        <td class="amount">¥{{ formatAmount(s.amount) }}</td>
                        <td class="amount">¥{{ formatScheduleAmount(s.interest) }}</td>
                        <td class="amount">¥{{ formatScheduleAmount(s.serviceFee) }}</td>
                        <td class="amount">¥{{ formatScheduleAmount(s.overdueInterest) }}</td>
                        <td class="amount">¥{{ formatScheduleAmount(schedulePeriodTotal(s)) }}</td>
                        <td class="amount">{{ s.paidAmount != null && Number(s.paidAmount) > 0 ? '¥' + formatAmount(s.paidAmount) : '-' }}</td>
                        <td>{{ s.paidAt ? formatTime(s.paidAt) : '-' }}</td>
                        <td>
                          <span :class="['schedule-status', s.status === 'PAID' ? 'status-paid' : 'status-pending']">
                            {{ s.status === 'PAID' ? '已还' : '待还' }}
                          </span>
                        </td>
                      </tr>
                    </tbody>
                  </table>
                </div>
                <div class="schedule-modal-footer">
                  <button class="btn primary" @click="scheduleBill = null">关闭</button>
                </div>
              </div>
            </div>
          </Transition>
        </Teleport>
      </section>
    </main>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { api } from './api'

const tab = ref('products')
const userId = ref('u1')
const loading = ref(false)
const err = ref('')
const productErr = ref('')
const orderErr = ref('')
const billErr = ref('')

const products = ref([])
const orders = ref([])
const bills = ref([])
const repayAmounts = reactive({})
const installmentPeriods = reactive({})
const installmentDaysPerBill = reactive({})
const installmentProductIds = reactive({})
const scheduleBill = ref(null)
const editingProductId = ref(null)
const productForm = reactive({
  name: '',
  overdueDailyRate: '',
  billType: 'NORMAL',
  installmentDailyRate: '',
  installmentOverdueDailyRate: '',
  installmentPerPeriodFeeRate: '',
  installmentPeriodCount: '',
  installmentDaysPerPeriod: '',
})

const form = reactive({
  orderNo: '',
  merchantOrderNo: '',
  cardNo: '',
  tradeCompany: '',
  amount: '',
  tradeChannel: '',
  tradeType: '',
  billType: 'NORMAL',
  installmentCount: 3,
  installmentDaysPerPeriod: 30,
})

function formatTime(iso) {
  if (!iso) return '-'
  try {
    const d = new Date(iso)
    return d.toLocaleString('zh-CN')
  } catch {
    return iso
  }
}

function formatAmount(v) {
  if (v == null || v === '') return '-'
  const n = Number(v)
  if (Number.isNaN(n)) return String(v)
  return n.toFixed(2)
}

/** 还款计划中利息/服务费/逾期利息：缺省或 null 时显示 0.00，保证列始终有值 */
function formatScheduleAmount(v) {
  if (v == null || v === '') return '0.00'
  const n = Number(v)
  return Number.isNaN(n) ? '0.00' : n.toFixed(2)
}

/** 本期应还金额 = 本金 + 利息 + 服务费 + 逾期利息 */
function schedulePeriodTotal(s) {
  const a = Number(s?.amount) || 0
  const i = Number(s?.interest) || 0
  const f = Number(s?.serviceFee) || 0
  const o = Number(s?.overdueInterest) || 0
  return a + i + f + o
}

async function loadProducts() {
  productErr.value = ''
  loading.value = true
  try {
    const res = await api.products.list()
    products.value = res ?? []
  } catch (e) {
    productErr.value = e.message
    products.value = []
  } finally {
    loading.value = false
  }
}

function editProduct(p) {
  editingProductId.value = p.id
  productForm.name = p.name ?? ''
  productForm.overdueDailyRate = p.overdueDailyRate != null ? String(p.overdueDailyRate) : ''
  productForm.billType = p.billType ?? 'NORMAL'
  productForm.installmentDailyRate = p.installmentDailyRate != null ? String(p.installmentDailyRate) : ''
  productForm.installmentOverdueDailyRate = p.installmentOverdueDailyRate != null ? String(p.installmentOverdueDailyRate) : ''
  productForm.installmentPerPeriodFeeRate = p.installmentPerPeriodFeeRate != null ? String(p.installmentPerPeriodFeeRate) : ''
  productForm.installmentPeriodCount = p.installmentPeriodCount != null ? String(p.installmentPeriodCount) : ''
  productForm.installmentDaysPerPeriod = p.installmentDaysPerPeriod != null ? String(p.installmentDaysPerPeriod) : ''
}

function cancelEditProduct() {
  editingProductId.value = null
  productForm.name = ''
  productForm.overdueDailyRate = ''
  productForm.billType = 'NORMAL'
  productForm.installmentDailyRate = ''
  productForm.installmentOverdueDailyRate = ''
  productForm.installmentPerPeriodFeeRate = ''
  productForm.installmentPeriodCount = ''
  productForm.installmentDaysPerPeriod = ''
}

async function saveProduct() {
  productErr.value = ''
  if (!productForm.name?.trim()) {
    productErr.value = '产品名称必填'
    return
  }
  loading.value = true
  try {
    const body = {
      name: productForm.name.trim(),
      billType: productForm.billType,
    }
    if (productForm.overdueDailyRate !== '' && productForm.overdueDailyRate != null) {
      body.overdueDailyRate = Number(productForm.overdueDailyRate)
    }
    if (productForm.installmentDailyRate !== '' && productForm.installmentDailyRate != null) {
      body.installmentDailyRate = Number(productForm.installmentDailyRate)
    }
    if (productForm.installmentOverdueDailyRate !== '' && productForm.installmentOverdueDailyRate != null) {
      body.installmentOverdueDailyRate = Number(productForm.installmentOverdueDailyRate)
    }
    if (productForm.installmentPerPeriodFeeRate !== '' && productForm.installmentPerPeriodFeeRate != null) {
      body.installmentPerPeriodFeeRate = Number(productForm.installmentPerPeriodFeeRate)
    }
    if (productForm.installmentPeriodCount !== '' && productForm.installmentPeriodCount != null) {
      body.installmentPeriodCount = Number(productForm.installmentPeriodCount)
    }
    if (productForm.installmentDaysPerPeriod !== '' && productForm.installmentDaysPerPeriod != null) {
      body.installmentDaysPerPeriod = Number(productForm.installmentDaysPerPeriod)
    }
    if (editingProductId.value) {
      await api.products.update(editingProductId.value, body)
    } else {
      await api.products.create(body)
    }
    cancelEditProduct()
    await loadProducts()
  } catch (e) {
    productErr.value = e.message
  } finally {
    loading.value = false
  }
}

async function loadOrders() {
  orderErr.value = ''
  if (!userId.value.trim()) {
    orderErr.value = '请先输入 userId'
    return
  }
  loading.value = true
  try {
    const res = await api.orders.list(userId.value, 0, 50)
    orders.value = res?.items ?? []
  } catch (e) {
    orderErr.value = e.message
    orders.value = []
  } finally {
    loading.value = false
  }
}

async function createOrder() {
  orderErr.value = ''
  if (!form.orderNo?.trim() || !form.cardNo?.trim() || !form.amount || !userId.value.trim()) {
    orderErr.value = '订单号、卡号、金额、userId 必填'
    return
  }
  loading.value = true
  try {
    const body = {
      orderNo: form.orderNo.trim(),
      merchantOrderNo: form.merchantOrderNo?.trim() || null,
      userId: userId.value.trim(),
      cardNo: form.cardNo.trim(),
      tradeTime: new Date().toISOString(),
      tradeCompany: form.tradeCompany?.trim() || null,
      amount: Number(form.amount),
      accountTime: new Date().toISOString(),
      tradeChannel: form.tradeChannel?.trim() || null,
      tradeType: form.tradeType?.trim() || null,
    }
    if (form.billType === 'INSTALLMENT') {
      body.billType = 'INSTALLMENT'
      body.installmentCount = form.installmentCount
      if (form.installmentDaysPerPeriod != null && form.installmentDaysPerPeriod > 0) {
        body.installmentDaysPerPeriod = form.installmentDaysPerPeriod
      }
    }
    await api.orders.create(body)
    form.orderNo = ''
    form.merchantOrderNo = ''
    form.amount = ''
    await loadOrders()
  } catch (e) {
    orderErr.value = e.message
  } finally {
    loading.value = false
  }
}

async function refundOrder(id) {
  orderErr.value = ''
  loading.value = true
  try {
    await api.orders.refund(id)
    await loadOrders()
  } catch (e) {
    orderErr.value = e.message
  } finally {
    loading.value = false
  }
}

async function showSchedule(b) {
  if (!b?.id) {
    scheduleBill.value = b
    return
  }
  try {
    const fresh = await api.bills.get(b.id)
    scheduleBill.value = fresh ?? b
  } catch (e) {
    scheduleBill.value = b
  }
}

function dateRange() {
  const now = new Date()
  const from = new Date(now.getFullYear(), now.getMonth(), 1)
  const to = new Date(now.getFullYear(), now.getMonth() + 2, 0)
  return {
    from: from.toISOString().slice(0, 10),
    to: to.toISOString().slice(0, 10),
  }
}

async function loadCurrentBills() {
  billErr.value = ''
  if (!userId.value.trim()) {
    billErr.value = '请先输入 userId'
    return
  }
  loading.value = true
  try {
    const { from, to } = dateRange()
    const res = await api.bills.current(userId.value, from, to)
    bills.value = res ?? []
  } catch (e) {
    billErr.value = e.message
    bills.value = []
  } finally {
    loading.value = false
  }
}

async function loadHistoryBills() {
  billErr.value = ''
  if (!userId.value.trim()) {
    billErr.value = '请先输入 userId'
    return
  }
  loading.value = true
  try {
    const res = await api.bills.history(userId.value, null, 0, 50)
    bills.value = res?.items ?? []
  } catch (e) {
    billErr.value = e.message
    bills.value = []
  } finally {
    loading.value = false
  }
}

async function repayBill(id, amount) {
  const n = Number(amount)
  if (!Number.isFinite(n) || n <= 0) {
    billErr.value = '请输入有效还款金额'
    return
  }
  billErr.value = ''
  loading.value = true
  try {
    await api.bills.repay(id, n)
    repayAmounts[id] = ''
    await loadCurrentBills()
  } catch (e) {
    billErr.value = e.message
  } finally {
    loading.value = false
  }
}

async function setBillInstallment(billId, productId) {
  if (!productId) return
  billErr.value = ''
  loading.value = true
  try {
    const body = { billType: 'INSTALLMENT', productId: Number(productId) }
    await api.bills.setInstallment(billId, body)
    installmentProductIds[billId] = ''
    await loadCurrentBills()
  } catch (e) {
    billErr.value = e.message
  } finally {
    loading.value = false
  }
}

async function updateBillDueDate(bill) {
  if (!bill || !bill.id) return
  const inputId = `due-input-${bill.id}`
  const el = document.getElementById(inputId)
  const value = el?.value
  if (!value) return
  billErr.value = ''
  loading.value = true
  try {
    await api.bills.updateDueDate(bill.id, value)
    await loadCurrentBills()
  } catch (e) {
    billErr.value = e.message
  } finally {
    loading.value = false
  }
}

// 默认加载产品
loadProducts()
</script>

<style>
* {
  box-sizing: border-box;
}
body {
  margin: 0;
  font-family: 'Segoe UI', 'PingFang SC', 'Microsoft YaHei', sans-serif;
  background: #f5f6f8;
  color: #1a1d21;
}
.app {
  max-width: 1000px;
  margin: 0 auto;
  padding: 1rem 1.5rem;
}
.header {
  margin-bottom: 1.5rem;
}
.header h1 {
  font-size: 1.5rem;
  font-weight: 600;
  margin: 0 0 0.75rem 0;
  color: #0f172a;
}
.user-bar {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  flex-wrap: wrap;
}
.user-bar label {
  font-weight: 500;
}
.user-bar input {
  width: 140px;
  padding: 0.4rem 0.6rem;
  border: 1px solid #cbd5e1;
  border-radius: 6px;
}
.user-bar .hint {
  font-size: 0.85rem;
  color: #64748b;
}
.tabs {
  display: flex;
  gap: 0.25rem;
  margin-bottom: 1.25rem;
}
.tabs button {
  padding: 0.5rem 1rem;
  border: 1px solid #e2e8f0;
  background: #fff;
  border-radius: 6px;
  cursor: pointer;
  font-size: 0.95rem;
}
.tabs button.active {
  background: #0f172a;
  color: #fff;
  border-color: #0f172a;
}
.main {
  background: #fff;
  border-radius: 10px;
  padding: 1.25rem;
  box-shadow: 0 1px 3px rgba(0,0,0,0.06);
}
.panel h2 {
  font-size: 1.15rem;
  margin: 0 0 1rem 0;
}
.panel h3 {
  font-size: 1rem;
  margin: 0 0 0.75rem 0;
}
.btn {
  padding: 0.45rem 0.9rem;
  border: 1px solid #cbd5e1;
  background: #fff;
  border-radius: 6px;
  cursor: pointer;
  font-size: 0.9rem;
  margin-right: 0.5rem;
  margin-bottom: 0.5rem;
}
.btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
.btn.primary {
  background: #0f172a;
  color: #fff;
  border-color: #0f172a;
}
.btn.small {
  padding: 0.25rem 0.5rem;
  font-size: 0.8rem;
}
.btn.danger {
  color: #b91c1c;
  border-color: #fecaca;
  background: #fef2f2;
}
.form-card {
  background: #f8fafc;
  padding: 1rem;
  border-radius: 8px;
  margin-bottom: 1rem;
}
.form-row {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
  margin-bottom: 0.5rem;
}
.form-row .form-label {
  min-width: 10rem;
  font-size: 0.9rem;
  color: #475569;
}
.form-row input,
.form-row select {
  padding: 0.4rem 0.6rem;
  border: 1px solid #e2e8f0;
  border-radius: 6px;
  min-width: 120px;
}
.repay-input {
  width: 90px;
  padding: 0.25rem 0.4rem;
  margin-right: 0.25rem;
  border: 1px solid #e2e8f0;
  border-radius: 4px;
}
.table-actions,
.bill-actions {
  margin-bottom: 0.75rem;
}
.table-wrap {
  overflow-x: auto;
}
table {
  width: 100%;
  border-collapse: collapse;
  font-size: 0.9rem;
}
th, td {
  padding: 0.5rem 0.75rem;
  text-align: left;
  border-bottom: 1px solid #e2e8f0;
}
th {
  font-weight: 600;
  background: #f8fafc;
  color: #475569;
  white-space: nowrap;
}
/* 账单列表表：固定列宽防错位 */
.bills-table {
  table-layout: fixed;
  min-width: 980px;
}
.bills-table .amount {
  text-align: right;
  font-variant-numeric: tabular-nums;
}
.bills-table .cell-type {
  overflow: hidden;
  max-width: 200px;
}
.bills-table .installment-inline {
  display: flex;
  flex-wrap: wrap;
  gap: 0.35rem;
  align-items: center;
}
.bills-table .installment-inline .repay-input {
  min-width: 0;
  max-width: 100%;
}
.bills-table .installment-inline select.repay-input {
  max-width: 110px;
}
.bills-table .installment-inline input.repay-input {
  width: 4rem;
}
.bills-table .cell-actions {
  white-space: nowrap;
  vertical-align: middle;
}
.bills-table .cell-buttons .btn-group,
.bills-table .cell-repay .repay-group {
  display: inline-flex;
  align-items: center;
  gap: 0.35rem;
  flex-wrap: nowrap;
}
.bills-table .cell-buttons .btn-group .btn,
.bills-table .cell-repay .repay-group .btn {
  flex-shrink: 0;
  margin: 0;
}
.bills-table .cell-repay .repay-amount {
  width: 5rem;
  min-width: 5rem;
  box-sizing: border-box;
  margin: 0;
}
.bills-table .repay-input {
  height: 1.85rem;
  line-height: 1.2;
  vertical-align: middle;
}
.bills-table .cell-actions .btn-group .btn,
.bills-table .cell-actions .repay-group .btn,
.bills-table .cell-actions .repay-group .repay-input {
  margin: 0;
}
.bills-table .btn.small {
  height: 1.85rem;
  line-height: 1.2;
  padding: 0.25rem 0.5rem;
  box-sizing: border-box;
}
.status-paid {
  color: #059669;
  font-weight: 500;
}

/* 还款计划弹窗 */
.schedule-overlay {
  position: fixed;
  inset: 0;
  background: rgba(15, 23, 42, 0.5);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 100;
  padding: 1rem;
}
.schedule-modal {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.25);
  max-width: 90vw;
  width: 800px;
  min-height: 420px;
  max-height: calc(100vh - 2rem);
  overflow: hidden;
  display: flex;
  flex-direction: column;
}
.schedule-modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 1.25rem 1.5rem;
  border-bottom: 1px solid #e2e8f0;
  background: #f8fafc;
}
.schedule-modal-header h3 {
  margin: 0;
  font-size: 1.2rem;
  font-weight: 600;
  color: #0f172a;
}
.schedule-close {
  width: 32px;
  height: 32px;
  border: none;
  background: transparent;
  color: #64748b;
  font-size: 1.5rem;
  line-height: 1;
  cursor: pointer;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: color 0.15s, background 0.15s;
}
.schedule-close:hover {
  color: #0f172a;
  background: #e2e8f0;
}
.schedule-summary {
  display: flex;
  flex-wrap: wrap;
  gap: 0.75rem 1.5rem;
  padding: 1rem 1.5rem;
  font-size: 0.9rem;
  color: #64748b;
  background: #fff;
  border-bottom: 1px solid #f1f5f9;
}
.schedule-summary strong {
  color: #0f172a;
  font-weight: 600;
}
.schedule-table-wrap {
  overflow: auto;
  flex: 1;
  min-height: 280px;
}
.schedule-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 0.9rem;
}
.schedule-table th,
.schedule-table td {
  padding: 0.75rem 1.25rem;
  text-align: left;
  border-bottom: 1px solid #f1f5f9;
}
.schedule-table th {
  font-weight: 600;
  color: #475569;
  background: #f8fafc;
  white-space: nowrap;
}
.schedule-table td.amount {
  font-variant-numeric: tabular-nums;
  font-weight: 500;
  color: #0f172a;
}
.schedule-table tr.row-paid {
  background: #f0fdf4;
}
.schedule-table tr.row-paid td {
  color: #166534;
}
.schedule-status {
  display: inline-block;
  padding: 0.2rem 0.5rem;
  border-radius: 999px;
  font-size: 0.75rem;
  font-weight: 500;
}
.schedule-status.status-paid {
  background: #dcfce7;
  color: #166534;
}
.schedule-status.status-pending {
  background: #fef3c7;
  color: #92400e;
}
.schedule-modal-footer {
  padding: 1.25rem 1.5rem;
  border-top: 1px solid #e2e8f0;
  background: #f8fafc;
}
.schedule-modal-footer .btn {
  margin: 0;
}

/* 弹窗过渡 */
.schedule-modal-enter-active,
.schedule-modal-leave-active {
  transition: opacity 0.2s ease;
}
.schedule-modal-enter-active .schedule-modal,
.schedule-modal-leave-active .schedule-modal {
  transition: transform 0.2s ease;
}
.schedule-modal-enter-from,
.schedule-modal-leave-to {
  opacity: 0;
}
.schedule-modal-enter-from .schedule-modal,
.schedule-modal-leave-to .schedule-modal {
  transform: scale(0.96);
}

.error {
  color: #b91c1c;
  font-size: 0.9rem;
  margin: 0.5rem 0;
}
.muted {
  color: #64748b;
  font-size: 0.9rem;
}
</style>
