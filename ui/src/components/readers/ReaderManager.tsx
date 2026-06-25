import { useEffect, useState } from 'react'
import { api } from '../../services/api'
import type { Reader } from '../../types/api'
import StatusBadge from '../shared/StatusBadge'

export default function ReaderManager() {
  const [readers, setReaders] = useState<Reader[]>([])
  const [name, setName] = useState('')
  const [serial, setSerial] = useState('')
  const [ipAddress, setIpAddress] = useState('')
  const [location, setLocation] = useState('')
  const [message, setMessage] = useState('')
  const [editing, setEditing] = useState<Reader | null>(null)

  const load = () => {
    api.readers.list().then(setReaders).catch(() => {})
  }

  useEffect(load, [])

  const resetForm = () => {
    setName(''); setSerial(''); setIpAddress(''); setLocation(''); setMessage(''); setEditing(null)
  }

  const handleCreate = async () => {
    try {
      await api.readers.create({ name, serial, ipAddress, location })
      setMessage('Lector creado correctamente')
      resetForm()
      load()
    } catch (e: any) {
      setMessage(`Error: ${e.message}`)
    }
  }

  const handleUpdate = async () => {
    if (!editing) return
    try {
      await api.readers.update(editing.id, { name, serial, ipAddress, location })
      setMessage('Lector actualizado correctamente')
      resetForm()
      load()
    } catch (e: any) {
      setMessage(`Error: ${e.message}`)
    }
  }

  const startEdit = (r: Reader) => {
    setEditing(r)
    setName(r.name)
    setSerial(r.serial)
    setIpAddress(r.ipAddress)
    setLocation(r.location)
  }

  const handleToggleStatus = async (id: number, active: boolean) => {
    try {
      await api.readers.setStatus(id, !active)
      load()
    } catch {}
  }

  return (
    <div className="max-w-4xl space-y-6">
      <h2 className="text-xl font-semibold text-gray-900">Lectores</h2>

      {/* Form */}
      <div className="bg-white rounded-lg border border-gray-200 p-4 space-y-3">
        <p className="text-sm font-medium text-gray-700">
          {editing ? 'Editar Lector' : 'Registrar Nuevo Lector'}
        </p>
        <div className="grid grid-cols-2 gap-3">
          <input value={name} onChange={e => setName(e.target.value)} placeholder="Nombre (ej: FX9600-Portón)"
            className="border border-gray-300 rounded-lg px-3 py-2 text-sm" />
          <input value={serial} onChange={e => setSerial(e.target.value)} placeholder="Número de serie"
            className="border border-gray-300 rounded-lg px-3 py-2 text-sm" />
          <input value={ipAddress} onChange={e => setIpAddress(e.target.value)} placeholder="Dirección IP"
            className="border border-gray-300 rounded-lg px-3 py-2 text-sm font-mono" />
          <input value={location} onChange={e => setLocation(e.target.value)} placeholder="Ubicación (ej: Portón Principal)"
            className="border border-gray-300 rounded-lg px-3 py-2 text-sm" />
        </div>
        <div className="flex gap-2">
          <button onClick={editing ? handleUpdate : handleCreate}
            className="bg-indigo-600 text-white px-4 py-2 rounded-lg text-sm font-medium hover:bg-indigo-700">
            {editing ? 'Guardar Cambios' : 'Registrar Lector'}
          </button>
          {editing && (
            <button onClick={resetForm}
              className="text-sm text-gray-600 hover:text-gray-800 px-4 py-2">
              Cancelar
            </button>
          )}
        </div>
        {message && <p className="text-sm text-gray-600">{message}</p>}
      </div>

      {/* List */}
      <div className="bg-white rounded-lg border border-gray-200 overflow-hidden">
        <table className="min-w-full divide-y divide-gray-200">
          <thead className="bg-gray-50">
            <tr>
              <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">Nombre</th>
              <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">Serial</th>
              <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">IP</th>
              <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">Ubicación</th>
              <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">Estado</th>
              <th className="px-4 py-3"></th>
            </tr>
          </thead>
          <tbody className="divide-y divide-gray-200">
            {readers.map(r => (
              <tr key={r.id} className="hover:bg-gray-50">
                <td className="px-4 py-3 text-sm text-gray-900">{r.name}</td>
                <td className="px-4 py-3 text-sm text-gray-500 font-mono">{r.serial}</td>
                <td className="px-4 py-3 text-sm text-gray-500 font-mono">{r.ipAddress}</td>
                <td className="px-4 py-3 text-sm text-gray-500">{r.location}</td>
                <td className="px-4 py-3">
                  <StatusBadge status={String(r.active)} variant="active" />
                </td>
                <td className="px-4 py-3 flex gap-2">
                  <button onClick={() => startEdit(r)}
                    className="text-xs text-indigo-600 hover:text-indigo-800">
                    Editar
                  </button>
                  <button onClick={() => handleToggleStatus(r.id, r.active)}
                    className={`text-xs ${r.active ? 'text-red-600 hover:text-red-800' : 'text-green-600 hover:text-green-800'}`}>
                    {r.active ? 'Desactivar' : 'Activar'}
                  </button>
                </td>
              </tr>
            ))}
            {readers.length === 0 && (
              <tr>
                <td colSpan={6} className="px-4 py-8 text-center text-sm text-gray-400">
                  No hay lectores registrados
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>
    </div>
  )
}
