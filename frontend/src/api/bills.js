import { request } from './request.js'

export function get(id) {
  return request(`/api/bills/${id}`)
}

export function current(userId, from, to) {
  return request(
    `/api/bills/current?userId=${encodeURIComponent(userId)}&from=${from}&to=${to}`
  )
}

export function history(userId, status, offset = 0, limit = 20) {
  let path = `/api/bills/history?userId=${encodeURIComponent(userId)}&offset=${offset}&limit=${limit}`
  if (status) path += `&status=${status}`
  return request(path)
}

export function repay(id, amount) {
  return request(`/api/bills/${id}/repay`, {
    method: 'POST',
    body: JSON.stringify({ amount }),
  })
}

export function setInstallment(id, body) {
  return request(`/api/bills/${id}/update`, {
    method: 'POST',
    body: JSON.stringify(body),
  })
}

export function updateDueDate(id, dueDate) {
  return request(`/api/bills/${id}/due-date`, {
    method: 'POST',
    body: JSON.stringify({ dueDate }),
  })
}

export function previewInstallment(id, body) {
  return request(`/api/bills/${id}/installment-preview`, {
    method: 'POST',
    body: JSON.stringify(body),
  })
}
