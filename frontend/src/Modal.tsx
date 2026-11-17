import { useEffect, type ReactNode } from 'react'

// A pop-up window. Closes on the × button, the Escape key, or a click outside it.
export function Modal({ onClose, children }: { onClose: () => void; children: ReactNode }) {
  useEffect(() => {
    function onKey(event: KeyboardEvent) {
      if (event.key === 'Escape') onClose()
    }
    window.addEventListener('keydown', onKey)
    return () => window.removeEventListener('keydown', onKey)
  }, [onClose])

  return (
    <div className="modal-backdrop" onClick={onClose}>
      {/* stopPropagation: clicks inside the pop-up shouldn't count as "outside" */}
      <div className="modal" role="dialog" aria-modal="true" onClick={event => event.stopPropagation()}>
        <button type="button" className="modal-close" aria-label="Close" onClick={onClose}>
          ×
        </button>
        {children}
      </div>
    </div>
  )
}
