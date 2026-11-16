import { useState } from 'react'
import { postJson } from './api'

type Member = { id: string; name: string }

export function AddExpenseForm({ groupId, members, me, onAdded }: {
  groupId: string
  members: Member[]
  me: string
  onAdded: () => void
}) {
  const [description, setDescription] = useState('')
  const [amount, setAmount] = useState('')
  const [paidBy, setPaidBy] = useState(me)
  const [splitBetween, setSplitBetween] = useState<string[]>(members.map(m => m.id))
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState<string | null>(null)

  function toggle(memberId: string) {
    setSplitBetween(current =>
      current.includes(memberId)
        ? current.filter(id => id !== memberId)   // untick
        : [...current, memberId]                  // tick
    )
  }

  async function handleSubmit(event: React.FormEvent) {
    event.preventDefault()
    const amountCents = Math.round(parseFloat(amount) * 100)   // "12.50" → 1250
    if (!description.trim() || !(amountCents > 0) || splitBetween.length === 0) {
      setError('Fill in a description, an amount above $0, and at least one person.')
      return
    }
    setSaving(true)
    setError(null)
    try {
      await postJson(`/groups/${groupId}/expenses`, {
        description,
        paidBy,
        amountCents,
        currency: 'AUD',
        splitBetween,
        split: { type: 'equal', people: splitBetween },
      })
      setDescription('')
      setAmount('')
      onAdded()
    } catch (err) {
      setError((err as Error).message)
    } finally {
      setSaving(false)
    }
  }

  return (
    <form onSubmit={handleSubmit}>
      <h3>Add an expense</h3>
      <p>
        <input placeholder="What was it? (e.g. Dinner)" value={description}
          onChange={e => setDescription(e.target.value)} />{' '}
        <input placeholder="Amount ($)" type="number" step="0.01" min="0" value={amount}
          onChange={e => setAmount(e.target.value)} />
      </p>
      <p>
        Paid by{' '}
        <select value={paidBy} onChange={e => setPaidBy(e.target.value)}>
          {members.map(m => <option key={m.id} value={m.id}>{m.name}</option>)}
        </select>
      </p>
      <p>Split equally between:</p>
      {members.map(m => (
        <label key={m.id} style={{ display: 'block' }}>
          <input type="checkbox" checked={splitBetween.includes(m.id)} onChange={() => toggle(m.id)} />
          {' '}{m.name}
        </label>
      ))}
      <button type="submit" disabled={saving}>{saving ? 'Adding…' : 'Add expense'}</button>
      {error && <p>{error}</p>}
    </form>
  )
}
