<template>
  <header class="app-header">
    <h1>账单系统 · 本地调试</h1>
    <div class="user-bar">
      <label>当前用户</label>
      <input v-model="userId" type="text" placeholder="输入 userId 如 u1" />
      <span class="hint">订单/账单列表均按此用户查询</span>
    </div>
  </header>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({ modelValue: { type: String, default: 'u1' } })
const emit = defineEmits(['update:modelValue'])

const userId = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
})
</script>

<style scoped>
.app-header {
  margin-bottom: 1.5rem;
}

.app-header h1 {
  font-size: 1.5rem;
  font-weight: 600;
  margin: 0 0 0.75rem 0;
  color: var(--color-primary);
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
  border: 1px solid var(--color-border-strong);
  border-radius: var(--radius);
}

.user-bar .hint {
  font-size: 0.85rem;
  color: var(--color-text-muted);
}
</style>
