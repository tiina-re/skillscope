// Hele/tume režiim. Valik salvestatakse localStorage'isse (jääb alles ka pärast väljalogimist),
// esimesel korral võetakse operatsioonisüsteemi eelistus.
const THEME_STORAGE_KEY = 'theme'
const DARK = 'dark'
const LIGHT = 'light'

export default {
  getInitialTheme() {
    const storedTheme = this.getStoredTheme()
    if (storedTheme === DARK || storedTheme === LIGHT) {
      return storedTheme
    }
    return window.matchMedia('(prefers-color-scheme: dark)').matches ? DARK : LIGHT
  },

  getCurrentTheme() {
    return document.documentElement.getAttribute('data-bs-theme') === DARK ? DARK : LIGHT
  },

  // Bootstrap 5.3 ja theme.css loevad seda atribuuti
  applyTheme(theme) {
    document.documentElement.setAttribute('data-bs-theme', theme)
  },

  toggleTheme() {
    const newTheme = this.getCurrentTheme() === DARK ? LIGHT : DARK
    this.applyTheme(newTheme)
    this.saveTheme(newTheme)
    return newTheme
  },

  // localStorage võib privaatses aknas olla keelatud — siis lihtsalt ei salvestata
  getStoredTheme() {
    try {
      return localStorage.getItem(THEME_STORAGE_KEY)
    } catch {
      return null
    }
  },

  saveTheme(theme) {
    try {
      localStorage.setItem(THEME_STORAGE_KEY, theme)
    } catch {
      // Valik kehtib siis ainult selle lehe avamise ajaks
    }
  },
}
