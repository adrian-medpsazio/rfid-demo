import type { AccessEvent } from '../types/api'

type EventHandler = (event: AccessEvent) => void

export function createSseConnection(onEvent: EventHandler): EventSource {
  const es = new EventSource('/api/v1/events')

  es.addEventListener('access-event', (e: MessageEvent) => {
    try {
      const data: AccessEvent = JSON.parse(e.data)
      onEvent(data)
    } catch {
      // ignore parse errors
    }
  })

  es.onerror = () => {
    // auto-reconnect is built into EventSource
  }

  return es
}
