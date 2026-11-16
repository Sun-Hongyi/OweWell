import { useEffect, useState } from 'react'
import { getJson, postJson } from './api'
import { formatMoney } from './money'

type Payment = { from: string; to: string; amountCents: number; currency: string }

export function SettleUp({ groupId, nameOf, refreshKey, onPaid }: {
  groupId: string
  nameOf: (userId: string) => string
  refreshKey: number
  onPaid: () => void
}) {
  const [payments, setPayments] = useState<Payment[]>([])
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    getJson<Payment[]>(`/groups/${groupId}/settlement`)
      .then(setPayments)
      .catch(err => setError(err.message))
  }, [groupId, refreshKey])

  async function markPaid(payment: Payment) {
    const message = `Mark ${nameOf(payment.from)} → ${nameOf(payment.to)} ` +
      `${formatMoney(payment.amountCents, payment.currency)} as paid?`
    if (!window.confirm(message)) return
    try {
      await postJson(`/groups/${groupId}/payments`, payment)
      onPaid()
    } catch (err) {
      alert((err as Error).message)
    }
  }

  if (error) return <p>Couldn't load settlement: {error}</p>

  return (
    <>
      <h3>Settle up</h3>
      {payments.length === 0 ? (
        <p>Everyone is settled up 🎉</p>
      ) : (
        <ul>
          {payments.map((payment, index) => {
            const payer = nameOf(payment.from)
            return (
              <li key={index}>
                {payer} {payer === 'You' ? 'pay' : 'pays'} {nameOf(payment.to)}{' '}
                {formatMoney(payment.amountCents, payment.currency)}
                <button onClick={() => markPaid(payment)}>Mark as paid</button>
              </li>
            )
          })}
        </ul>
      )}
    </>
  )
}
