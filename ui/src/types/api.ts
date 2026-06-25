export interface Reader {
  id: number
  name: string
  serial: string
  ipAddress: string
  location: string
  active: boolean
  createdAt: string
}

export interface Member {
  id: number
  firstName: string
  lastName: string
  email: string
  phone: string
  photoUrl: string
  memberCode: string
  active: boolean
  createdAt: string
  updatedAt: string
}

export interface Vehicle {
  id: number
  plate: string
  brand: string
  model: string
  color: string
  member: Member | null
  createdAt: string
}

export interface RfidTag {
  id: number
  epc: string
  member: Member | null
  vehicle: Vehicle | null
  assignedAt: string
  revokedAt: string | null
  active: boolean
}

export interface AccessLog {
  id: number
  tagEpc: string
  readerId: string
  member: Member | null
  vehicle: Vehicle | null
  authorized: boolean
  reason: string
  readCount: number
  tagTimestamp: string
  serverTimestamp: string
  createdAt: string
}

export interface AccessEvent {
  eventId: string
  timestamp: string
  readerId: string
  antenna: number
  antennaName: string | null
  epc: string
  decision: string
  reason: string | null
  memberName: string | null
  vehiclePlate: string | null
  vehicleColor: string | null
  vehicleBrand: string | null
  vehicleModel: string | null
}
