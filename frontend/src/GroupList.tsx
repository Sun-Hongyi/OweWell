import { useEffect, useState } from 'react'
import { Link } from 'react-router'

type GroupSummary = { id: string; name: string }

export function GroupList({ refreshKey }: { refreshKey: number }) {
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
  }, [refreshKey])

  if (error) return <p>Couldn't load your groups: {error}</p>
  if (groups === null) return <p>Loading your groups…</p>
  if (groups.length === 0) return <p>You're not in any groups yet.</p>

  return (
    <ul>
      {groups.map(group => (
        <li key={group.id}>
          <Link to={`/group/${group.id}`}>{group.name}</Link>
        </li>
      ))}
    </ul>
  )
}
