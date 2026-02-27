import { request } from './request.js'

export function list() {
  return request('/api/products')
}

export function get(id) {
  return request(`/api/products/${id}`)
}

export function create(body) {
  return request('/api/products', { method: 'POST', body: JSON.stringify(body) })
}

export function update(id, body) {
  return request(`/api/products/${id}/update`, { method: 'POST', body: JSON.stringify(body) })
}
