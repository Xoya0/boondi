interface ErrorStateProps {
  message: string
  onRetry?: () => void
}

/** Shared error treatment for lists — every page used to render this ad hoc, and only one
 *  of them actually offered a retry action. */
export default function ErrorState({ message, onRetry }: ErrorStateProps) {
  return (
    <div className="text-center py-12 px-6" role="alert">
      <div className="text-3xl mb-2" aria-hidden="true">⚠️</div>
      <p className="text-stone-500 text-sm mb-3">{message}</p>
      {onRetry && (
        <button
          onClick={onRetry}
          className="text-brand-600 text-sm font-medium hover:underline cursor-pointer"
        >
          Retry
        </button>
      )}
    </div>
  )
}
