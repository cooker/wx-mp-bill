<template>
  <div class="form-card">
    <h3>{{ editingId ? '编辑产品' : '新增产品' }}</h3>
    <div class="form-row">
      <input v-model="form.name" placeholder="产品名称 *" />
      <input v-model="form.overdueDailyRate" type="number" step="0.000001" placeholder="逾期日利率，如 0.0005" />
      <select v-model="form.billType">
        <option value="NORMAL">正常账单</option>
        <option value="INSTALLMENT">分期账单</option>
      </select>
    </div>
    <div v-if="form.billType === 'INSTALLMENT'" class="form-row form-row-installment">
      <span class="form-label">分期配置</span>
      <input v-model="form.installmentPeriodCount" type="number" min="1" placeholder="分期期数" />
      <input v-model="form.installmentDaysPerPeriod" type="number" min="1" placeholder="每期天数" />
      <input v-model="form.installmentDailyRate" type="number" step="0.000001" placeholder="分期日利率" />
      <input v-model="form.installmentOverdueDailyRate" type="number" step="0.000001" placeholder="分期逾期日利率" />
      <input v-model="form.installmentPerPeriodFeeRate" type="number" step="0.000001" placeholder="每期服务费本金利率" />
    </div>
    <button class="btn primary" :disabled="loading" @click="submit">{{ editingId ? '保存' : '新增' }}</button>
    <button v-if="editingId" class="btn" @click="cancel">取消</button>
  </div>
</template>

<script setup>
import { reactive, watch, computed } from 'vue'

const props = defineProps({
  editingProduct: { type: Object, default: null },
  loading: { type: Boolean, default: false },
})
const emit = defineEmits(['submit', 'cancel'])

const form = reactive({
  name: '',
  overdueDailyRate: '',
  billType: 'NORMAL',
  installmentDailyRate: '',
  installmentOverdueDailyRate: '',
  installmentPerPeriodFeeRate: '',
  installmentPeriodCount: '',
  installmentDaysPerPeriod: '',
})

const editingId = computed(() => props.editingProduct?.id)

watch(
  () => props.editingProduct,
  (p) => {
    if (!p) {
      form.name = ''
      form.overdueDailyRate = ''
      form.billType = 'NORMAL'
      form.installmentDailyRate = ''
      form.installmentOverdueDailyRate = ''
      form.installmentPerPeriodFeeRate = ''
      form.installmentPeriodCount = ''
      form.installmentDaysPerPeriod = ''
    } else {
      form.name = p.name ?? ''
      form.overdueDailyRate = p.overdueDailyRate != null ? String(p.overdueDailyRate) : ''
      form.billType = p.billType ?? 'NORMAL'
      form.installmentDailyRate = p.installmentDailyRate != null ? String(p.installmentDailyRate) : ''
      form.installmentOverdueDailyRate = p.installmentOverdueDailyRate != null ? String(p.installmentOverdueDailyRate) : ''
      form.installmentPerPeriodFeeRate = p.installmentPerPeriodFeeRate != null ? String(p.installmentPerPeriodFeeRate) : ''
      form.installmentPeriodCount = p.installmentPeriodCount != null ? String(p.installmentPeriodCount) : ''
      form.installmentDaysPerPeriod = p.installmentDaysPerPeriod != null ? String(p.installmentDaysPerPeriod) : ''
    }
  },
  { immediate: true }
)

function buildBody() {
  const body = { name: form.name.trim(), billType: form.billType }
  if (form.overdueDailyRate !== '' && form.overdueDailyRate != null) body.overdueDailyRate = Number(form.overdueDailyRate)
  if (form.installmentDailyRate !== '' && form.installmentDailyRate != null) body.installmentDailyRate = Number(form.installmentDailyRate)
  if (form.installmentOverdueDailyRate !== '' && form.installmentOverdueDailyRate != null) body.installmentOverdueDailyRate = Number(form.installmentOverdueDailyRate)
  if (form.installmentPerPeriodFeeRate !== '' && form.installmentPerPeriodFeeRate != null) body.installmentPerPeriodFeeRate = Number(form.installmentPerPeriodFeeRate)
  if (form.installmentPeriodCount !== '' && form.installmentPeriodCount != null) body.installmentPeriodCount = Number(form.installmentPeriodCount)
  if (form.installmentDaysPerPeriod !== '' && form.installmentDaysPerPeriod != null) body.installmentDaysPerPeriod = Number(form.installmentDaysPerPeriod)
  return body
}

function submit() {
  if (!form.name?.trim()) return
  emit('submit', buildBody())
}
function cancel() {
  emit('cancel')
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

.form-row-installment {
  align-items: center;
}
</style>
