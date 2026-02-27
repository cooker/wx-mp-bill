<template>
  <Teleport to="body">
    <Transition name="modal">
      <div v-if="bill" class="overlay" @click.self="$emit('close')">
        <div class="modal">
          <div class="modal-header">
            <h3>还款计划 · 账单 #{{ bill.id }}</h3>
            <button type="button" class="close" aria-label="关闭" @click="$emit('close')">×</button>
          </div>
          <div class="summary">
            <span>账单月 {{ bill.billMonth }}</span>
            <span>共 {{ bill.installmentCount }} 期</span>
            <span>应还总额 <strong>¥{{ formatAmount(bill.totalDueAmount ?? bill.totalAmount) }}</strong></span>
            <span>已还 <strong>¥{{ bill.paidAmount }}</strong></span>
          </div>
          <div class="table-wrap">
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
                <tr
                  v-for="s in bill.installmentSchedule"
                  :key="s.periodNo"
                  :class="{ 'row-paid': s.status === 'PAID' }"
                >
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
                    <span :class="['badge', s.status === 'PAID' ? 'paid' : 'pending']">
                      {{ s.status === 'PAID' ? '已还' : '待还' }}
                    </span>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
          <div class="modal-footer">
            <button class="btn primary" @click="$emit('close')">关闭</button>
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup>
import { formatTime, formatAmount, formatScheduleAmount, schedulePeriodTotal } from '../../utils/format.js'

defineProps({ bill: { type: Object, default: null } })
defineEmits(['close'])
</script>

<style scoped>
.overlay {
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

.modal {
  background: var(--color-surface);
  border-radius: 12px;
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.25);
  max-width: 90vw;
  width: 800px;
  max-height: calc(100vh - 2rem);
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 1.25rem 1.5rem;
  border-bottom: 1px solid var(--color-border);
  background: #f8fafc;
}

.modal-header h3 {
  margin: 0;
  font-size: 1.2rem;
  font-weight: 600;
  color: var(--color-primary);
}

.close {
  width: 32px;
  height: 32px;
  border: none;
  background: transparent;
  color: var(--color-text-muted);
  font-size: 1.5rem;
  line-height: 1;
  border-radius: var(--radius);
  display: flex;
  align-items: center;
  justify-content: center;
}

.close:hover {
  color: var(--color-primary);
  background: var(--color-border);
}

.summary {
  display: flex;
  flex-wrap: wrap;
  gap: 0.75rem 1.5rem;
  padding: 1rem 1.5rem;
  font-size: 0.9rem;
  color: var(--color-text-muted);
  border-bottom: 1px solid #f1f5f9;
}

.summary strong {
  color: var(--color-primary);
}

.table-wrap {
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
  border-bottom: 1px solid #f1f5f9;
}

.schedule-table th {
  font-weight: 600;
  color: var(--color-text-muted);
  background: #f8fafc;
}

.schedule-table td.amount {
  font-variant-numeric: tabular-nums;
  font-weight: 500;
}

.schedule-table tr.row-paid {
  background: var(--color-success-bg);
}

.schedule-table tr.row-paid td {
  color: #166534;
}

.badge {
  display: inline-block;
  padding: 0.2rem 0.5rem;
  border-radius: 999px;
  font-size: 0.75rem;
  font-weight: 500;
}

.badge.paid {
  background: #dcfce7;
  color: #166534;
}

.badge.pending {
  background: var(--color-warning-bg);
  color: var(--color-warning);
}

.modal-footer {
  padding: 1.25rem 1.5rem;
  border-top: 1px solid var(--color-border);
  background: #f8fafc;
}

.modal-enter-active,
.modal-leave-active {
  transition: opacity 0.2s ease;
}

.modal-enter-active .modal,
.modal-leave-active .modal {
  transition: transform 0.2s ease;
}

.modal-enter-from,
.modal-leave-to {
  opacity: 0;
}

.modal-enter-from .modal,
.modal-leave-to .modal {
  transform: scale(0.96);
}
</style>
