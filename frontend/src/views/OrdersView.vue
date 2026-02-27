<template>
  <section class="panel">
    <h2>订单</h2>
    <OrderForm :loading="loading" :reset-trigger="formResetTrigger" @submit="createOrder" />
    <div class="table-actions">
      <button class="btn" :disabled="loading" @click="load">刷新订单列表</button>
    </div>
    <p v-if="error" class="error">{{ error }}</p>
    <OrderTable :orders="orders" :loading="loading" @refund="refundOrder" />
  </section>
</template>

<script setup>
import { ref, watch, onMounted } from 'vue'
import * as api from '../api/orders.js'
import OrderForm from '../components/order/OrderForm.vue'
import OrderTable from '../components/order/OrderTable.vue'

const props = defineProps({ userId: { type: String, default: '' } })

const orders = ref([])
const loading = ref(false)
const error = ref('')
const formResetTrigger = ref(0)

async function load() {
  if (!props.userId.trim()) {
    error.value = '请先输入 userId'
    return
  }
  error.value = ''
  loading.value = true
  try {
    const res = await api.list(props.userId, 0, 50)
    orders.value = res?.items ?? []
  } catch (e) {
    error.value = e.message
    orders.value = []
  } finally {
    loading.value = false
  }
}

async function createOrder(body) {
  if (!body.orderNo?.trim() || !body.cardNo?.trim() || !body.amount || !props.userId.trim()) {
    error.value = '订单号、卡号、金额、userId 必填'
    return
  }
  error.value = ''
  loading.value = true
  try {
    await api.create({
      ...body,
      userId: props.userId.trim(),
      orderNo: body.orderNo.trim(),
      cardNo: body.cardNo.trim(),
      tradeTime: new Date().toISOString(),
      accountTime: new Date().toISOString(),
    })
    formResetTrigger.value++
    await load()
  } catch (e) {
    error.value = e.message
  } finally {
    loading.value = false
  }
}

async function refundOrder(id) {
  error.value = ''
  loading.value = true
  try {
    await api.refund(id)
    await load()
  } catch (e) {
    error.value = e.message
  } finally {
    loading.value = false
  }
}

watch(() => props.userId, () => { orders.value = [] })
onMounted(() => { if (props.userId) load() })
</script>

<style scoped>
.panel h2 {
  font-size: 1.15rem;
  margin: 0 0 1rem 0;
}

.table-actions {
  margin-bottom: 0.75rem;
}
</style>
