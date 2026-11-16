import { useState } from 'react'
import { CreateGroupForm } from './CreateGroupForm'
import { GroupList } from './GroupList'

export function HomePage() {
  const [refreshKey, setRefreshKey] = useState(0)

  return (
    <>
      <h2>Your groups</h2>
      <CreateGroupForm onCreated={() => setRefreshKey(key => key + 1)} />
      <GroupList refreshKey={refreshKey} />
    </>
  )
}
