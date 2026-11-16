import { useEffect, useState } from 'react'
import { BrowserRouter, Link, Route, Routes } from 'react-router'
import { HomePage } from './HomePage'
import { GroupPage } from './GroupPage'
import { JoinPage } from './JoinPage'
import { PrivacyPage } from './PrivacyPage'
import { csrfToken } from './api'

type Me = { id: string; name: string; email: string }

function App() {
  const [me, setMe] = useState<Me | null>(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    fetch('/me')
      .then(res => (res.ok ? res.json() : null))
      .then(data => {
        setMe(data)
        // If they were on a page before signing in (like a join link), go back to it
        const returnTo = sessionStorage.getItem('returnTo')
        if (data && returnTo) {
          sessionStorage.removeItem('returnTo')
          window.location.replace(returnTo)
        }
      })
      .catch(() => setMe(null))
      .finally(() => setLoading(false))
  }, [])

  async function signOut() {
    await fetch('/logout', {
      method: 'POST',
      headers: { 'X-XSRF-TOKEN': csrfToken() },
    })
    window.location.href = '/'   // reload, which now shows "Sign in"
  }

  if (loading) {
    return <p>Loading…</p>
  }

  // The privacy page is public, so show it even when not signed in
  if (!me && window.location.pathname === '/privacy') {
    return (
      <BrowserRouter>
        <main><PrivacyPage /></main>
      </BrowserRouter>
    )
  }

  if (!me) {
    return (
      <main className="landing">
        <h1>OweWell</h1>
        <p className="tagline">Split group expenses without the awkward maths.</p>
        <ul className="features">
          <li>👥 Create a group and invite friends with a link</li>
          <li>🧾 Add expenses: split equally, by shares, by percentage or by exact amounts</li>
          <li>⚖️ See who owes who at a glance</li>
          <li>✅ Settle up in the fewest payments possible</li>
        </ul>
        <a
          href="/oauth2/authorization/google"
          onClick={() => sessionStorage.setItem('returnTo', window.location.pathname)}
        >
          Sign in with Google
        </a>
        <p className="small">Free. No ads. No bank details needed.</p>
        <p className="small"><a href="/privacy">Privacy policy</a></p>
      </main>
    )
  }

  return (
    <BrowserRouter>
      <main>
        <h1><Link to="/">OweWell</Link></h1>
        <p>
          Signed in as {me.name} ({me.email}){' '}
          <button onClick={signOut}>Sign out</button>
        </p>
        <Routes>
          <Route path="/" element={<HomePage />} />
          <Route path="/group/:groupId" element={<GroupPage />} />
          <Route path="/join/:token" element={<JoinPage />} />
          <Route path="/privacy" element={<PrivacyPage />} />
        </Routes>
      </main>
    </BrowserRouter>
  )
}

export default App
