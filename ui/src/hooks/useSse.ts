import { useEffect, useState, useCallback } from 'react'
import { createSseConnection } from '../services/sse'
import type { AccessEvent } from '../types/api'

const MAX_EVENTS = 100

export function useSse() {
  const [events, setEvents] = useState<AccessEvent[]>([])
  const [connected, setConnected] = useState(false)

  const addEvent = useCallback((event: AccessEvent) => {
    setEvents(prev => [event, ...prev].slice(0, MAX_EVENTS))
  }, [])

  useEffect(() => {
    const es = createSseConnection(addEvent)

    es.onopen = () => setConnected(true)

    return () => {
      es.close()
      setConnected(false)
    }
  }, [addEvent])

  return { events, connected }
}
