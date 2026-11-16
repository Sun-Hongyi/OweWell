import { useEffect, useState } from 'react'
import { BrowserRouter, Link, Route, Routes } from 'react-router'
import { HomePage } from './HomePage'
import { GroupPage } from './GroupPage'
import { JoinPage } from './JoinPage'

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

  if (loading) {
    return <p>Loading…</p>
  }

  if (!me) {
    return (
      <main>
        <h1>OweWell</h1>
        <a
          href="/oauth2/authorization/google"
          onClick={() => sessionStorage.setItem('returnTo', window.location.pathname)}
        >
          Sign in with Google
        </a>
      </main>
    )
  }

  return (
    <BrowserRouter>
      <main>
        <h1><Link to="/">OweWell</Link></h1>
        <p>Signed in as {me.name} ({me.email})</p>
        <Routes>
          <Route path="/" element={<HomePage />} />
          <Route path="/group/:groupId" element={<GroupPage />} />
          <Route path="/join/:token" element={<JoinPage />} />
        </Routes>
      </main>
    </BrowserRouter>
  )
}

export default App
