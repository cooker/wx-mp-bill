<template>
  <section class="panel">
    <h2>账单</h2>
    <div class="bill-actions">
      <button class="btn" :disabled="loading" @click="loadCurrent">本期应还</button>
      <button class="btn" :disabled="loading" @click="loadHistory">历史账单</button>
    </div>
    <p v-if="error" class="error">{{ error }}</p>
    <BillTable
      :bills="bills"
      :installment-products="installmentProducts"
      :loading="loading"
      :repay-amounts="repayAmounts"
      :installment-product-ids="installmentProductIds"
      @repay="repayBill"
      @set-installment="setInstallment"
      @save-due-date="saveDueDate"
      @show-schedule="onShowSchedule"
    />
    <ScheduleModal :bill="scheduleBill" @close="scheduleBill = null" />
  </section>
</template>

<script setup>
import { ref, reactive, watch, onMounted } from 'vue'
import * as billsApi from '../api/bills.js'
import * as productsApi from '../api/products.js'
import BillTable from '../components/bill/BillTable.vue'
import ScheduleModal from '../components/bill/ScheduleModal.vue'

const props = defineProps({ userId: { type: String, default: '' } })

const bills = ref([])
const installmentProducts = ref([])
const loading = ref(false)
const error = ref('')
const scheduleBill = ref(null)
const repayAmounts = reactive({})
const installmentProductIds = reactive({})

function dateRange() {
  const now = new Date()
  const from = new Date(now.getFullYear(), now.getMonth(), 1)
  const to = new Date(now.getFullYear(), now.getMonth() + 2, 0)
  return { from: from.toISOString().slice(0, 10), to: to.toISOString().slice(0, 10) }
}

async function loadCurrent() {
  if (!props.userId.trim()) {
    error.value = '请先输入 userId'
    return
  }
  error.value = ''
  loading.value = true
  try {
    const { from, to } = dateRange()
    const res = await billsApi.current(props.userId, from, to)
    bills.value = res ?? []
  } catch (e) {
    error.value = e.message
    bills.value = []
  } finally {
    loading.value = false
  }
}

async function loadHistory() {
  if (!props.userId.trim()) {
    error.value = '请先输入 userId'
    return
  }
  error.value = ''
  loading.value = true
  try {
    const res = await billsApi.history(props.userId, null, 0, 50)
    bills.value = res?.items ?? []
  } catch (e) {
    error.value = e.message
    bills.value = []
  } finally {
    loading.value = false
  }
}

async function loadProducts() {
  try {
    const res = await productsApi.list()
    installmentProducts.value = (res ?? []).filter((p) => p.billType === 'INSTALLMENT')
  } catch {
    installmentProducts.value = []
  }
}

async function repayBill(id, amount) {
  const n = Number(amount)
  if (!Number.isFinite(n) || n <= 0) {
    error.value = '请输入有效还款金额'
    return
  }
  error.value = ''
  loading.value = true
  try {
    await billsApi.repay(id, n)
    repayAmounts[id] = ''
    await loadCurrent()
  } catch (e) {
    error.value = e.message
  } finally {
    loading.value = false
  }
}

async function setInstallment(billId, productId) {
  if (!productId) return
  error.value = ''
  loading.value = true
  try {
    await billsApi.setInstallment(billId, { billType: 'INSTALLMENT', productId: Number(productId) })
    installmentProductIds[billId] = ''
    await loadCurrent()
  } catch (e) {
    error.value = e.message
  } finally {
    loading.value = false
  }
}

async function onShowSchedule(bill) {
  scheduleBill.value = bill
  if (bill?.id) {
    try {
      const fresh = await billsApi.get(bill.id)
      scheduleBill.value = fresh ?? bill
    } catch {
      scheduleBill.value = bill
    }
  }
}

async function saveDueDate(bill) {
  if (!bill?.id) return
  const el = document.getElementById('due-input-' + bill.id)
  const value = el?.value
  if (!value) return
  error.value = ''
  loading.value = true
  try {
    await billsApi.updateDueDate(bill.id, value)
    await loadCurrent()
  } catch (e) {
    error.value = e.message
  } finally {
    loading.value = false
  }
}

watch(() => props.userId, () => { bills.value = [] })
onMounted(loadProducts)
</script>

<style scoped>
.panel h2 {
  font-size: 1.15rem;
  margin: 0 0 1rem 0;
}

.bill-actions {
  margin-bottom: 0.75rem;
}
</style>
