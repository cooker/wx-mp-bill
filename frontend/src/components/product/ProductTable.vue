<template>
  <div class="table-wrap">
    <table v-if="products.length">
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
            <button class="btn small" :disabled="loading" @click="$emit('edit', p)">编辑</button>
          </td>
        </tr>
      </tbody>
    </table>
    <p v-else class="muted">暂无数据，请新增产品</p>
  </div>
</template>

<script setup>
defineProps({
  products: { type: Array, default: () => [] },
  loading: { type: Boolean, default: false },
})
defineEmits(['edit'])
</script>
