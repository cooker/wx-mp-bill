<template>
  <section class="panel">
    <h2>产品配置</h2>
    <ProductForm
      :editing-product="editingProduct"
      :loading="loading"
      @submit="onSubmit"
      @cancel="editingProduct = null"
    />
    <div class="table-actions">
      <button class="btn" :disabled="loading" @click="load">刷新列表</button>
    </div>
    <p v-if="error" class="error">{{ error }}</p>
    <ProductTable :products="products" :loading="loading" @edit="editingProduct = $event" />
  </section>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import * as api from '../api/products.js'
import ProductForm from '../components/product/ProductForm.vue'
import ProductTable from '../components/product/ProductTable.vue'

const products = ref([])
const loading = ref(false)
const error = ref('')
const editingProduct = ref(null)

async function load() {
  error.value = ''
  loading.value = true
  try {
    const res = await api.list()
    products.value = res ?? []
  } catch (e) {
    error.value = e.message
    products.value = []
  } finally {
    loading.value = false
  }
}

async function onSubmit(body) {
  error.value = ''
  loading.value = true
  try {
    if (editingProduct.value?.id) {
      await api.update(editingProduct.value.id, body)
    } else {
      await api.create(body)
    }
    editingProduct.value = null
    await load()
  } catch (e) {
    error.value = e.message
  } finally {
    loading.value = false
  }
}

onMounted(load)
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
