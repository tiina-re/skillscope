<script>
import { PhUserCircle, PhSignOut } from '@phosphor-icons/vue'
import SessionStorageService from '@/services/SessionStorageService.js'
import LoginService from '@/services/LoginService.js'
import NavigationService from '@/services/NavigationService.js'

export default {
  name: 'AccountIcon',
  components: { PhUserCircle, PhSignOut },
  computed: {
    isLoggedIn() {
      return SessionStorageService.userIsLoggedIn()
    },
  },
  methods: {
    logout() {
      LoginService.postLogoutRequest()
        .catch(() => {
          // Kui backendi sessioon on juba aegunud, logime frontendis ikkagi välja
        })
        .finally(() => {
          sessionStorage.clear()
          NavigationService.navigateToLogin()
        })
    },
  },
}
</script>

<template>
  <div class="dropdown me-3">
    <button
      type="button"
      class="btn p-1 account-btn"
      :class="{ 'is-logged-in': isLoggedIn }"
      :aria-label="isLoggedIn ? 'Konto (sisse logitud)' : 'Konto'"
      data-bs-toggle="dropdown"
    >
      <PhUserCircle :size="28" />
    </button>
    <ul class="dropdown-menu dropdown-menu-custom dropdown-menu-end">
      <li>
        <button class="dropdown-item d-flex align-items-center gap-2" @click="logout">
          <PhSignOut :size="18" />
          Logi välja
        </button>
      </li>
    </ul>
  </div>
</template>

<style scoped>
.account-btn,
.account-btn:hover {
  color: var(--bs-primary);
}

/* Sisselogitud kasutajal brändi roheline (vt assets/theme.css) */
.account-btn.is-logged-in,
.account-btn.is-logged-in:hover {
  color: var(--brand-green);
}

.dropdown-menu-custom {
  --bs-dropdown-min-width: 10rem;
  --bs-dropdown-bg: var(--bs-gray-100);
  --bs-dropdown-border-radius: 0.75rem;
  --bs-dropdown-link-color: var(--bs-emphasis-color);
}
</style>
