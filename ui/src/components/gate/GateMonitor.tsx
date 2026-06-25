import { useSse } from '../../hooks/useSse'
import AccessFeed from './AccessFeed'
import GateStatus from './GateStatus'

export default function GateMonitor() {
  const { events, connected } = useSse()

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <h2 className="text-xl font-semibold text-gray-900">Monitor de Puerta</h2>
        <span className={`inline-flex items-center gap-1.5 text-xs font-medium ${
          connected ? 'text-green-600' : 'text-red-600'
        }`}>
          <span className={`w-1.5 h-1.5 rounded-full ${
            connected ? 'bg-green-500' : 'bg-red-500'
          }`} />
          {connected ? 'Conectado' : 'Desconectado'}
        </span>
      </div>

      <GateStatus />

      <div>
        <h3 className="text-sm font-medium text-gray-700 mb-3">Eventos en Vivo</h3>
        <div className="bg-white rounded-lg border border-gray-200 p-4">
          <AccessFeed events={events} />
        </div>
      </div>
    </div>
  )
}
