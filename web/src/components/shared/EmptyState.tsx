interface EmptyStateProps {
  icon: string
  message: string
  action?: { label: string; onClick: () => void }
}

/** Shared "nothing here yet" treatment for lists — a little icon instead of bare text, used
 *  consistently across every feed/list page. */
export default function EmptyState({ icon, message, action }: EmptyStateProps) {
  return (
    <div className="text-center py-16 px-6">
      <div className="text-4xl mb-3" aria-hidden="true">{icon}</div>
      <p className="text-stone-500 text-sm">{message}</p>
      {action && (
        <button
          onClick={action.onClick}
          className="mt-3 text-brand-600 text-sm font-medium hover:underline cursor-pointer"
        >
          {action.label}
        </button>
      )}
    </div>
  )
}
