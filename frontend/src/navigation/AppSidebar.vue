<script>
import {
  PhHouse,
  PhFlask,
  PhFilePlus,
  PhStack,
  PhPersonSimple,
  PhQuestion,
  PhUsers,
} from '@phosphor-icons/vue'
import SessionStorageService from '@/services/SessionStorageService.js'

export default {
  name: 'AppSidebar',
  components: { PhPersonSimple, PhStack, PhFilePlus, PhHouse, PhFlask, PhQuestion, PhUsers },
  methods: {
    // Lingi nähtavus tuleb marsruudi meta.roles'ist (src/router/index.js)
    canSee(path) {
      return SessionStorageService.hasRole(this.$router.resolve(path).meta.roles)
    },
  },
}
</script>

<template>
  <aside class="sidebar-wrapper">
    <nav class="sidebar navbar-light d-flex flex-column p-1 pt-3">
      <div class="nav nav-pills flex-column">
        <p class="sidebar-label"></p>
        <RouterLink
          v-if="canSee('/dashboard')"
          class="nav-link d-flex align-items-center gap-2"
          to="/dashboard"
        >
          <PhHouse :size="20" />
          <span class="nav-text">Töölaud</span>
        </RouterLink>
        <RouterLink
          v-if="canSee('/tests')"
          class="nav-link d-flex align-items-center gap-2"
          to="/tests"
        >
          <PhFlask :size="20" />
          <span class="nav-text">Testid</span>
        </RouterLink>
        <RouterLink
          v-if="canSee('/my-tests')"
          class="nav-link d-flex align-items-center gap-2"
          to="/my-tests"
        >
          <PhPersonSimple :size="20" />
          <span class="nav-text">Minu testid</span>
        </RouterLink>
        <RouterLink
          v-if="canSee('/tests/new')"
          class="nav-link d-flex align-items-center gap-2"
          to="/tests/new"
        >
          <PhFilePlus :size="20" />
          <span class="nav-text">Loo uus test</span>
        </RouterLink>
        <RouterLink
          v-if="canSee('/competences')"
          class="nav-link d-flex align-items-center gap-2"
          to="/competences"
        >
          <PhStack :size="20" />
          <span class="nav-text">Kompetentsid</span>
        </RouterLink>
        <RouterLink
          v-if="canSee('/questions')"
          class="nav-link d-flex align-items-center gap-2"
          :class="{ 'section-active': $route.path.startsWith('/questions') }"
          to="/questions"
        >
          <PhQuestion :size="20" />
          <span class="nav-text">Küsimuste pank</span>
        </RouterLink>
        <RouterLink
          v-if="canSee('/users')"
          class="nav-link d-flex align-items-center gap-2"
          to="/users"
        >
          <PhUsers :size="20" />
          <span class="nav-text">Kasutajad</span>
        </RouterLink>
      </div>
    </nav>
  </aside>
</template>

<style scoped>
.sidebar-wrapper {
  flex-shrink: 0;
  padding: 1.5rem 0 1.5rem 1.5rem;
}
.nav-text {
  text-transform: uppercase;
  font-size: 12px;
  font-weight: bold;
}
.sidebar {
  position: sticky;
  top: 1.5rem;
  width: 220px;
  padding: 1.25rem 0.75rem;
  background: var(--bs-body-bg);
  border: 1px solid var(--app-surface-border);
  border-radius: 1rem;
  overflow: hidden;
  box-shadow: var(--app-surface-shadow);
}

/* Aaloe illustratsiooni tumeda kivi värvi päiseriba üle kogu kaardi laiuse (negatiivsed marginid tühistavad .sidebar paddingu) */
.sidebar-label {
  margin: -1.05rem -0.75rem 0.75rem;
  background: var(--app-sidebar-bar);
  font-size: 0.75rem;
  font-weight: 700;
  letter-spacing: 0.1em;
  text-transform: uppercase;
  color: #fff;
  height: 3px;
}

.nav-link {
  padding: 0.6rem 0.75rem;
  border-radius: 0.6rem;
  color: var(--bs-gray-600);
  font-weight: 500;
  transition:
    background-color 0.2s ease,
    color 0.2s ease;
}

/* Kirjutab üle main.css globaalse a:hover rohelise tausta */
.nav-link:hover {
  background-color: rgba(var(--bs-primary-rgb), 0.06);
  color: var(--app-ink);
}

/* "Küsimuste pank" jääb aktiivseks ka alamlehel /questions/ai */
.nav-link.router-link-exact-active,
.nav-link.section-active {
  background-color: rgba(var(--bs-primary-rgb), 0.1);
  color: var(--bs-primary);
}
</style>
