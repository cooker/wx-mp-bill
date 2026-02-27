<template>
  <div class="app">
    <AppHeader v-model="userId" />
    <AppTabs v-model:current="tab" />
    <main class="main">
      <ProductsView v-show="tab === 'products'" />
      <OrdersView v-show="tab === 'orders'" :user-id="userId" />
      <BillsView v-show="tab === 'bills'" :user-id="userId" />
    </main>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import AppHeader from './components/AppHeader.vue'
import AppTabs from './components/AppTabs.vue'
import ProductsView from './views/ProductsView.vue'
import OrdersView from './views/OrdersView.vue'
import BillsView from './views/BillsView.vue'

const tab = ref('products')
const userId = ref('u1')
</script>

<style scoped>
.app {
  max-width: 1000px;
  margin: 0 auto;
  padding: 1rem 1.5rem;
}

.main {
  background: var(--color-surface);
  border-radius: var(--radius-lg);
  padding: 1.25rem;
  box-shadow: var(--shadow);
}
</style>
