import { useEffect, useRef, useState } from 'react'
import StatusBadge from '../shared/StatusBadge'
import type { AccessEvent } from '../../types/api'

type Filter = 'ALL' | 'GRANTED' | 'DENIED'

interface Props {
  events: AccessEvent[]
}

export default function AccessFeed({ events }: Props) {
  const [filter, setFilter] = useState<Filter>('ALL')
  const bottomRef = useRef<HTMLDivElement>(null)

  const filtered = filter === 'ALL' ? events : events.filter(e => e.decision === filter)

  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: 'smooth' })
  }, [filtered.length])

  const btn = (f: Filter) =>
    `px-3 py-1 text-xs font-medium rounded-full transition-colors ${
      filter === f
        ? f === 'ALL' ? 'bg-gray-700 text-white'
          : f === 'GRANTED' ? 'bg-green-600 text-white'
          : 'bg-red-600 text-white'
        : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
    }`

  return (
    <div>
      <div className="flex gap-2 mb-3">
        <button className={btn('ALL')} onClick={() => setFilter('ALL')}>Todas</button>
        <button className={btn('GRANTED')} onClick={() => setFilter('GRANTED')}>Autorizadas</button>
        <button className={btn('DENIED')} onClick={() => setFilter('DENIED')}>Denegadas</button>
        {filter !== 'ALL' && (
          <span className="text-xs text-gray-400 ml-auto self-center">
            {filtered.length} de {events.length}
          </span>
        )}
      </div>

      {filtered.length === 0 ? (
        <div className="text-center text-gray-400 py-12">
          {events.length === 0 ? 'Esperando lecturas...' : filter === 'GRANTED' ? 'Sin autorizaciones' : 'Sin denegaciones'}
        </div>
      ) : (
        <div className="space-y-2 max-h-[600px] overflow-y-auto">
          {filtered.map((e) => (
            <div
              key={e.eventId}
              className={`flex items-start gap-3 p-3 rounded-lg border ${
                e.decision === 'GRANTED'
                  ? 'bg-green-50 border-green-200'
                  : 'bg-red-50 border-red-200'
              }`}
            >
              <div className={`w-2 h-2 rounded-full mt-1.5 ${
                e.decision === 'GRANTED' ? 'bg-green-500' : 'bg-red-500'
              }`} />
              <div className="flex-1 min-w-0">
                <div className="flex items-center gap-2">
                  <p className="text-sm font-medium text-gray-900 truncate">
                    {e.epc}
                  </p>
                  <span className="text-xs text-gray-400 font-mono">
                    #{e.readerId}{e.antennaName ? ` · ${e.antennaName}` : ` · Antena ${e.antenna}`}
                  </span>
                </div>
                {e.memberName && (
                  <p className="text-xs text-gray-600 mt-1">
                    {e.memberName}
                    {e.vehiclePlate && (
                      <span className="ml-1">
                        · {e.vehicleBrand} · {e.vehicleModel} · {e.vehiclePlate}
                        {e.vehicleColor && (
                          <span className="ml-1 text-gray-400">
                            · {e.vehicleColor}
                          </span>
                        )}
                      </span>
                    )}
                  </p>
                )}
                {e.reason && (
                  <p className="text-xs text-gray-400 mt-0.5">{e.reason}</p>
                )}
              </div>
              <StatusBadge status={e.decision} variant="decision" />
            </div>
          ))}
          <div ref={bottomRef} />
        </div>
      )}
    </div>
  )
}
