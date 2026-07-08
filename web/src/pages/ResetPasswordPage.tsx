import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { Link, useNavigate, useSearchParams } from 'react-router-dom'
import { AxiosError } from 'axios'
import AuthLayout from '../components/auth/AuthLayout'
import { authApi } from '../api/auth'

const schema = z.object({
  newPassword: z
    .string()
    .min(8, 'Password must be at least 8 characters')
    .max(100, 'Password is too long'),
  confirmPassword: z.string(),
}).refine(data => data.newPassword === data.confirmPassword, {
  message: "Passwords don't match",
  path: ['confirmPassword'],
})

type FormData = z.infer<typeof schema>

export default function ResetPasswordPage() {
  const navigate = useNavigate()
  const [searchParams] = useSearchParams()
  const token = searchParams.get('token')
  const [serverError, setServerError] = useState<string | null>(null)

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<FormData>({ resolver: zodResolver(schema) })

  if (!token) {
    return (
      <AuthLayout title="Invalid link">
        <div className="text-center py-4">
          <p className="text-stone-600 text-sm">
            This password reset link is invalid or has expired.
          </p>
          <Link
            to="/forgot-password"
            className="inline-block mt-4 text-sm text-brand-600 hover:text-brand-800 font-medium"
          >
            Request a new link
          </Link>
        </div>
      </AuthLayout>
    )
  }

  const onSubmit = async (data: FormData) => {
    setServerError(null)
    try {
      await authApi.resetPassword({ token, newPassword: data.newPassword })
      navigate('/login', { replace: true, state: { passwordReset: true } })
    } catch (err) {
      const axiosErr = err as AxiosError<{ message: string }>
      setServerError(
        axiosErr.response?.data?.message ?? 'Failed to reset password. The link may have expired.'
      )
    }
  }

  return (
    <AuthLayout title="Set a new password" subtitle="Choose a strong password for your account">
      <form onSubmit={handleSubmit(onSubmit)} noValidate className="space-y-4">
        {serverError && (
          <div className="bg-red-50 border border-red-200 text-red-700 text-sm px-4 py-3 rounded-xl" role="alert">
            {serverError}
          </div>
        )}

        <div>
          <label htmlFor="newPassword" className="block text-sm font-medium text-stone-700 mb-1">
            New password
          </label>
          <input
            id="newPassword"
            type="password"
            autoComplete="new-password"
            {...register('newPassword')}
            aria-invalid={!!errors.newPassword}
            aria-describedby={errors.newPassword ? 'newPassword-error' : undefined}
            className="w-full px-4 py-2.5 border border-stone-300 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-brand-500 focus:border-transparent transition"
            placeholder="Minimum 8 characters"
          />
          {errors.newPassword && (
            <p id="newPassword-error" className="text-red-600 text-xs mt-1">{errors.newPassword.message}</p>
          )}
        </div>

        <div>
          <label htmlFor="confirmPassword" className="block text-sm font-medium text-stone-700 mb-1">
            Confirm new password
          </label>
          <input
            id="confirmPassword"
            type="password"
            autoComplete="new-password"
            {...register('confirmPassword')}
            aria-invalid={!!errors.confirmPassword}
            aria-describedby={errors.confirmPassword ? 'confirmPassword-error' : undefined}
            className="w-full px-4 py-2.5 border border-stone-300 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-brand-500 focus:border-transparent transition"
            placeholder="••••••••"
          />
          {errors.confirmPassword && (
            <p id="confirmPassword-error" className="text-red-600 text-xs mt-1">{errors.confirmPassword.message}</p>
          )}
        </div>

        <button
          type="submit"
          disabled={isSubmitting}
          className="w-full bg-brand-600 hover:bg-brand-700 disabled:bg-brand-400 text-white font-medium py-2.5 px-4 rounded-xl text-sm transition cursor-pointer"
        >
          {isSubmitting ? 'Saving…' : 'Set new password'}
        </button>
      </form>
    </AuthLayout>
  )
}
