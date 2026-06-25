const BASE = '/api/v1'

async function request<T>(url: string, init?: RequestInit): Promise<T> {
  const res = await fetch(`${BASE}${url}`, {
    headers: { 'Content-Type': 'application/json', ...init?.headers },
    ...init,
  })
  if (!res.ok) throw new Error(`${res.status} ${res.statusText}`)
  return res.json()
}

/** Spring Boot Page<T> wrapper — extracts content array */
async function requestPage<T>(url: string): Promise<T[]> {
  const page = await request<{ content: T[] }>(url)
  return page.content
}

export const api = {
  readers: {
    list: () => request<import('../types/api').Reader[]>('/readers'),
    create: (data: Partial<import('../types/api').Reader>) =>
      request<import('../types/api').Reader>('/readers', { method: 'POST', body: JSON.stringify(data) }),
    update: (id: number, data: Partial<import('../types/api').Reader>) =>
      request<import('../types/api').Reader>(`/readers/${id}`, { method: 'PUT', body: JSON.stringify(data) }),
    setStatus: (id: number, active: boolean) =>
      request<void>(`/readers/${id}/status`, { method: 'PATCH', body: JSON.stringify({ active }) }),
  },
  members: {
    list: () => requestPage<import('../types/api').Member>('/members'),
    create: (data: Partial<import('../types/api').Member>) =>
      request<import('../types/api').Member>('/members', { method: 'POST', body: JSON.stringify(data) }),
    deactivate: (id: number) =>
      request<void>(`/members/${id}/deactivate`, { method: 'PATCH' }),
    activate: (id: number) =>
      request<void>(`/members/${id}/activate`, { method: 'PATCH' }),
  },
  vehicles: {
    list: () => request<import('../types/api').Vehicle[]>('/vehicles'),
    listByMember: (memberId: number) =>
      request<import('../types/api').Vehicle[]>(`/vehicles/by-member/${memberId}`),
    create: (data: Partial<import('../types/api').Vehicle>) =>
      request<import('../types/api').Vehicle>('/vehicles', { method: 'POST', body: JSON.stringify(data) }),
    linkToMember: (vehicleId: number, memberId: number) =>
      request<import('../types/api').Vehicle>(`/vehicles/${vehicleId}/link/${memberId}`, { method: 'PUT' }),
  },
  tags: {
    getByEpc: (epc: string) =>
      request<import('../types/api').RfidTag>(`/tags/${epc}`),
    getByMember: (memberId: number) =>
      request<import('../types/api').RfidTag | null>(`/tags/by-member/${memberId}`)
        .catch(() => null),
    getByVehicle: (vehicleId: number) =>
      request<import('../types/api').RfidTag | null>(`/tags/by-vehicle/${vehicleId}`)
        .catch(() => null),
    assign: (epc: string, entityType: string, entityId: string) =>
      request<import('../types/api').RfidTag>('/tags/assign', {
        method: 'POST',
        body: JSON.stringify({ epc, entityType, entityId }),
      }),
    unassign: (epc: string) =>
      request<void>('/tags/unassign', {
        method: 'POST',
        body: JSON.stringify({ epc }),
      }),
  },
  logs: {
    list: () => requestPage<import('../types/api').AccessLog>('/access-logs'),
  },
}
