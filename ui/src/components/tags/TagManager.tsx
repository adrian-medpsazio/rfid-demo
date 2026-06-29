import {useEffect, useState} from 'react'
import {api} from '../../services/api'
import type {Member, Vehicle} from '../../types/api'

export default function TagManager() {
    const [epc, setEpc] = useState('')
    const [entityType, setEntityType] = useState<'MEMBER' | 'VEHICLE'>('MEMBER')
    const [entityId, setEntityId] = useState('')
    const [members, setMembers] = useState<Member[]>([])
    const [vehicles, setVehicles] = useState<Vehicle[]>([])
    const [message, setMessage] = useState('')
    const [loadingTag, setLoadingTag] = useState(false)

    useEffect(() => {
        api.members.list().then(setMembers).catch(() => {
        })
        api.vehicles.list().then(setVehicles).catch(() => {
        })
    }, [])

    // cuando cambia la entidad seleccionada, buscar si ya tiene tag
    useEffect(() => {
        if (!entityId) {
            setEpc('');
            return
        }

        setLoadingTag(true)
        const lookup = entityType === 'MEMBER'
            ? api.tags.getByMember(Number(entityId))
            : api.tags.getByVehicle(Number(entityId))

        lookup.then(tag => {
            setEpc(tag?.epc ?? '')
        }).catch(() => {
            setEpc('')
        }).finally(() => setLoadingTag(false))
    }, [entityId, entityType])

    const handleAssign = async () => {
        try {
            const tag = await api.tags.assign(epc, entityType, entityId)
            setMessage(`Tag asignado (ID: ${tag.id}) a ${entityType === 'MEMBER' ? 'miembro' : 'vehículo'} #${entityId}`)
        } catch (e: any) {
            setMessage(`Error: ${e.message}`)
        }
    }

    const handleUnassign = async () => {
        try {
            await api.tags.unassign(epc)
            setMessage(`Tag desvinculado`)
            setEpc('')
        } catch (e: any) {
            setMessage(`Error: ${e.message}`)
        }
    }

    return (
        <div className="max-w-lg space-y-6">
            <h2 className="text-xl font-semibold text-gray-900">Asignación de Tags</h2>

            <div className="bg-white rounded-lg border border-gray-200 p-4 space-y-4">
                <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">EPC</label>
                    <input
                        type="text"
                        value={epc}
                        onChange={e => setEpc(e.target.value)}
                        className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:ring-2 focus:ring-indigo-500"
                        placeholder={loadingTag ? 'Buscando tag existente...' : 'E20000123456789012345678'}
                    />
                </div>

                <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">Asignar a</label>
                    <div className="flex gap-2 mb-2">
                        <button
                            onClick={() => {
                                setEntityType('MEMBER');
                                setEntityId('')
                            }}
                            className={`px-3 py-1.5 rounded-lg text-xs font-medium ${
                                entityType === 'MEMBER' ? 'bg-indigo-100 text-indigo-700' : 'bg-gray-100 text-gray-600'
                            }`}
                        >Miembro
                        </button>
                        {/*<button*/}
                        {/*  onClick={() => { setEntityType('VEHICLE'); setEntityId('') }}*/}
                        {/*  className={`px-3 py-1.5 rounded-lg text-xs font-medium ${*/}
                        {/*    entityType === 'VEHICLE' ? 'bg-indigo-100 text-indigo-700' : 'bg-gray-100 text-gray-600'*/}
                        {/*  }`}*/}
                        {/*>Vehículo</button>*/}
                    </div>

                    <select
                        value={entityId}
                        onChange={e => setEntityId(e.target.value)}
                        className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm"
                    >
                        <option value="">Seleccionar {/* {entityType === 'MEMBER' ?  '*/}un
                            miembro{/*' :  'un vehículo'}*/}...
                        </option>
                        {
                            members.filter(m => m.active)
                                .flatMap(m => vehicles.filter(v => v.member?.id === m.id)
                                    .map(v => (
                                            <option key={m.id} value={m.id}>{m.firstName} {m.lastName} ({m.memberCode})
                                                — {v.plate} - {v.brand} {v.model}</option>
                                        )
                                    )
                                )
                        }
                    </select>
                </div>

                <div className="flex gap-2">
                    <button
                        onClick={handleAssign}
                        disabled={!epc || !entityId}
                        className="flex-1 bg-indigo-600 text-white px-4 py-2 rounded-lg text-sm font-medium hover:bg-indigo-700 disabled:opacity-50 transition-colors"
                    >
                        Asignar Tag
                    </button>
                    <button
                        onClick={handleUnassign}
                        disabled={!epc}
                        className="flex-1 bg-gray-200 text-gray-700 px-4 py-2 rounded-lg text-sm font-medium hover:bg-gray-300 disabled:opacity-50 transition-colors"
                    >
                        Desvincular
                    </button>
                </div>

                {message && <p className="text-sm text-gray-600">{message}</p>}
            </div>
        </div>
    )
}
