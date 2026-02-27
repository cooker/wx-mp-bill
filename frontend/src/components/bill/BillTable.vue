<template>
  <div class="table-wrap bills-table-wrap">
    <table v-if="bills.length" class="bills-table">
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
          <th>逾期利息</th>
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
            <template v-if="b.billType === 'INSTALLMENT' && b.installmentCount">
              {{ b.installmentCount }} 期<template v-if="b.installmentDaysPerPeriod"> / {{ b.installmentDaysPerPeriod }} 天</template>
            </template>
            <template v-else-if="(b.status === 'UNPAID' || b.status === 'OVERDUE') && b.billType !== 'INSTALLMENT'">
              <div class="installment-inline">
                <select v-model="installmentProductIds[b.id]" class="input-sm">
                  <option value="">选择分期产品</option>
                  <option
                    v-for="p in installmentProducts"
                    :key="p.id"
                    :value="p.id"
                  >
                    {{ p.name }}(#{{ p.id }})<template v-if="p.installmentPeriodCount"> {{ p.installmentPeriodCount }}期</template><template v-if="p.installmentDaysPerPeriod">/{{ p.installmentDaysPerPeriod }}天</template>
                  </option>
                </select>
                <button
                  class="btn small"
                  :disabled="loading || !installmentProductIds[b.id]"
                  @click="$emit('set-installment', b.id, installmentProductIds[b.id])"
                >
                  分期
                </button>
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
              class="status-badge unpaid"
            >待还</span>
            <span
              v-else-if="b.status === 'OVERDUE'"
              class="status-badge overdue"
            >逾期</span>
            <span v-else class="status-badge paid">已结清</span>
          </td>
          <td>
            <input
              :id="'due-input-' + b.id"
              type="date"
              :value="b.dueDate"
              class="input-sm"
              style="width: 100%"
            />
          </td>
          <td class="cell-buttons">
            <button
              v-if="b.billType === 'INSTALLMENT' && b.installmentSchedule?.length"
              class="btn small"
              @click="$emit('show-schedule', b)"
            >
              查看计划
            </button>
            <button
              class="btn small"
              :disabled="loading"
              @click="$emit('save-due-date', b)"
            >
              保存还款日
            </button>
          </td>
          <td class="cell-repay">
            <template v-if="b.status === 'UNPAID' || b.status === 'OVERDUE'">
              <input
                v-model="repayAmounts[b.id]"
                type="number"
                step="0.01"
                placeholder="金额"
                class="input-sm repay-amount"
              />
              <button
                class="btn small primary"
                :disabled="loading"
                @click="$emit('repay', b.id, repayAmounts[b.id])"
              >
                还款
              </button>
            </template>
            <span v-else class="muted">已结清</span>
          </td>
        </tr>
      </tbody>
    </table>
    <p v-else class="muted">暂无账单数据（先查本期应还或历史）</p>
  </div>
</template>

<script setup>
import { formatAmount, formatScheduleAmount } from '../../utils/format.js'

defineProps({
  bills: { type: Array, default: () => [] },
  installmentProducts: { type: Array, default: () => [] },
  loading: { type: Boolean, default: false },
  repayAmounts: { type: Object, default: () => ({}) },
  installmentProductIds: { type: Object, default: () => ({}) },
})
defineEmits(['repay', 'set-installment', 'save-due-date', 'show-schedule'])
</script>

<style scoped>
.bills-table-wrap {
  min-width: 980px;
}

.bills-table {
  table-layout: fixed;
}

.bills-table .amount {
  text-align: right;
  font-variant-numeric: tabular-nums;
}

.bills-table .cell-type {
  overflow: hidden;
  max-width: 200px;
}

.installment-inline {
  display: flex;
  flex-wrap: wrap;
  gap: 0.35rem;
  align-items: center;
}

.input-sm {
  width: 90px;
  padding: 0.25rem 0.4rem;
  margin-right: 0.25rem;
  border: 1px solid var(--color-border);
  border-radius: 4px;
  height: 1.85rem;
  box-sizing: border-box;
}

.repay-amount {
  width: 5rem;
  min-width: 5rem;
}

.cell-buttons .btn,
.cell-repay .btn {
  margin: 0 0.25rem 0 0;
}

.cell-repay .input-sm {
  margin-right: 0.25rem;
}

.status-badge {
  display: inline-block;
  padding: 0.2rem 0.5rem;
  border-radius: 4px;
  font-size: 0.8rem;
  font-weight: 500;
}

.status-badge.unpaid {
  background: #fef3c7;
  color: #92400e;
}

.status-badge.overdue {
  background: #fee2e2;
  color: #b91c1c;
}

.status-badge.paid {
  color: var(--color-success);
}
</style>
