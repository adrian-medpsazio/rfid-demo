interface Props {
  status: string
  variant?: 'decision' | 'active' | 'default'
}

const variants = {
  decision: { AUTORIZADO: 'bg-green-200 text-green-800', DENEGADO: 'bg-red-100 text-red-800' },
  active: { Activo: 'bg-green-200 text-green-800', Inactivo: 'bg-gray-100 text-gray-800' },
  default: 'bg-blue-100 text-blue-800',
}

export default function StatusBadge({ status, variant = 'default' }: Props) {
  const cls =
    variant === 'decision'
      ? variants.decision[status as keyof typeof variants.decision] ?? variants.default
      : variant === 'active'
        ? variants.active[status as keyof typeof variants.active] ?? variants.default
        : variants.default

  return (
    <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${cls}`}>
      {status}
    </span>
  )
}
