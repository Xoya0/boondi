import type { ReactNode } from 'react'

interface AuthLayoutProps {
  title: string
  subtitle?: string
  children: ReactNode
}

export default function AuthLayout({ title, subtitle, children }: AuthLayoutProps) {
  return (
    <div className="min-h-screen bg-gray-50 flex items-center justify-center px-4">
      <div className="w-full max-w-md">
        {/* Logo / Brand */}
        <div className="text-center mb-8">
          <h1 className="text-4xl font-bold text-indigo-600 tracking-tight">Boondi</h1>
          <p className="text-gray-500 text-sm mt-1">Private social networking</p>
        </div>

        {/* Card */}
        <div className="bg-white rounded-2xl shadow-sm border border-gray-200 p-8">
          <h2 className="text-xl font-semibold text-gray-900 mb-1">{title}</h2>
          {subtitle && <p className="text-sm text-gray-500 mb-6">{subtitle}</p>}
          {children}
        </div>
      </div>
    </div>
  )
}
