import { useEffect, useRef, useState } from 'react'
import StatusBadge from '../shared/StatusBadge'
import type { AccessEvent } from '../../types/api'

type Filter = 'ALL' | 'GRANTED' | 'DENIED'

interface Props {
  events: AccessEvent[]
}

function formatTime(iso: string) {
  try {
    const d = new Date(iso)
    return d.toLocaleTimeString('es-BO', {hourCycle: "h24"})
  } catch {
    return iso
  }
}

function DecisionDot({ decision }: { decision: string }) {
  return (
    <span className={`inline-block size-2 rounded-full ${
      decision === 'GRANTED' ? 'bg-green-500' : 'bg-red-500'
    }`} />
  )
}

export default function AccessFeed({ events }: Props) {
  const [filter, setFilter] = useState<Filter>('ALL')
  const bottomRef = useRef<HTMLDivElement>(null)

  const filtered = filter === 'ALL' ? events : events.filter(e => e.decision === filter)

  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: 'smooth' })
  }, [filtered.length])

  const btn = (f: Filter) =>
    `px-3 py-1.5 text-xs font-semibold rounded-lg transition-all ${
      filter === f
        ? f === 'ALL' ? 'bg-gray-800 text-white shadow-sm'
          : f === 'GRANTED' ? 'bg-green-600 text-white shadow-sm shadow-green-200'
          : 'bg-red-600 text-white shadow-sm shadow-red-200'
        : 'bg-gray-50 text-gray-500 hover:bg-gray-100 border border-gray-200'
    }`

  const showEmpty = (message: string) => (
    <div className="flex flex-col items-center justify-center py-16 text-gray-300">
      <div className="size-12 mb-3 rounded-full bg-gray-50 flex items-center justify-center">
        <svg className="size-6 text-gray-300" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.5}>
          <path strokeLinecap="round" strokeLinejoin="round" d="M9 12.75 11.25 15 15 9.75M21 12c0 1.268-.63 2.39-1.593 3.068a3.745 3.745 0 0 1-1.043 3.296 3.745 3.745 0 0 1-3.296 1.043A3.745 3.745 0 0 1 12 21c-1.268 0-2.39-.63-3.068-1.593a3.746 3.746 0 0 1-3.296-1.043 3.745 3.745 0 0 1-1.043-3.296A3.745 3.745 0 0 1 3 12c0-1.268.63-2.39 1.593-3.068a3.745 3.745 0 0 1 1.043-3.296 3.746 3.746 0 0 1 3.296-1.043A3.746 3.746 0 0 1 12 3c1.268 0 2.39.63 3.068 1.593a3.746 3.746 0 0 1 3.296 1.043 3.746 3.746 0 0 1 1.043 3.296A3.745 3.745 0 0 1 21 12Z" />
        </svg>
      </div>
      <p className="text-sm font-medium text-gray-400">{message}</p>
    </div>
  )

  return (
    <div>
      {/* Filters */}
      <div className="flex gap-2 mb-4">
        <button className={btn('ALL')} onClick={() => setFilter('ALL')}>Todas</button>
        <button className={btn('GRANTED')} onClick={() => setFilter('GRANTED')}>Autorizadas</button>
        <button className={btn('DENIED')} onClick={() => setFilter('DENIED')}>Denegadas</button>
        {filter !== 'ALL' && (
          <span className="text-xs text-gray-400 ml-auto self-center">
            {filtered.length} de {events.length}
          </span>
        )}
      </div>

      {/* Feed */}
      {filtered.length === 0 ? (
        events.length === 0
          ? showEmpty('Esperando lecturas del lector RFID...')
          : showEmpty(filter === 'GRANTED' ? 'Sin autorizaciones' : 'Sin denegaciones')
      ) : (
        <div className="space-y-3 max-h-200 overflow-y-auto pr-1 scroll-smooth">
          {filtered.map((e) => (
            <div
              key={e.eventId}
              className={`relative overflow-hidden rounded-xl border transition-shadow hover:shadow-md ${
                  e.decision === 'GRANTED'
                      ? 'bg-green-50 border-green-200'
                      : 'bg-red-50 border-red-200'
              }`}
            >
              {/* Left accent bar */}
              <div className={`absolute left-0 top-0 bottom-0 w-1 ${
                e.decision === 'GRANTED' ? 'bg-green-500' : 'bg-red-500'
              }`} />

              <div className="p-4 pl-5">
                {/* Top row: badge + timestamp */}
                <div className="flex items-center justify-between mb-2.5">
                  <DecisionDot decision={e.decision} />
                  <span className="text-[11px] text-gray-400 font-mono tracking-tight">
                    {formatTime(e.timestamp)}
                  </span>
                </div>

                {/* Member photo + name row */}
                <div className="flex items-start justify-center gap-3 mb-2">
                  {e.memberPhotoUrl ? (
                    <img
                      src={e.memberPhotoUrl}
                      alt=""
                      className="size-20 rounded-full object-cover ring-2 ring-gray-50 flex-shrink-0"
                      onError={ev => { (ev.target as HTMLImageElement).style.display = 'none' }}
                    />
                  ) : e.memberName ? (
                    <div className="size-20 rounded-full bg-gray-100 flex items-center justify-center flex-shrink-0 ring-2 ring-gray-50">
                      <span className="text-sm font-medium text-gray-400">
                        {e.memberName.charAt(0).toUpperCase()}
                      </span>
                    </div>
                  ) : null}

                  <div className="min-w-0 flex-1">
                    <p className="text-sm font-semibold text-gray-900 truncate">
                      {e.memberName || 'Desconocido'}
                    </p>

                    {e.vehiclePlate && (
                      <p className="text-xs text-gray-500 truncate">
                        {[e.vehicleBrand, e.vehicleModel, e.vehiclePlate, e.vehicleColor].filter(Boolean).join(' · ')}
                      </p>
                    )}

                    {/* Vehicle thumbnail */}
                    {e.vehicleImageUrl && (
                        <div className="mb-2.5 ml-14">
                          <img
                              src={e.vehicleImageUrl}
                              alt=""
                              className="h-52 w-auto rounded-lg object-cover border border-gray-100"
                              onError={ev => { (ev.target as HTMLImageElement).style.display = 'none' }}
                          />
                        </div>
                    )}
                  </div>

                  <StatusBadge status={e.decision} variant="decision" />
                </div>



                {/* EPC + reader metadata */}
                <div className="flex items-center gap-1.5 text-[11px] text-gray-400 truncate">
                  <span className="font-mono">{e.epc}</span>
                  <span>·</span>
                  <span>{e.readerId}</span>
                  <span>·</span>
                  <span>{e.antennaName || `Antena ${e.antenna}`}</span>
                </div>
              </div>
            </div>
          ))}
          <div ref={bottomRef} />
        </div>
      )}
    </div>
  )
}
