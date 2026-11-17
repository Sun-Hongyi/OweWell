import { useEffect, useState } from 'react'
import { BrowserRouter, Link, Route, Routes } from 'react-router'
import { HomePage } from './HomePage'
import { GroupPage } from './GroupPage'
import { JoinPage } from './JoinPage'
import { PrivacyPage } from './PrivacyPage'
import { HeroArt } from './HeroArt'
import { NavItem, HomeIcon, ShieldIcon, SignOutIcon } from './NavItem'
import { csrfToken } from './api'

type Me = { id: string; name: string; email: string }

// "Owe" in white, "Well" in orange
function Brand() {
  return <>Owe<span>Well</span></>
}

// Remember the current page, so sign-in brings you back to it (e.g. a join link)
function rememberPage() {
  sessionStorage.setItem('returnTo', window.location.pathname)
}

// "Hongyi Sun" → "HS"
function initials(name: string) {
  return name.split(' ').map(part => part[0]).slice(0, 2).join('').toUpperCase()
}

function App() {
  const [me, setMe] = useState<Me | null>(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    fetch('/me')
      .then(res => (res.ok ? res.json() : null))
      .then(data => {
        setMe(data)
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
    window.location.href = '/'
  }

  if (loading) {
    return <p style={{ textAlign: 'center', marginTop: 80 }}>Loading…</p>
  }

  // The privacy page is public, so show it even when not signed in
  if (!me && window.location.pathname === '/privacy') {
    return (
      <BrowserRouter>
        <header className="topbar">
          <a className="brand" href="/"><Brand /></a>
        </header>
        <main><PrivacyPage /></main>
      </BrowserRouter>
    )
  }

  // Landing page for visitors who aren't signed in
  if (!me) {
    return (
      <div className="landing">
        <header className="topbar">
          <span className="brand"><Brand /></span>
          <a className="btn-primary" href="/oauth2/authorization/google" onClick={rememberPage}>
            Sign in
          </a>
        </header>

        <section className="hero">
          <div>
            <h1>Split costs,<br />not friendships.</h1>
            <p className="tagline">
              Add shared expenses, see who owes who,<br />
              and settle up in the fewest payments.
            </p>
            <a className="btn-primary btn-lg" href="/oauth2/authorization/google" onClick={rememberPage}>
              Get started
            </a>
            <p className="small">
              Free · No ads · No bank details · <a href="/privacy">Privacy</a>
            </p>
          </div>
          <HeroArt />
        </section>

        <section className="features">
          <div className="feature">
            <strong>Groups</strong>
            <span>Create a group and invite friends with a link.</span>
          </div>
          <div className="feature">
            <strong>Any split</strong>
            <span>Equally, by shares, by percentage or exact amounts.</span>
          </div>
          <div className="feature">
            <strong>Clear balances</strong>
            <span>See who owes who at a glance.</span>
          </div>
          <div className="feature">
            <strong>Fewest payments</strong>
            <span>An optimal algorithm settles everyone up.</span>
          </div>
        </section>
      </div>
    )
  }

  // Signed in: top bar with icon nav, then the page
  return (
    <BrowserRouter>
      <header className="topbar">
        <Link to="/" className="brand"><Brand /></Link>
        <nav className="nav">
          <NavItem label="Groups" icon={<HomeIcon />} to="/" />
          <NavItem label="Privacy" icon={<ShieldIcon />} to="/privacy" />
          <NavItem label="Sign out" icon={<SignOutIcon />} onClick={signOut} />
        </nav>
        <div className="avatar" title={me.name}>{initials(me.name)}</div>
      </header>
      <main>
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
