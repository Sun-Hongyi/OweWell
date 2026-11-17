import type { ReactNode } from 'react'
import { NavLink } from 'react-router'

// One top-bar item: shows an icon, and swaps to its name on hover
export function NavItem({ label, icon, to, onClick }: {
  label: string
  icon: ReactNode
  to?: string            // a page link…
  onClick?: () => void   // …or a button action (like Sign out)
}) {
  const content = (
    <>
      <span className="nav-icon" aria-hidden="true">{icon}</span>
      <span className="nav-label">{label}</span>
    </>
  )

  if (to) {
    return (
      <NavLink to={to} end className="nav-item" aria-label={label} title={label}>
        {content}
      </NavLink>
    )
  }
  return (
    <button type="button" className="nav-item" aria-label={label} title={label} onClick={onClick}>
      {content}
    </button>
  )
}

// Simple line icons, drawn with SVG
const iconProps = {
  viewBox: '0 0 24 24',
  fill: 'none',
  stroke: 'currentColor',
  strokeWidth: 2,
  strokeLinecap: 'round' as const,
  strokeLinejoin: 'round' as const,
}

export const HomeIcon = () => (
  <svg {...iconProps}>
    <path d="M3 11l9-8 9 8" />
    <path d="M5 10v10h14V10" />
    <path d="M10 20v-6h4v6" />
  </svg>
)

export const ShieldIcon = () => (
  <svg {...iconProps}>
    <path d="M12 3l8 3v6c0 5-3.5 8-8 9-4.5-1-8-4-8-9V6z" />
    <path d="M9 12l2 2 4-4" />
  </svg>
)

export const SignOutIcon = () => (
  <svg {...iconProps}>
    <path d="M15 4h4v16h-4" />
    <path d="M10 8l-4 4 4 4" />
    <path d="M6 12h10" />
  </svg>
)
