<script>
import AppSidebar from '@/navigation/AppSidebar.vue'
import AppNavbar from '@/navigation/AppNavbar.vue'
import AiChatWidget from '@/components/AiChatWidget.vue'
import DotWaveBackground from '@/components/DotWaveBackground.vue'

// Testi tegemise ajal ei tohi tausta liikumine tähelepanu hajutada
const ROUTES_WITHOUT_DOT_WAVE = ['testStartRoute', 'testAttemptRoute']

export default {
  name: 'MainLayout',
  components: { Navbar: AppNavbar, AppSidebar, AiChatWidget, DotWaveBackground },
  data() {
    return {
      // Ainult mobiilis: menüü avaneb ülariba alla (laiemal ekraanil on külgmenüü alati nähtav)
      isMenuOpen: false,
    }
  },
  computed: {
    isDotWaveShown() {
      return !ROUTES_WITHOUT_DOT_WAVE.includes(this.$route.name)
    },
    // AI küsimuste loomine on mõeldud ainult küsimuste koostajatele
    canCreateQuestions() {
      const roleName = sessionStorage.getItem('roleName')
      return roleName === 'ADMIN' || roleName === 'HALDUR'
    },
  },
}
</script>

<template>
  <Navbar
    :show-dot-wave="isDotWaveShown"
    show-menu-button
    :is-menu-open="isMenuOpen"
    @event-menu-toggled="isMenuOpen = !isMenuOpen"
  />

  <!-- position-relative: mobiilis hõljub avatud menüü (AppSidebar, position: absolute) selle ülaservas -->
  <div class="d-flex layout position-relative">
    <DotWaveBackground v-if="isDotWaveShown" />
    <!-- Külgmenüü püsib täpilaine kohal oma CSS-i position'iga (AppSidebar.vue) -->
    <AppSidebar :is-open="isMenuOpen" @event-menu-closed="isMenuOpen = false" />
    <!-- Klõps lehe sisul sulgeb mobiilis avatud menüü -->
    <main class="flex-grow-1 p-3 position-relative" @click="isMenuOpen = false">
      <RouterView />
    </main>
  </div>

  <AiChatWidget v-if="canCreateQuestions" />
</template>
