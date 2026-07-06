import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import { ProtectedRoute, PublicOnlyRoute } from './router'
import LoginPage from './pages/LoginPage'
import RegisterPage from './pages/RegisterPage'
import ForgotPasswordPage from './pages/ForgotPasswordPage'
import ResetPasswordPage from './pages/ResetPasswordPage'
import HomePage from './pages/HomePage'
import ProfilePage from './pages/ProfilePage'
import PostDetailPage from './pages/PostDetailPage'
import BookmarksPage from './pages/BookmarksPage'
import FollowListPage from './pages/FollowListPage'
import NotificationsPage from './pages/NotificationsPage'
import SearchPage from './pages/SearchPage'

export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        {/* Public-only: redirect to /home if already logged in */}
        <Route element={<PublicOnlyRoute />}>
          <Route path="/login" element={<LoginPage />} />
          <Route path="/register" element={<RegisterPage />} />
          <Route path="/forgot-password" element={<ForgotPasswordPage />} />
          <Route path="/reset-password" element={<ResetPasswordPage />} />
        </Route>

        {/* Protected: require auth */}
        <Route element={<ProtectedRoute />}>
          <Route path="/home" element={<HomePage />} />
          <Route path="/profile/:username" element={<ProfilePage />} />
          <Route path="/profile/:username/followers" element={<FollowListPage />} />
          <Route path="/profile/:username/following" element={<FollowListPage />} />
          <Route path="/post/:postId" element={<PostDetailPage />} />
          <Route path="/bookmarks" element={<BookmarksPage />} />
          <Route path="/notifications" element={<NotificationsPage />} />
          <Route path="/search" element={<SearchPage />} />
        </Route>

        {/* Default */}
        <Route path="/" element={<Navigate to="/home" replace />} />
        <Route path="*" element={<Navigate to="/login" replace />} />
      </Routes>
    </BrowserRouter>
  )
}
