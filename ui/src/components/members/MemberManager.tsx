import { useEffect, useRef, useState } from 'react'
import { api } from '../../services/api'
import type { Member } from '../../types/api'
import StatusBadge from '../shared/StatusBadge'

export default function MemberManager() {
  const [members, setMembers] = useState<Member[]>([])
  const [firstName, setFirstName] = useState('')
  const [lastName, setLastName] = useState('')
  const [email, setEmail] = useState('')
  const [phone, setPhone] = useState('')
  const [memberCode, setMemberCode] = useState('')
  const [message, setMessage] = useState('')
  const [uploading, setUploading] = useState<number | null>(null)

  const load = () => {
    api.members.list().then(setMembers).catch(() => {})
  }

  useEffect(load, [])

  const handleCreate = async () => {
    try {
      await api.members.create({ firstName, lastName, email, phone, memberCode })
      setMessage(`Miembro creado correctamente`)
      setFirstName(''); setLastName(''); setEmail(''); setPhone(''); setMemberCode('')
      load()
    } catch (e: any) {
      setMessage(`Error: ${e.message}`)
    }
  }

  const handleDeactivate = async (id: number) => {
    try {
      await api.members.deactivate(id)
      load()
    } catch {}
  }

  const handleActivate = async (id: number) => {
    try {
      await api.members.activate(id)
      load()
    } catch {}
  }

  const handlePhotoUpload = async (id: number, file: File | undefined) => {
    if (!file) return
    setUploading(id)
    try {
      await api.members.uploadPhoto(id, file)
      load()
    } catch (e: any) {
      setMessage(`Error al subir foto: ${e.message}`)
    } finally {
      setUploading(null)
    }
  }

  return (
    <div className="max-w-4xl space-y-6">
      <h2 className="text-xl font-semibold text-gray-900">Miembros</h2>

      {/* Form */}
      <div className="bg-white rounded-lg border border-gray-200 p-4 space-y-3">
        <div className="grid grid-cols-2 gap-3">
          <input value={firstName} onChange={e => setFirstName(e.target.value)} placeholder="Nombre"
            className="border border-gray-300 rounded-lg px-3 py-2 text-sm" />
          <input value={lastName} onChange={e => setLastName(e.target.value)} placeholder="Apellido"
            className="border border-gray-300 rounded-lg px-3 py-2 text-sm" />
          <input value={email} onChange={e => setEmail(e.target.value)} placeholder="Email"
            className="border border-gray-300 rounded-lg px-3 py-2 text-sm" />
          <input value={phone} onChange={e => setPhone(e.target.value)} placeholder="Teléfono"
            className="border border-gray-300 rounded-lg px-3 py-2 text-sm" />
          <input value={memberCode} onChange={e => setMemberCode(e.target.value)} placeholder="Código de miembro"
            className="border border-gray-300 rounded-lg px-3 py-2 text-sm" />
        </div>
        <div className="flex gap-2">
          <button onClick={handleCreate}
            className="bg-indigo-600 text-white px-4 py-2 rounded-lg text-sm font-medium hover:bg-indigo-700">
            Crear Miembro
          </button>
        </div>
        {message && <p className="text-sm text-gray-600">{message}</p>}
      </div>

      {/* List */}
      <div className="bg-white rounded-lg border border-gray-200 overflow-hidden">
        <table className="min-w-full divide-y divide-gray-200">
          <thead className="bg-gray-50">
            <tr>
              <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">Foto</th>
              <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">ID</th>
              <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">Nombre</th>
              <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">Email</th>
              <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">Código</th>
              <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">Estado</th>
              <th className="px-4 py-3"></th>
            </tr>
          </thead>
          <tbody className="divide-y divide-gray-200">
            {members.map(m => (
              <PhotoRow key={m.id} member={m} uploading={uploading} onUpload={handlePhotoUpload} onDeactivate={handleDeactivate} onActivate={handleActivate} />
            ))}
          </tbody>
        </table>
      </div>
    </div>
  )
}

function PhotoRow({ member, uploading, onUpload, onDeactivate, onActivate }: {
  member: Member
  uploading: number | null
  onUpload: (id: number, file: File | undefined) => void
  onDeactivate: (id: number) => void
  onActivate: (id: number) => void
}) {
  const inputRef = useRef<HTMLInputElement>(null)
  const isUploading = uploading === member.id

  return (
    <tr className="hover:bg-gray-50">
      <td className="px-4 py-3">
        <div className="flex items-center gap-2">
          {member.photoUrl ? (
            <img
              src={api.members.getPhotoUrl(member.id)}
              alt="Foto"
              className="w-10 h-10 rounded-full object-cover border border-gray-200"
              onError={e => { (e.target as HTMLImageElement).style.display = 'none' }}
            />
          ) : (
            <div className="w-10 h-10 rounded-full bg-gray-100 flex items-center justify-center text-xs text-gray-400">
              Sin foto
            </div>
          )}
          <input
            ref={inputRef}
            type="file"
            accept="image/jpeg,image/png"
            className="hidden"
            onChange={e => onUpload(member.id, e.target.files?.[0])}
          />
          <button
            onClick={() => inputRef.current?.click()}
            disabled={isUploading}
            className="text-xs text-indigo-600 hover:text-indigo-800 disabled:text-gray-400"
          >
            {isUploading ? 'Subiendo...' : member.photoUrl ? 'Cambiar' : 'Subir'}
          </button>
        </div>
      </td>
      <td className="px-4 py-3 text-sm text-gray-900">{member.id}</td>
      <td className="px-4 py-3 text-sm text-gray-900">{member.firstName} {member.lastName}</td>
      <td className="px-4 py-3 text-sm text-gray-500">{member.email}</td>
      <td className="px-4 py-3 text-sm font-mono text-gray-500">{member.memberCode}</td>
      <td className="px-4 py-3">
        <StatusBadge status={String(member.active)} variant="active" />
      </td>
      <td className="px-4 py-3">
        {member.active ? (
          <button onClick={() => onDeactivate(member.id)}
            className="text-xs text-red-600 hover:text-red-800">
            Desactivar
          </button>
        ) : (
          <button onClick={() => onActivate(member.id)}
            className="text-xs text-green-600 hover:text-green-800">
            Activar
          </button>
        )}
      </td>
    </tr>
  )
}
