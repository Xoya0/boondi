import { Component, type ErrorInfo, type ReactNode } from 'react'

interface Props {
  children: ReactNode
}

interface State {
  error: Error | null
}

/**
 * App-level error boundary (E10-06): a render error anywhere below used to white-screen
 * the whole app with nothing but a console stack trace. Recovery is a full reload —
 * safest option since we can't know how much state the error corrupted.
 */
export default class ErrorBoundary extends Component<Props, State> {
  state: State = { error: null }

  static getDerivedStateFromError(error: Error): State {
    return { error }
  }

  componentDidCatch(error: Error, info: ErrorInfo) {
    console.error('Unhandled render error:', error, info.componentStack)
  }

  render() {
    if (!this.state.error) return this.props.children

    return (
      <div className="min-h-screen bg-white flex items-center justify-center px-4">
        <div className="text-center max-w-sm">
          <p className="text-4xl mb-4">😵</p>
          <h1 className="text-lg font-semibold text-stone-900 mb-2">Something went wrong</h1>
          <p className="text-sm text-stone-400 mb-6">
            An unexpected error occurred. Reloading usually fixes it — if it keeps
            happening, please let us know what you were doing.
          </p>
          <button
            onClick={() => window.location.reload()}
            className="bg-brand-600 hover:bg-brand-700 text-white text-sm font-medium px-5 py-2 rounded-full cursor-pointer"
          >
            Reload
          </button>
        </div>
      </div>
    )
  }
}
