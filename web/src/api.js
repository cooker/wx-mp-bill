const BASE = ''

async function request(path, options = {}) {
  const res = await fetch(BASE + path, {
    headers: { 'Content-Type': 'application/json', ...options.headers },
    ...options,
  })
  if (!res.ok) {
    const text = await res.text()
    throw new Error(text || `HTTP ${res.status}`)
  }
  const text = await res.text()
  if (res.status === 204 || !text || !text.trim()) return null
  return JSON.parse(text)
}

export const api = {
  products: {
    list: () => request('/api/products'),
    get: (id) => request(`/api/products/${id}`),
    create: (body) => request('/api/products', { method: 'POST', body: JSON.stringify(body) }),
    update: (id, body) => request(`/api/products/${id}/update`, { method: 'POST', body: JSON.stringify(body) }),
  },
  orders: {
    list: (userId, offset = 0, limit = 20) =>
      request(`/api/orders?userId=${encodeURIComponent(userId)}&offset=${offset}&limit=${limit}`),
    get: (id) => request(`/api/orders/${id}`),
    create: (body) => request('/api/orders', { method: 'POST', body: JSON.stringify(body) }),
    refund: (id) => request(`/api/orders/${id}/refund`, { method: 'POST' }),
  },
  bills: {
    get: (id) => request(`/api/bills/${id}`),
    current: (userId, from, to) =>
      request(`/api/bills/current?userId=${encodeURIComponent(userId)}&from=${from}&to=${to}`),
    history: (userId, status, offset = 0, limit = 20) => {
      let path = `/api/bills/history?userId=${encodeURIComponent(userId)}&offset=${offset}&limit=${limit}`
      if (status) path += `&status=${status}`
      return request(path)
    },
    repay: (id, amount) =>
      request(`/api/bills/${id}/repay`, { method: 'POST', body: JSON.stringify({ amount }) }),
    setInstallment: (id, body) =>
      request(`/api/bills/${id}/update`, { method: 'POST', body: JSON.stringify(body) }),
    updateDueDate: (id, dueDate) =>
      request(`/api/bills/${id}/due-date`, { method: 'POST', body: JSON.stringify({ dueDate }) }),
    previewInstallment: (id, body) =>
      request(`/api/bills/${id}/installment-preview`, { method: 'POST', body: JSON.stringify(body) }),
  },
}
