import { useState } from 'react'
import { postJson } from './api'
import { formatMoney } from './money'

type Member = { id: string; name: string }
type SplitType = 'equal' | 'shares' | 'percentage' | 'exact'

export function AddExpenseForm({ groupId, members, me, onAdded }: {
  groupId: string
  members: Member[]
  me: string
  onAdded: () => void
}) {
  const [description, setDescription] = useState('')
  const [amount, setAmount] = useState('')
  const [paidBy, setPaidBy] = useState(me)
  const [splitType, setSplitType] = useState<SplitType>('equal')
  const [splitBetween, setSplitBetween] = useState<string[]>(members.map(m => m.id))
  const [values, setValues] = useState<Record<string, string>>({})   // per-person number for shares/percent/exact
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const amountCents = Math.round(parseFloat(amount) * 100)   // "12.50" → 1250

  function toggle(memberId: string) {
    setSplitBetween(current =>
      current.includes(memberId)
        ? current.filter(id => id !== memberId)
        : [...current, memberId]
    )
  }

  function setValue(memberId: string, value: string) {
    setValues(current => ({ ...current, [memberId]: value }))
  }

  // Adds up what's been typed so far, for the "so far" hint
  function typedTotal(): number {
    return members.reduce((sum, m) => {
      const n = parseFloat(values[m.id] ?? '')
      if (!(n > 0)) return sum
      return sum + (splitType === 'exact' ? Math.round(n * 100) : n)
    }, 0)
  }

  // Builds the "split" part of the request, or throws a friendly error
  function buildSplit() {
    if (splitType === 'equal') {
      if (splitBetween.length === 0) throw new Error('Tick at least one person.')
      return { people: splitBetween, split: { type: 'equal', people: splitBetween } }
    }

    if (splitType === 'exact') {
      const amountsCents: Record<string, number> = {}
      for (const m of members) {
        const cents = Math.round(parseFloat(values[m.id] ?? '') * 100)
        if (cents > 0) amountsCents[m.id] = cents
      }
      const people = Object.keys(amountsCents)
      if (people.length === 0) throw new Error('Enter an amount for at least one person.')
      const sum = Object.values(amountsCents).reduce((a, b) => a + b, 0)
      if (sum !== amountCents) {
        throw new Error(`The amounts add up to ${formatMoney(sum, 'AUD')}, but the total is ${formatMoney(amountCents, 'AUD')}.`)
      }
      return { people, split: { type: 'exact', amountsCents } }
    }

    // shares and percentage both use "weights" (whole numbers)
    const weights: Record<string, number> = {}
    for (const m of members) {
      const n = parseInt(values[m.id] ?? '', 10)
      if (n > 0) weights[m.id] = n
    }
    const people = Object.keys(weights)
    if (people.length === 0) throw new Error('Enter a number for at least one person.')
    if (splitType === 'percentage') {
      const sum = Object.values(weights).reduce((a, b) => a + b, 0)
      if (sum !== 100) throw new Error(`The percentages add up to ${sum}%, but they need to add up to 100%.`)
    }
    return { people, split: { type: splitType, weights } }
  }

  async function handleSubmit(event: React.FormEvent) {
    event.preventDefault()
    setError(null)
    if (!description.trim() || !(amountCents > 0)) {
      setError('Fill in a description and an amount above $0.')
      return
    }
    let built
    try {
      built = buildSplit()
    } catch (err) {
      setError((err as Error).message)
      return
    }
    setSaving(true)
    try {
      await postJson(`/groups/${groupId}/expenses`, {
        description,
        paidBy,
        amountCents,
        currency: 'AUD',
        splitBetween: built.people,
        split: built.split,
      })
      setDescription('')
      setAmount('')
      setValues({})
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
      <p>
        Split{' '}
        <select value={splitType} onChange={e => { setSplitType(e.target.value as SplitType); setValues({}) }}>
          <option value="equal">equally</option>
          <option value="shares">by shares</option>
          <option value="percentage">by percentage</option>
          <option value="exact">by exact amounts</option>
        </select>
      </p>

      {splitType === 'equal' ? (
        members.map(m => (
          <label key={m.id} style={{ display: 'block' }}>
            <input type="checkbox" checked={splitBetween.includes(m.id)} onChange={() => toggle(m.id)} />
            {' '}{m.name}
          </label>
        ))
      ) : (
        <>
          {members.map(m => (
            <label key={m.id} style={{ display: 'block' }}>
              {m.name}{' '}
              <input
                type="number"
                min="0"
                step={splitType === 'exact' ? '0.01' : '1'}
                placeholder={splitType === 'shares' ? 'shares' : splitType === 'percentage' ? '%' : '$'}
                value={values[m.id] ?? ''}
                onChange={e => setValue(m.id, e.target.value)}
                style={{ width: 80 }}
              />
            </label>
          ))}
          {splitType === 'percentage' && <p>So far: {typedTotal()}% of 100%</p>}
          {splitType === 'exact' && amountCents > 0 && (
            <p>So far: {formatMoney(typedTotal(), 'AUD')} of {formatMoney(amountCents, 'AUD')}</p>
          )}
          {splitType === 'shares' && <p>Example: 2 and 1 means the first person pays twice as much.</p>}
        </>
      )}

      <button type="submit" disabled={saving}>{saving ? 'Adding…' : 'Add expense'}</button>
      {error && <p>{error}</p>}
    </form>
  )
}
