import { useEffect, useState } from 'react'

type Me = { id: string; name: string; email: string }

function App() {
  const [me, setMe] = useState<Me | null>(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    fetch('/me')
      .then(res => (res.ok ? res.json() : null))
      .then(data => setMe(data))
      .catch(() => setMe(null))
      .finally(() => setLoading(false))
  }, [])

  if (loading) {
    return <p>Loading…</p>
  }

  if (!me) {
    return (
      <main>
        <h1>OweWell</h1>
        <a href="/oauth2/authorization/google">Sign in with Google</a>
      </main>
    )
  }

  return (
    <main>
      <h1>OweWell</h1>
      <p>Signed in as {me.name} ({me.email})</p>
    </main>
  )
}

export default App
