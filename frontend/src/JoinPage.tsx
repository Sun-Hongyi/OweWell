import { useState } from 'react'
import { useNavigate, useParams } from 'react-router'
import { postJson } from './api'

export function JoinPage() {
  const { token } = useParams()
  const navigate = useNavigate()
  const [error, setError] = useState<string | null>(null)
  const [joining, setJoining] = useState(false)

  async function join() {
    setJoining(true)
    setError(null)
    try {
      const result = await postJson<{ groupId: string }>(`/invites/${token}/accept`, {})
      navigate(`/group/${result.groupId}`)   // go straight to the group
    } catch (err) {
      setError((err as Error).message)
      setJoining(false)
    }
  }

  return (
    <>
      <h2>You've been invited to a group</h2>
      <button onClick={join} disabled={joining}>
        {joining ? 'Joining…' : 'Join group'}
      </button>
      {error && <p>{error}</p>}
    </>
  )
}
