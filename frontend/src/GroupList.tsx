import { useEffect, useState } from 'react'

type GroupSummary = { id: string; name: string }

export function GroupList() {
  const [groups, setGroups] = useState<GroupSummary[] | null>(null)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    fetch('/groups')
      .then(res => {
        if (!res.ok) throw new Error(`Request failed with status ${res.status}`)
        return res.json()
      })
      .then(setGroups)
      .catch(err => setError(err.message))
  }, [])

  if (error) return <p>Couldn't load your groups: {error}</p>
  if (groups === null) return <p>Loading your groups…</p>
  if (groups.length === 0) return <p>You're not in any groups yet.</p>

  return (
    <ul>
      {groups.map(group => (
        <li key={group.id}>{group.name}</li>
      ))}
    </ul>
  )
}
