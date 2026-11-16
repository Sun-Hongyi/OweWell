import { useEffect, useState } from 'react'
import { useParams } from 'react-router'
import { deleteRequest, getJson } from './api'
import { formatMoney } from './money'
import { InviteButton } from './InviteButton'
import { AddExpenseForm } from './AddExpenseForm'
import { SettleUp } from './SettleUp'

type Member = { id: string; name: string }
type GroupDetails = { id: string; name: string; members: Member[] }
type Expense = {
  id: string
  description: string
  paidBy: string
  totalCents: number
  currency: string
  voided: boolean
}
type Balance = { user: string; amountCents: number; currency: string }
type Me = { id: string }

export function GroupPage() {
  const { groupId } = useParams()
  const [group, setGroup] = useState<GroupDetails | null>(null)
  const [expenses, setExpenses] = useState<Expense[]>([])
  const [balances, setBalances] = useState<Balance[]>([])
  const [meId, setMeId] = useState<string | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [refreshKey, setRefreshKey] = useState(0)

  useEffect(() => {
    // Ask for everything at once, and wait until it's all arrived
    Promise.all([
      getJson<GroupDetails>(`/groups/${groupId}`),
      getJson<Expense[]>(`/groups/${groupId}/expenses`),
      getJson<Balance[]>(`/groups/${groupId}/balances`),
      getJson<Me>('/me'),
    ])
      .then(([details, expenseList, balanceList, me]) => {
        setGroup(details)
        setExpenses(expenseList)
        setBalances(balanceList)
        setMeId(me.id)
      })
      .catch(err => setError(err.message))
  }, [groupId, refreshKey])

  if (error) return <p>Couldn't load this group: {error}</p>
  if (!group || !meId) return <p>Loading…</p>

  // Turn a user ID into their name, and say "You" for yourself
  const nameOf = (userId: string) =>
    userId === meId
      ? 'You'
      : group.members.find(member => member.id === userId)?.name ?? userId

  async function deleteExpense(expenseId: string) {
    if (!window.confirm('Delete this expense?')) return
    try {
      await deleteRequest(`/groups/${groupId}/expenses/${expenseId}`)
      setRefreshKey(k => k + 1)
    } catch (err) {
      alert((err as Error).message)
    }
  }

  return (
    <>
      <h2>{group.name}</h2>

      <h3>Members</h3>
      <ul>
        {group.members.map(member => (
          <li key={member.id}>{member.name}{member.id === meId && ' (you)'}</li>
        ))}
      </ul>
      <InviteButton groupId={group.id} />

      <AddExpenseForm
        groupId={group.id}
        members={group.members}
        me={meId}
        onAdded={() => setRefreshKey(k => k + 1)}
      />

      <h3>Balances</h3>
      {balances.length === 0 ? (
        <p>No balances yet. Add an expense to get started.</p>
      ) : (
        <ul>
          {balances.map(balance => (
            <li key={balance.user}>
              {nameOf(balance.user)}{' '}
              {balance.amountCents > 0 ? (
                <span className="owed">
                  {balance.user === meId ? 'are' : 'is'} owed {formatMoney(balance.amountCents, balance.currency)}
                </span>
              ) : balance.amountCents < 0 ? (
                <span className="owes">
                  {balance.user === meId ? 'owe' : 'owes'} {formatMoney(-balance.amountCents, balance.currency)}
                </span>
              ) : (
                <span className="settled">{balance.user === meId ? 'are' : 'is'} settled up</span>
              )}
            </li>
          ))}
        </ul>
      )}

      <SettleUp
        groupId={group.id}
        nameOf={nameOf}
        refreshKey={refreshKey}
        onPaid={() => setRefreshKey(k => k + 1)}
      />

      <h3>Expenses</h3>
      {expenses.length === 0 ? (
        <p>No expenses yet.</p>
      ) : (
        <ul>
          {expenses.map(expense => (
            <li key={expense.id} style={expense.voided ? { textDecoration: 'line-through', opacity: 0.6 } : {}}>
              {expense.description}: {formatMoney(expense.totalCents, expense.currency)}, paid by{' '}
              {nameOf(expense.paidBy)}
              {expense.voided
                ? ' (deleted)'
                : <button onClick={() => deleteExpense(expense.id)}>Delete</button>}
            </li>
          ))}
        </ul>
      )}
    </>
  )
}
