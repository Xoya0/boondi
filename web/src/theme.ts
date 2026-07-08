/**
 * Dark mode (E10-08). The theme is a `.dark` class on <html> — see index.css for the
 * actual palette mapping. Preference persists in localStorage; when unset we follow
 * the OS preference (and live-update if the OS setting changes).
 */
const STORAGE_KEY = 'boondi-theme'

export type Theme = 'light' | 'dark'

function systemTheme(): Theme {
  return window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light'
}

function storedTheme(): Theme | null {
  const value = localStorage.getItem(STORAGE_KEY)
  return value === 'light' || value === 'dark' ? value : null
}

export function currentTheme(): Theme {
  return document.documentElement.classList.contains('dark') ? 'dark' : 'light'
}

function apply(theme: Theme) {
  document.documentElement.classList.toggle('dark', theme === 'dark')
}

/** Call once before the app renders so the first paint has the right theme. */
export function initTheme() {
  apply(storedTheme() ?? systemTheme())
  // Follow OS changes only while the user hasn't picked an explicit preference.
  window.matchMedia('(prefers-color-scheme: dark)').addEventListener('change', e => {
    if (!storedTheme()) apply(e.matches ? 'dark' : 'light')
  })
}

/** Flips the theme and pins it as an explicit preference. Returns the new theme. */
export function toggleTheme(): Theme {
  const next: Theme = currentTheme() === 'dark' ? 'light' : 'dark'
  localStorage.setItem(STORAGE_KEY, next)
  apply(next)
  return next
}
