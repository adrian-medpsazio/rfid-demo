import { useState } from 'react'
import AppShell from './components/layout/AppShell'
import GateMonitor from './components/gate/GateMonitor'
import TagManager from './components/tags/TagManager'
import AccessLogView from './components/gate/AccessLogView'
import MemberManager from './components/members/MemberManager'
import VehicleManager from './components/vehicles/VehicleManager'
import ReaderManager from './components/readers/ReaderManager'

export default function App() {
  const [page, setPage] = useState('gate')

  return (
    <AppShell page={page} onNavigate={setPage}>
      {page === 'gate' && <GateMonitor />}
      {page === 'members' && <MemberManager />}
      {page === 'vehicles' && <VehicleManager />}
      {page === 'tags' && <TagManager />}
      {page === 'readers' && <ReaderManager />}
      {page === 'logs' && <AccessLogView />}
    </AppShell>
  )
}
