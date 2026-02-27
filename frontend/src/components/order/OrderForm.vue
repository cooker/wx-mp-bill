<template>
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
        <input v-model.number="form.installmentDaysPerPeriod" type="number" min="1" placeholder="每期天数" />
      </template>
    </div>
    <button class="btn primary" :disabled="loading" @click="submit">创建</button>
  </div>
</template>

<script setup>
import { reactive, watch } from 'vue'

const props = defineProps({
  loading: { type: Boolean, default: false },
  resetTrigger: { type: Number, default: 0 },
})
const emit = defineEmits(['submit'])

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

function reset() {
  form.orderNo = ''
  form.merchantOrderNo = ''
  form.amount = ''
}
watch(() => props.resetTrigger, (n) => { if (n > 0) reset() })

function submit() {
  emit('submit', {
    orderNo: form.orderNo.trim(),
    merchantOrderNo: form.merchantOrderNo?.trim() || null,
    cardNo: form.cardNo.trim(),
    tradeCompany: form.tradeCompany?.trim() || null,
    amount: Number(form.amount),
    tradeChannel: form.tradeChannel?.trim() || null,
    tradeType: form.tradeType?.trim() || null,
    billType: form.billType,
    installmentCount: form.billType === 'INSTALLMENT' ? form.installmentCount : undefined,
    installmentDaysPerPeriod: form.billType === 'INSTALLMENT' && form.installmentDaysPerPeriod > 0 ? form.installmentDaysPerPeriod : undefined,
  })
}
</script>

<style scoped>
.form-card {
  background: #f8fafc;
  padding: 1rem;
  border-radius: 8px;
  margin-bottom: 1rem;
}

.form-card h3 {
  font-size: 1rem;
  margin: 0 0 0.75rem 0;
}
</style>
