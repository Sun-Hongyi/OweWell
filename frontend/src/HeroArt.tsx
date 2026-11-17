// A receipt being split into coins, drawn with SVG
export function HeroArt() {
  return (
    <svg className="hero-art" viewBox="0 0 360 360" role="img" aria-label="A receipt being split into coins">
      <defs>
        <linearGradient id="coin" x1="0" y1="0" x2="1" y2="1">
          <stop offset="0" stopColor="#f6c065" />
          <stop offset="1" stopColor="#e8742c" />
        </linearGradient>
      </defs>

      {/* the receipt, slightly tilted, with a torn bottom edge */}
      <g transform="rotate(-8 150 160)">
        <path
          d="M70 40 h160 v220 l-16 -12 -16 12 -16 -12 -16 12 -16 -12 -16 12 -16 -12 -16 12 -16 -12 -16 12 z"
          fill="#fff7f2"
        />
        <rect x="95" y="70" width="70" height="10" rx="5" fill="#ec6a2c" />
        <rect x="95" y="100" width="80" height="8" rx="4" fill="#e6d6cc" />
        <rect x="185" y="100" width="25" height="8" rx="4" fill="#e6d6cc" />
        <rect x="95" y="125" width="60" height="8" rx="4" fill="#e6d6cc" />
        <rect x="185" y="125" width="25" height="8" rx="4" fill="#e6d6cc" />
        <rect x="95" y="150" width="70" height="8" rx="4" fill="#e6d6cc" />
        <rect x="185" y="150" width="25" height="8" rx="4" fill="#e6d6cc" />
        <line x1="95" y1="180" x2="210" y2="180" stroke="#e6d6cc" strokeWidth="2" strokeDasharray="4 6" />
        <rect x="95" y="200" width="45" height="10" rx="5" fill="#2a0f0a" />
        <rect x="165" y="200" width="45" height="10" rx="5" fill="#ec6a2c" />
      </g>

      {/* dashed arrow: the total splitting off */}
      <path
        d="M235 150 C 305 150, 318 195, 292 228"
        fill="none"
        stroke="#f2a03d"
        strokeWidth="4"
        strokeLinecap="round"
        strokeDasharray="6 9"
      />

      {/* three coins = three people */}
      {[
        [250, 262],
        [306, 290],
        [222, 310],
      ].map(([cx, cy]) => (
        <g key={`${cx}-${cy}`}>
          <circle cx={cx} cy={cy} r="32" fill="url(#coin)" stroke="#fff7f2" strokeWidth="4" />
          <text
            x={cx}
            y={cy}
            textAnchor="middle"
            dominantBaseline="central"
            fontSize="30"
            fontWeight="700"
            fill="#fff7f2"
          >
            $
          </text>
        </g>
      ))}
    </svg>
  )
}
