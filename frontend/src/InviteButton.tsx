import { useState } from 'react'
import { postJson } from './api'

type CreatedInvite = { token: string; expiresAt: string }

export function InviteButton({ groupId }: { groupId: string }) {
  const [link, setLink] = useState<string | null>(null)
  const [copied, setCopied] = useState(false)
  const [error, setError] = useState<string | null>(null)

  async function createInvite() {
    setError(null)
    setCopied(false)
    try {
      const invite = await postJson<CreatedInvite>(`/groups/${groupId}/invites`, {})
      setLink(`${window.location.origin}/join/${invite.token}`)
    } catch (err) {
      setError((err as Error).message)
    }
  }

  async function copyLink() {
    if (!link) return
    await navigator.clipboard.writeText(link)
    setCopied(true)
  }

  return (
    <div>
      <button onClick={createInvite}>Invite friends</button>
      {link && (
        <p>
          Share this link (it works for 7 days):{' '}
          <input value={link} readOnly size={60} />{' '}
          <button onClick={copyLink}>{copied ? 'Copied!' : 'Copy'}</button>
        </p>
      )}
      {error && <p>{error}</p>}
    </div>
  )
}
