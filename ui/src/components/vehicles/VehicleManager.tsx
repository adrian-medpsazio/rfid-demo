import { useEffect, useRef, useState } from 'react'
import { api } from '../../services/api'
import type { Member, Vehicle } from '../../types/api'

export default function VehicleManager() {
  const [vehicles, setVehicles] = useState<Vehicle[]>([])
  const [members, setMembers] = useState<Member[]>([])
  const [plate, setPlate] = useState('')
  const [brand, setBrand] = useState('')
  const [model, setModel] = useState('')
  const [color, setColor] = useState('')
  const [message, setMessage] = useState('')
  const [linkVehicleId, setLinkVehicleId] = useState<number | null>(null)
  const [uploading, setUploading] = useState<number | null>(null)

  const load = () => {
    api.vehicles.list().then(setVehicles).catch(() => {})
    api.members.list().then(setMembers).catch(() => {})
  }

  useEffect(load, [])

  const handleCreate = async () => {
    try {
      await api.vehicles.create({ plate, brand, model, color })
      setMessage(`Vehículo creado correctamente`)
      setPlate(''); setBrand(''); setModel(''); setColor('')
      load()
    } catch (e: any) {
      setMessage(`Error: ${e.message}`)
    }
  }

  const handleLink = async (vehicleId: number, memberId: number) => {
    try {
      await api.vehicles.linkToMember(vehicleId, memberId)
      setLinkVehicleId(null)
      load()
    } catch {}
  }

  const handleImageUpload = async (id: number, file: File | undefined) => {
    if (!file) return
    setUploading(id)
    try {
      await api.vehicles.uploadImage(id, file)
      load()
    } catch (e: any) {
      setMessage(`Error al subir imagen: ${e.message}`)
    } finally {
      setUploading(null)
    }
  }

  const linkedMemberName = (v: Vehicle) =>
    v.member ? `${v.member.firstName} ${v.member.lastName}` : '—'

  return (
    <div className="max-w-4xl space-y-6">
      <h2 className="text-xl font-semibold text-gray-900">Vehículos</h2>

      {/* Create form */}
      <div className="bg-white rounded-lg border border-gray-200 p-4 space-y-3">
        <div className="grid grid-cols-2 gap-3">
          <input value={plate} onChange={e => setPlate(e.target.value)} placeholder="Placa"
            className="border border-gray-300 rounded-lg px-3 py-2 text-sm" />
          <input value={brand} onChange={e => setBrand(e.target.value)} placeholder="Marca"
            className="border border-gray-300 rounded-lg px-3 py-2 text-sm" />
          <input value={model} onChange={e => setModel(e.target.value)} placeholder="Modelo"
            className="border border-gray-300 rounded-lg px-3 py-2 text-sm" />
          <input value={color} onChange={e => setColor(e.target.value)} placeholder="Color"
            className="border border-gray-300 rounded-lg px-3 py-2 text-sm" />
        </div>
        <button onClick={handleCreate}
          className="bg-indigo-600 text-white px-4 py-2 rounded-lg text-sm font-medium hover:bg-indigo-700">
          Crear Vehículo
        </button>
        {message && <p className="text-sm text-gray-600">{message}</p>}
      </div>

      {/* Table */}
      <div className="bg-white rounded-lg border border-gray-200 overflow-hidden">
        <table className="min-w-full divide-y divide-gray-200">
          <thead className="bg-gray-50">
            <tr>
              <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">Imagen</th>
              <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">ID</th>
              <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">Placa</th>
              <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">Modelo</th>
              <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">Dueño</th>
              <th className="px-4 py-3"></th>
            </tr>
          </thead>
          <tbody className="divide-y divide-gray-200">
            {vehicles.map(v => (
              <ImageRow key={v.id} vehicle={v} uploading={uploading} onUpload={handleImageUpload} linkVehicleId={linkVehicleId} members={members} onLink={handleLink} onSetLink={setLinkVehicleId} linkedMemberName={linkedMemberName} />
            ))}
          </tbody>
        </table>
      </div>
    </div>
  )
}

function ImageRow({ vehicle, uploading, onUpload, linkVehicleId, members, onLink, onSetLink, linkedMemberName }: {
  vehicle: Vehicle
  uploading: number | null
  onUpload: (id: number, file: File | undefined) => void
  linkVehicleId: number | null
  members: Member[]
  onLink: (vehicleId: number, memberId: number) => void
  onSetLink: (id: number | null) => void
  linkedMemberName: (v: Vehicle) => string
}) {
  const inputRef = useRef<HTMLInputElement>(null)
  const isUploading = uploading === vehicle.id

  return (
    <tr className="hover:bg-gray-50">
      <td className="px-4 py-3">
        <div className="flex items-center gap-2">
          {vehicle.imageKey ? (
            <img
              src={api.vehicles.getImageUrl(vehicle.id)}
              alt="Vehículo"
              className="w-10 h-10 rounded object-cover border border-gray-200"
              onError={e => { (e.target as HTMLImageElement).style.display = 'none' }}
            />
          ) : (
            <div className="w-10 h-10 rounded bg-gray-100 flex items-center justify-center text-xs text-gray-400">
              Sin img
            </div>
          )}
          <input
            ref={inputRef}
            type="file"
            accept="image/jpeg,image/png"
            className="hidden"
            onChange={e => onUpload(vehicle.id, e.target.files?.[0])}
          />
          <button
            onClick={() => inputRef.current?.click()}
            disabled={isUploading}
            className="text-xs text-indigo-600 hover:text-indigo-800 disabled:text-gray-400"
          >
            {isUploading ? 'Subiendo...' : vehicle.imageKey ? 'Cambiar' : 'Subir'}
          </button>
        </div>
      </td>
      <td className="px-4 py-3 text-sm text-gray-900">{vehicle.id}</td>
      <td className="px-4 py-3 text-sm font-mono text-gray-900">{vehicle.plate}</td>
      <td className="px-4 py-3 text-sm text-gray-500">{vehicle.brand} {vehicle.model}</td>
      <td className="px-4 py-3 text-sm text-gray-600">{linkedMemberName(vehicle)}</td>
      <td className="px-4 py-3">
        {linkVehicleId === vehicle.id ? (
          <select
            className="border border-gray-300 rounded px-2 py-1 text-xs"
            onChange={e => onLink(vehicle.id, Number(e.target.value))}
            defaultValue=""
          >
            <option value="" disabled>Seleccionar miembro...</option>
            {members.filter(m => m.active).map(m => (
              <option key={m.id} value={m.id}>{m.firstName} {m.lastName}</option>
            ))}
          </select>
        ) : (
          <button
            onClick={() => onSetLink(vehicle.id)}
            className="text-xs text-indigo-600 hover:text-indigo-800"
          >
            {vehicle.member ? 'Cambiar' : 'Vincular a miembro'}
          </button>
        )}
      </td>
    </tr>
  )
}
