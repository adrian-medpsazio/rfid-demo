interface Props {
  active: string
  onNavigate: (page: string) => void
}

const links = [
  { id: 'gate', label: 'Monitor de Puerta', icon: null },
  { id: 'members', label: 'Miembros', icon: null },
  { id: 'vehicles', label: 'Vehículos', icon: null },
  { id: 'tags', label: 'Tags', icon: null },
  { id: 'readers', label: 'Lectores', icon: null },
  { id: 'logs', label: 'Historial de Accesos', icon: null },
]

export default function Sidebar({ active, onNavigate }: Props) {
  return (
    <aside className="w-64 bg-white border-r border-gray-200 flex flex-col">
      <div className="p-4 border-b border-gray-200">
        <h1 className="text-lg font-bold text-gray-900">Control de Acceso</h1>
        <p className="text-xs text-gray-500">Country Club</p>
      </div>
      <nav className="flex-1 p-2 space-y-1">
        {links.map(l => (
          <button
            key={l.id}
            onClick={() => onNavigate(l.id)}
            className={`w-full flex items-center gap-3 px-3 py-2 rounded-lg text-sm font-medium transition-colors ${
              active === l.id
                ? 'bg-indigo-50 text-indigo-700'
                : 'text-gray-600 hover:bg-gray-50'
            }`}
          >
            {l.label}
          </button>
        ))}
      </nav>
    </aside>
  )
}
