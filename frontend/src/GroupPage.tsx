import { useEffect, useState } from 'react'
import { useParams } from 'react-router'
import { getJson } from './api'
import { formatMoney } from './money'

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

export function GroupPage() {
  const { groupId } = useParams()
  const [group, setGroup] = useState<GroupDetails | null>(null)
  const [expenses, setExpenses] = useState<Expense[]>([])
  const [balances, setBalances] = useState<Balance[]>([])
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    // Ask for all three at once, and wait until they've all arrived
    Promise.all([
      getJson<GroupDetails>(`/groups/${groupId}`),
      getJson<Expense[]>(`/groups/${groupId}/expenses`),
      getJson<Balance[]>(`/groups/${groupId}/balances`),
    ])
      .then(([details, expenseList, balanceList]) => {
        setGroup(details)
        setExpenses(expenseList)
        setBalances(balanceList)
      })
      .catch(err => setError(err.message))
  }, [groupId])

  if (error) return <p>Couldn't load this group: {error}</p>
  if (!group) return <p>Loading…</p>

  // Turn a user ID into their name, using the member list
  const nameOf = (userId: string) =>
    group.members.find(member => member.id === userId)?.name ?? userId

  return (
    <>
      <h2>{group.name}</h2>

      <h3>Members</h3>
      <ul>
        {group.members.map(member => (
          <li key={member.id}>{member.name}</li>
        ))}
      </ul>

      <h3>Balances</h3>
      {balances.length === 0 ? (
        <p>No balances yet. Add an expense to get started.</p>
      ) : (
        <ul>
          {balances.map(balance => (
            <li key={balance.user}>
              {nameOf(balance.user)}{' '}
              {balance.amountCents > 0
                ? `is owed ${formatMoney(balance.amountCents, balance.currency)}`
                : balance.amountCents < 0
                  ? `owes ${formatMoney(-balance.amountCents, balance.currency)}`
                  : 'is settled up'}
            </li>
          ))}
        </ul>
      )}

      <h3>Expenses</h3>
      {expenses.length === 0 ? (
        <p>No expenses yet.</p>
      ) : (
        <ul>
          {expenses.map(expense => (
            <li key={expense.id} style={expense.voided ? { textDecoration: 'line-through', opacity: 0.6 } : {}}>
              {expense.description}: {formatMoney(expense.totalCents, expense.currency)}, paid by{' '}
              {nameOf(expense.paidBy)}
              {expense.voided && ' (deleted)'}
            </li>
          ))}
        </ul>
      )}
    </>
  )
}
