<template>
  <div class="table-wrap">
    <table v-if="orders.length">
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
              :disabled="loading"
              @click="$emit('refund', o.id)"
            >
              退款
            </button>
            <span v-else-if="o.orderType === 'NORMAL' && o.refunded" class="muted">已退款</span>
            <span v-else class="muted">退款单</span>
          </td>
        </tr>
      </tbody>
    </table>
    <p v-else class="muted">暂无订单（请先选择/输入 userId 并创建订单）</p>
  </div>
</template>

<script setup>
import { formatTime } from '../../utils/format.js'

defineProps({
  orders: { type: Array, default: () => [] },
  loading: { type: Boolean, default: false },
})
defineEmits(['refund'])
</script>
