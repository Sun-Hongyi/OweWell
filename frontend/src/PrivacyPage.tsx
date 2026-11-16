import { Link } from 'react-router'

export function PrivacyPage() {
  return (
    <>
      <h2>Privacy policy</h2>
      <p className="small">Last updated: 17 November 2026</p>

      <h3>What OweWell collects</h3>
      <ul>
        <li>From Google sign-in: your name, your email address and your Google account ID.</li>
        <li>What you add: groups, expenses and payments.</li>
      </ul>

      <h3>Why</h3>
      <ul>
        <li>To sign you in, show you your groups and work out who owes who.</li>
        <li>Members of a group can see the names of the other members, and that group's expenses.</li>
      </ul>

      <h3>What OweWell doesn't do</h3>
      <ul>
        <li>No ads, no tracking and no analytics.</li>
        <li>Your data is never sold or shared with anyone outside your groups.</li>
        <li>No bank details or payments are handled. "Mark as paid" is only a record.</li>
      </ul>

      <h3>Cookies</h3>
      <ul>
        <li>One cookie keeps you signed in, and one protects your forms from forged requests (CSRF). Nothing else.</li>
      </ul>

      <h3>Where your data is stored</h3>
      <ul>
        <li>In a PostgreSQL database hosted by Neon in Sydney, Australia. The app runs on Render.</li>
      </ul>

      <h3>Deleting your data</h3>
      <ul>
        <li>Email CONTACT_EMAIL and your account and data will be deleted.</li>
      </ul>

      <p><Link to="/">Back to OweWell</Link></p>
    </>
  )
}
