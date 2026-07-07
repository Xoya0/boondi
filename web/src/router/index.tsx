import { Navigate, Outlet } from 'react-router-dom'
import { useAuthStore } from '../store/authStore'

export function ProtectedRoute() {
  const isAuthenticated = useAuthStore(s => s.isAuthenticated())
  if (!isAuthenticated) {
    return <Navigate to="/login" replace />
  }
  return <Outlet />
}

export function PublicOnlyRoute() {
  const isAuthenticated = useAuthStore(s => s.isAuthenticated())
  if (isAuthenticated) {
    return <Navigate to="/home" replace />
  }
  return <Outlet />
}

/** Admin-only routes (E9-06) — requires auth AND role === 'ADMIN'. */
export function AdminRoute() {
  const isAuthenticated = useAuthStore(s => s.isAuthenticated())
  const user = useAuthStore(s => s.user)
  if (!isAuthenticated) {
    return <Navigate to="/login" replace />
  }
  if (user?.role !== 'ADMIN') {
    return <Navigate to="/home" replace />
  }
  return <Outlet />
}
