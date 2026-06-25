import Sidebar from './Sidebar'

interface Props {
  page: string
  onNavigate: (page: string) => void
  children: React.ReactNode
}

export default function AppShell({ page, onNavigate, children }: Props) {
  return (
    <div className="flex h-screen bg-gray-50">
      <Sidebar active={page} onNavigate={onNavigate} />
      <main className="flex-1 overflow-auto p-6">
        {children}
      </main>
    </div>
  )
}
