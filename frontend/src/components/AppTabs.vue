<template>
  <nav class="tabs">
    <button
      v-for="t in tabs"
      :key="t.id"
      :class="{ active: current === t.id }"
      @click="$emit('update:current', t.id)"
    >
      {{ t.label }}
    </button>
  </nav>
</template>

<script setup>
defineProps({
  current: { type: String, required: true },
  tabs: {
    type: Array,
    default: () => [
      { id: 'products', label: '产品' },
      { id: 'orders', label: '订单' },
      { id: 'bills', label: '账单' },
    ],
  },
})
defineEmits(['update:current'])
</script>

<style scoped>
.tabs {
  display: flex;
  gap: 0.25rem;
  margin-bottom: 1.25rem;
}

.tabs button {
  padding: 0.5rem 1rem;
  border: 1px solid var(--color-border);
  background: var(--color-surface);
  border-radius: var(--radius);
  font-size: 0.95rem;
}

.tabs button.active {
  background: var(--color-primary);
  color: #fff;
  border-color: var(--color-primary);
}
</style>
