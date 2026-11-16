import { useState, type FormEvent } from 'react'
import { postJson } from './api'

export function CreateGroupForm({ onCreated }: { onCreated: () => void }) {
  const [name, setName] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [saving, setSaving] = useState(false)

  async function handleSubmit(event: FormEvent) {
    event.preventDefault()   // stop the browser from reloading the page
    setSaving(true)
    setError(null)
    try {
      await postJson('/groups', { name })
      setName('')
      onCreated()            // tell the parent a group was made, so the list refreshes
    } catch (err) {
      setError((err as Error).message)
    } finally {
      setSaving(false)
    }
  }

  return (
    <form onSubmit={handleSubmit}>
      <input
        value={name}
        onChange={event => setName(event.target.value)}
        placeholder="Group name, e.g. Bali Trip"
      />
      <button type="submit" disabled={saving || name.trim() === ''}>
        {saving ? 'Creating…' : 'Create group'}
      </button>
      {error && <p>{error}</p>}
    </form>
  )
}
