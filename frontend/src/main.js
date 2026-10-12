import './assets/main.css'

import { createApp } from 'vue'
import { createPinia } from 'pinia'
import axios from 'axios'

import App from './App.vue'
import router from './router'
import ThemeService from './services/ThemeService.js'

// Hele/tume režiim enne esimest renderdamist, et leht ei vilguks
ThemeService.applyTheme(ThemeService.getInitialTheme())

// Bootstrap
import 'bootstrap/dist/css/bootstrap.min.css'
import 'bootstrap/dist/js/bootstrap.js'
// Brändi teema — peab tulema pärast Bootstrapi CSS-i
import './assets/theme.css'

// Extra imports
// leafleti css kujindused
import "leaflet/dist/leaflet.css";

const app = createApp(App)

app.use(createPinia())
app.use(router)

// Axios globaalselt kättesaadavaks
app.config.globalProperties.$axios = axios

// Sessiooni aegumisel suunatakse sisselogimise lehele
axios.interceptors.response.use(
  response => response,
  error => {
    if (error.response?.status === 401) {
      sessionStorage.clear()
      router.push({ name: 'loginRoute' })
    }
    return Promise.reject(error)
  }
)

app.mount('#app')
