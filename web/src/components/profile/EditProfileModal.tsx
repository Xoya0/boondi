import { useEffect, useRef, useState } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import type { UserProfile } from '../../types'
import { usersApi } from '../../api/users'

const schema = z.object({
  displayName: z.string().max(100, 'Display name is too long').optional(),
  bio: z.string().max(500, 'Bio cannot exceed 500 characters').optional(),
  username: z
    .string()
    .min(3, 'Username must be at least 3 characters')
    .max(50, 'Username is too long')
    .regex(/^[a-zA-Z0-9_]+$/, 'Only letters, numbers, and underscores'),
})

type FormData = z.infer<typeof schema>

interface EditProfileModalProps {
  profile: UserProfile
  onClose: () => void
  onSaved: (updated: UserProfile) => void
}

export default function EditProfileModal({ profile, onClose, onSaved }: EditProfileModalProps) {
  const [bannerFile, setBannerFile] = useState<File | null>(null)
  const [bannerPreview, setBannerPreview] = useState<string | null>(profile.bannerImageUrl)
  const [avatarFile, setAvatarFile] = useState<File | null>(null)
  const [avatarPreview, setAvatarPreview] = useState<string | null>(profile.profilePictureUrl)
  const [serverError, setServerError] = useState<string | null>(null)
  const bannerRef = useRef<HTMLInputElement>(null)
  const avatarRef = useRef<HTMLInputElement>(null)

  useEffect(() => {
    const handleKeyDown = (e: KeyboardEvent) => {
      if (e.key === 'Escape') onClose()
    }
    document.addEventListener('keydown', handleKeyDown)
    return () => document.removeEventListener('keydown', handleKeyDown)
  }, [onClose])

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<FormData>({
    resolver: zodResolver(schema),
    defaultValues: {
      displayName: profile.displayName ?? '',
      bio: profile.bio ?? '',
      username: profile.username,
    },
  })

  const handleBannerChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0]
    if (file) {
      setBannerFile(file)
      setBannerPreview(URL.createObjectURL(file))
    }
  }

  const handleAvatarChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0]
    if (file) {
      setAvatarFile(file)
      setAvatarPreview(URL.createObjectURL(file))
    }
  }

  const onSubmit = async (data: FormData) => {
    setServerError(null)
    try {
      if (avatarFile) await usersApi.uploadAvatar(avatarFile)
      if (bannerFile) await usersApi.uploadBanner(bannerFile)
      const updated = await usersApi.updateProfile({
        displayName: data.displayName || undefined,
        bio: data.bio ?? '',
        username: data.username,
      })
      onSaved(updated)
    } catch {
      setServerError('Failed to save profile. Please try again.')
    }
  }

  return (
    <div
      className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4"
      onClick={e => e.target === e.currentTarget && onClose()}
      role="dialog"
      aria-modal="true"
      aria-labelledby="edit-profile-title"
    >
      <div className="bg-white rounded-2xl w-full max-w-lg max-h-[90vh] overflow-y-auto">
        {/* Header */}
        <div className="flex items-center justify-between px-4 py-3 border-b border-stone-100 sticky top-0 bg-white">
          <button onClick={onClose} className="text-stone-500 hover:text-stone-600 cursor-pointer" aria-label="Close">
            <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24" aria-hidden="true">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
            </svg>
          </button>
          <h2 id="edit-profile-title" className="font-semibold text-stone-900 text-sm">Edit profile</h2>
          <button
            form="edit-profile-form"
            type="submit"
            disabled={isSubmitting}
            className="bg-stone-900 hover:bg-stone-700 disabled:bg-stone-300 text-white font-medium px-4 py-1.5 rounded-full text-sm transition cursor-pointer"
          >
            {isSubmitting ? 'Saving…' : 'Save'}
          </button>
        </div>

        <form id="edit-profile-form" onSubmit={handleSubmit(onSubmit)}>
          {/* Banner */}
          <button
            type="button"
            onClick={() => bannerRef.current?.click()}
            aria-label="Change banner image"
            className="relative h-32 w-full bg-stone-200 cursor-pointer block"
          >
            {bannerPreview ? (
              <img src={bannerPreview} alt="" className="w-full h-full object-cover" />
            ) : (
              <div className="w-full h-full flex items-center justify-center">
                <svg className="w-6 h-6 text-stone-500" fill="none" stroke="currentColor" viewBox="0 0 24 24" aria-hidden="true">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5}
                    d="M3 9a2 2 0 012-2h.93a2 2 0 001.664-.89l.812-1.22A2 2 0 0110.07 4h3.86a2 2 0 011.664.89l.812 1.22A2 2 0 0018.07 7H19a2 2 0 012 2v9a2 2 0 01-2 2H5a2 2 0 01-2-2V9z" />
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M15 13a3 3 0 11-6 0 3 3 0 016 0z" />
                </svg>
              </div>
            )}
            <div className="absolute inset-0 bg-black/20 flex items-center justify-center opacity-0 hover:opacity-100 focus-visible:opacity-100 transition-opacity">
              <span className="text-white text-xs font-medium">Change banner</span>
            </div>
          </button>
          <input ref={bannerRef} type="file" accept="image/jpeg,image/png,image/webp" className="hidden" onChange={handleBannerChange} />

          {/* Avatar */}
          <div className="px-4 -mt-8 mb-4">
            <button
              type="button"
              onClick={() => avatarRef.current?.click()}
              aria-label="Change avatar image"
              className="relative w-16 h-16 rounded-full border-2 border-white cursor-pointer block"
            >
              {avatarPreview ? (
                <img src={avatarPreview} alt="" className="w-full h-full rounded-full object-cover bg-stone-200" />
              ) : (
                <div className="w-full h-full rounded-full bg-brand-100 flex items-center justify-center">
                  <span className="text-brand-600 font-bold text-lg">
                    {(profile.displayName ?? profile.username).charAt(0).toUpperCase()}
                  </span>
                </div>
              )}
              <div className="absolute inset-0 rounded-full bg-black/20 flex items-center justify-center opacity-0 hover:opacity-100 focus-visible:opacity-100 transition-opacity">
                <svg className="w-4 h-4 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24" aria-hidden="true">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5}
                    d="M3 9a2 2 0 012-2h.93a2 2 0 001.664-.89l.812-1.22A2 2 0 0110.07 4h3.86a2 2 0 011.664.89l.812 1.22A2 2 0 0018.07 7H19a2 2 0 012 2v9a2 2 0 01-2 2H5a2 2 0 01-2-2V9z" />
                </svg>
              </div>
            </button>
          </div>
          <input ref={avatarRef} type="file" accept="image/jpeg,image/png,image/webp" className="hidden" onChange={handleAvatarChange} />

          {/* Fields */}
          <div className="px-4 pb-6 space-y-4">
            {serverError && (
              <div className="bg-red-50 border border-red-200 text-red-700 text-sm px-4 py-3 rounded-xl" role="alert">
                {serverError}
              </div>
            )}

            <div>
              <label htmlFor="edit-displayName" className="block text-xs font-medium text-stone-500 mb-1">Display name</label>
              <input
                id="edit-displayName"
                type="text"
                {...register('displayName')}
                aria-invalid={!!errors.displayName}
                aria-describedby={errors.displayName ? 'edit-displayName-error' : undefined}
                className="w-full px-3 py-2 border border-stone-200 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-brand-500"
                placeholder="Your Name"
              />
              {errors.displayName && <p id="edit-displayName-error" className="text-red-600 text-xs mt-1">{errors.displayName.message}</p>}
            </div>

            <div>
              <label htmlFor="edit-username" className="block text-xs font-medium text-stone-500 mb-1">Username</label>
              <div className="relative">
                <span className="absolute left-3 top-1/2 -translate-y-1/2 text-stone-500 text-sm" aria-hidden="true">@</span>
                <input
                  id="edit-username"
                  type="text"
                  {...register('username')}
                  aria-invalid={!!errors.username}
                  aria-describedby={errors.username ? 'edit-username-error' : undefined}
                  className="w-full pl-7 pr-3 py-2 border border-stone-200 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-brand-500"
                />
              </div>
              {errors.username && <p id="edit-username-error" className="text-red-600 text-xs mt-1">{errors.username.message}</p>}
            </div>

            <div>
              <label htmlFor="edit-bio" className="block text-xs font-medium text-stone-500 mb-1">Bio</label>
              <textarea
                id="edit-bio"
                {...register('bio')}
                rows={3}
                aria-invalid={!!errors.bio}
                aria-describedby={errors.bio ? 'edit-bio-error' : undefined}
                className="w-full px-3 py-2 border border-stone-200 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-brand-500 resize-none"
                placeholder="Tell us about yourself"
              />
              {errors.bio && <p id="edit-bio-error" className="text-red-600 text-xs mt-1">{errors.bio.message}</p>}
            </div>
          </div>
        </form>
      </div>
    </div>
  )
}
