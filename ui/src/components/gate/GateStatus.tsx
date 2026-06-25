import { useEffect, useState } from 'react'
import { api } from '../../services/api'
import type { Reader } from '../../types/api'
import StatusBadge from '../shared/StatusBadge'

export default function GateStatus() {
  const [readers, setReaders] = useState<Reader[]>([])

  useEffect(() => {
    api.readers.list().then(setReaders).catch(() => {})
  }, [])

  return (
    <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
      {readers.map(r => (
        <div key={r.id} className="bg-white rounded-lg border border-gray-200 p-4">
          <div className="flex items-center justify-between mb-2">
            <h3 className="font-medium text-gray-900">{r.name}</h3>
            <StatusBadge status={r.active ? 'online' : 'offline'} variant={r.active ? 'active' : 'default'} />
          </div>
          <p className="text-xs text-gray-500">{r.location}</p>
          <p className="text-xs text-gray-400 font-mono mt-1">{r.ipAddress}</p>
        </div>
      ))}
      {readers.length === 0 && (
        <p className="text-gray-400 text-sm col-span-full">Sin lectores registrados</p>
      )}
    </div>
  )
}
