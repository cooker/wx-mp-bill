export function formatTime(iso) {
  if (!iso) return '-'
  try {
    const d = new Date(iso)
    return d.toLocaleString('zh-CN')
  } catch {
    return iso
  }
}

export function formatAmount(v) {
  if (v == null || v === '') return '-'
  const n = Number(v)
  if (Number.isNaN(n)) return String(v)
  return n.toFixed(2)
}

export function formatScheduleAmount(v) {
  if (v == null || v === '') return '0.00'
  const n = Number(v)
  return Number.isNaN(n) ? '0.00' : n.toFixed(2)
}

export function schedulePeriodTotal(s) {
  const a = Number(s?.amount) || 0
  const i = Number(s?.interest) || 0
  const f = Number(s?.serviceFee) || 0
  const o = Number(s?.overdueInterest) || 0
  return a + i + f + o
}
