import { useEffect, useState } from 'react'
import { useNavigate, useParams } from 'react-router'
import { deleteRequest, getJson } from './api'
import { formatMoney } from './money'
import { InviteButton } from './InviteButton'
import { AddExpenseForm } from './AddExpenseForm'
import { SettleUp } from './SettleUp'
import { Modal } from './Modal'

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

// "Hongyi Sun" → "HS"
function initials(name: string) {
  return name.split(' ').map(part => part[0]).slice(0, 2).join('').toUpperCase()
}

export function GroupPage() {
  const { groupId } = useParams()
  const navigate = useNavigate()
  const [group, setGroup] = useState<GroupDetails | null>(null)
  const [expenses, setExpenses] = useState<Expense[]>([])
  const [balances, setBalances] = useState<Balance[]>([])
  const [meId, setMeId] = useState<string | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [refreshKey, setRefreshKey] = useState(0)
  const [adding, setAdding] = useState(false)   // is the "Add expense" pop-up open?

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

  // My own balance, for the big summary card
  const myBalance = balances.find(balance => balance.user === meId)
  const myCents = myBalance?.amountCents ?? 0
  const currency = myBalance?.currency ?? 'AUD'

  // A group can only be deleted once nobody owes anything
  const everyoneSettled = balances.every(balance => balance.amountCents === 0)

  async function deleteGroup() {
    if (!window.confirm(`Delete "${group?.name}"? It will disappear for every member.`)) return
    try {
      await deleteRequest(`/groups/${groupId}`)
      navigate('/')   // back to your groups list
    } catch (err) {
      alert((err as Error).message)
    }
  }

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
      {/* Group name and member avatars */}
      <div className="group-header">
        <h2>{group.name}</h2>
        <div className="members">
          {group.members.map(member => (
            <span
              key={member.id}
              className="avatar avatar-sm"
              title={member.id === meId ? `${member.name} (you)` : member.name}
            >
              {initials(member.name)}
            </span>
          ))}
          <span className="member-count">
            {group.members.length} {group.members.length === 1 ? 'member' : 'members'}
          </span>
        </div>
      </div>

      {/* The big summary card */}
      <section className="summary">
        {myCents > 0 ? (
          <>
            <span className="summary-label">You are owed</span>
            <span className="summary-amount owed">{formatMoney(myCents, currency)}</span>
          </>
        ) : myCents < 0 ? (
          <>
            <span className="summary-label">You owe</span>
            <span className="summary-amount owes">{formatMoney(-myCents, currency)}</span>
          </>
        ) : (
          <>
            <span className="summary-label">You're all</span>
            <span className="summary-amount">settled up</span>
          </>
        )}
        <div className="summary-actions">
          <button onClick={() => setAdding(true)}>+ Add expense</button>
        </div>
      </section>

      {group.members.length === 1 && (
        <p className="hint">You're the only one here. Invite friends to start splitting.</p>
      )}
      <InviteButton groupId={group.id} />

      {/* The add-expense form, in a pop-up */}
      {adding && (
        <Modal onClose={() => setAdding(false)}>
          <AddExpenseForm
            groupId={group.id}
            members={group.members}
            me={meId}
            onAdded={() => {
              setAdding(false)
              setRefreshKey(k => k + 1)
            }}
          />
        </Modal>
      )}

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
            <li key={expense.id} className={expense.voided ? 'expense voided' : 'expense'}>
              <div className="expense-main">
                <strong>{expense.description}</strong>
                <span className="sub">
                  Paid by {nameOf(expense.paidBy)}
                  {expense.voided && ' · deleted'}
                </span>
              </div>
              <span className="amount">{formatMoney(expense.totalCents, expense.currency)}</span>
              {!expense.voided && (
                <button onClick={() => deleteExpense(expense.id)}>Delete</button>
              )}
            </li>
          ))}
        </ul>
      )}

      {/* Delete the whole group (only once everyone is settled up) */}
      <div className="danger-zone">
        <button className="btn-danger" onClick={deleteGroup} disabled={!everyoneSettled}>
          Delete group
        </button>
        {!everyoneSettled && <p className="small">Settle up before deleting this group.</p>}
      </div>
    </>
  )
}
