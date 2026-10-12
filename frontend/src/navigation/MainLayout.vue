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
  <Navbar :show-dot-wave="isDotWaveShown" />

  <div class="d-flex layout">
    <DotWaveBackground v-if="isDotWaveShown" />
    <!-- position-relative hoiab külgmenüü ja sisu täpilaine kohal -->
    <AppSidebar class="position-relative" />
    <main class="flex-grow-1 p-3 position-relative">
      <RouterView />
    </main>
  </div>

  <AiChatWidget v-if="canCreateQuestions" />
</template>
