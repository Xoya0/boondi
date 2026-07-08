import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { Link } from 'react-router-dom'
import { AxiosError } from 'axios'
import AuthLayout from '../components/auth/AuthLayout'
import { authApi } from '../api/auth'

const schema = z.object({
  email: z.string().email('Enter a valid email address'),
})

type FormData = z.infer<typeof schema>

export default function ForgotPasswordPage() {
  const [submitted, setSubmitted] = useState(false)
  const [serverError, setServerError] = useState<string | null>(null)

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<FormData>({ resolver: zodResolver(schema) })

  const onSubmit = async (data: FormData) => {
    setServerError(null)
    try {
      await authApi.forgotPassword(data.email)
      setSubmitted(true)
    } catch (err) {
      const axiosErr = err as AxiosError<{ message: string }>
      setServerError(axiosErr.response?.data?.message ?? 'Something went wrong. Please try again.')
    }
  }

  if (submitted) {
    return (
      <AuthLayout title="Check your email">
        <div className="text-center py-4">
          <div className="text-5xl mb-4">📧</div>
          <p className="text-stone-600 text-sm leading-relaxed">
            If an account exists for that email address, we&apos;ve sent a password reset link.
            Check your inbox (and spam folder).
          </p>
          <Link
            to="/login"
            className="inline-block mt-6 text-sm text-brand-600 hover:text-brand-800 font-medium"
          >
            ← Back to sign in
          </Link>
        </div>
      </AuthLayout>
    )
  }

  return (
    <AuthLayout
      title="Reset your password"
      subtitle="Enter your email and we'll send you a reset link"
    >
      <form onSubmit={handleSubmit(onSubmit)} noValidate className="space-y-4">
        {serverError && (
          <div className="bg-red-50 border border-red-200 text-red-700 text-sm px-4 py-3 rounded-xl" role="alert">
            {serverError}
          </div>
        )}

        <div>
          <label htmlFor="email" className="block text-sm font-medium text-stone-700 mb-1">
            Email address
          </label>
          <input
            id="email"
            type="email"
            autoComplete="email"
            {...register('email')}
            aria-invalid={!!errors.email}
            aria-describedby={errors.email ? 'email-error' : undefined}
            className="w-full px-4 py-2.5 border border-stone-300 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-brand-500 focus:border-transparent transition"
            placeholder="you@example.com"
          />
          {errors.email && (
            <p id="email-error" className="text-red-600 text-xs mt-1">{errors.email.message}</p>
          )}
        </div>

        <button
          type="submit"
          disabled={isSubmitting}
          className="w-full bg-brand-600 hover:bg-brand-700 disabled:bg-brand-400 text-white font-medium py-2.5 px-4 rounded-xl text-sm transition cursor-pointer"
        >
          {isSubmitting ? 'Sending…' : 'Send reset link'}
        </button>
      </form>

      <p className="text-center text-sm text-stone-500 mt-6">
        Remember your password?{' '}
        <Link to="/login" className="text-brand-600 hover:text-brand-800 font-medium">
          Sign in
        </Link>
      </p>
    </AuthLayout>
  )
}
