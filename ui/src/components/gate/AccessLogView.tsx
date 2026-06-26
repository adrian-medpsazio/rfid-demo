import { useEffect, useState } from 'react'
import { api } from '../../services/api'
import type { AccessLog } from '../../types/api'
import StatusBadge from '../shared/StatusBadge'

type Filter = 'ALL' | 'GRANTED' | 'DENIED'

export default function AccessLogView() {
  const [logs, setLogs] = useState<AccessLog[]>([])
  const [readerMap, setReaderMap] = useState<Record<string, string>>({})
  const [filter, setFilter] = useState<Filter>('ALL')

  useEffect(() => {
    api.logs.list().then(setLogs).catch(() => {})
    api.readers.list().then(readers => {
      const map: Record<string, string> = {}
      readers.forEach(r => { map[r.name] = r.location })
      setReaderMap(map)
    }).catch(() => {})
  }, [])

  const readerLocation = (readerId: string) => readerMap[readerId] || readerId

  const filtered = filter === 'ALL'
    ? logs
    : logs.filter(l => filter === 'GRANTED' ? l.authorized : !l.authorized)

  const btn = (f: Filter) =>
    `px-3 py-1 text-xs font-medium rounded-full transition-colors ${
      filter === f
        ? f === 'ALL' ? 'bg-gray-700 text-white'
          : f === 'GRANTED' ? 'bg-green-600 text-white'
          : 'bg-red-600 text-white'
        : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
    }`

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <h2 className="text-xl font-semibold text-gray-900">Historial de Accesos</h2>
        <div className="flex gap-2">
          <button className={btn('ALL')} onClick={() => setFilter('ALL')}>Todas</button>
          <button className={btn('GRANTED')} onClick={() => setFilter('GRANTED')}>Autorizadas</button>
          <button className={btn('DENIED')} onClick={() => setFilter('DENIED')}>Denegadas</button>
          {filter !== 'ALL' && (
            <span className="text-xs text-gray-400 self-center ml-1">
              {filtered.length} de {logs.length}
            </span>
          )}
        </div>
      </div>

      <div className="bg-white rounded-lg border border-gray-200 overflow-hidden">
        <div className="max-h-200 overflow-y-auto">
          <table className="min-w-full divide-y divide-gray-200">
            <thead className="bg-gray-50 sticky top-0 z-10">
              <tr>
                <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">EPC</th>
                <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">Miembro</th>
                <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">Decisión</th>
                <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">Ubicación</th>
                <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">Hora</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-200">
              {filtered.map(l => (
                <tr key={l.id} className="hover:bg-gray-50">
                  <td className="px-4 py-3 text-sm font-mono text-gray-900">{l.tagEpc}</td>
                  <td className="px-4 py-3 text-sm text-gray-600">
                    <div className="flex items-center gap-2">
                      {l.member ? (
                        <>
                          {l.member.photoUrl && (
                            <img src={`/api/v1/members/${l.member.id}/photo`} alt=""
                              className="w-6 h-6 rounded-full object-cover border border-gray-200"
                              onError={ev => { (ev.target as HTMLImageElement).style.display = 'none' }} />
                          )}
                          {l.vehicle?.imageKey && (
                              <img src={`/api/v1/vehicles/${l.vehicle.id}/image`} alt=""
                                   className="w-6 h-6 rounded-full object-cover border border-gray-200"
                                   onError={ev => { (ev.target as HTMLImageElement).style.display = 'none' }} />
                          )}
                          <span>{l.member.firstName} {l.member.lastName}</span>
                        </>
                      ) : '-'}
                    </div>
                  </td>
                  <td className="px-4 py-3">
                    <StatusBadge status={l.authorized ? 'GRANTED' : 'DENIED'} variant="decision" />
                  </td>
                  <td className="px-4 py-3 text-sm text-gray-600">{readerLocation(l.readerId)}</td>
                  <td className="px-4 py-3 text-sm text-gray-500">
                    {new Date(l.tagTimestamp).toLocaleString('es-BO', {hourCycle: "h24"})}
                  </td>
                </tr>
              ))}
              {filtered.length === 0 && (
                <tr>
                  <td colSpan={5} className="px-4 py-8 text-center text-sm text-gray-400">
                    {logs.length === 0 ? 'Sin registros aún' : filter === 'GRANTED' ? 'Sin autorizaciones' : 'Sin denegaciones'}
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  )
}
