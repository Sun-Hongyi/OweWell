// 9000 cents → "$90.00"
export function formatMoney(cents: number, currency: string): string {
  return new Intl.NumberFormat('en-AU', { style: 'currency', currency }).format(cents / 100)
}
