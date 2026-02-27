import { request } from './request.js'

export function list(userId, offset = 0, limit = 20) {
  return request(
    `/api/orders?userId=${encodeURIComponent(userId)}&offset=${offset}&limit=${limit}`
  )
}

export function get(id) {
  return request(`/api/orders/${id}`)
}

export function create(body) {
  return request('/api/orders', { method: 'POST', body: JSON.stringify(body) })
}

export function refund(id) {
  return request(`/api/orders/${id}/refund`, { method: 'POST' })
}
