<script>
import AccountIcon from '@/navigation/AccountIcon.vue'
import ThemeToggle from '@/navigation/ThemeToggle.vue'

export default {
  name: 'AppNavbar',
  components: { AccountIcon: AccountIcon, ThemeToggle },
  props: {
    showAccountIcon: {
      type: Boolean,
      default: true,
    },
    // Lehe täpilaine jookseb ka üle kaare. MainLayout annab sama tingimuse, millega näidatakse
    // DotWaveBackground'i, et mõlemad kihid alustaksid animatsiooni samal hetkel.
    showDotWave: {
      type: Boolean,
      default: false,
    },
  },
}
</script>

<template>
  <nav class="navbar navbar-expand-lg px-4 app-nav shadow-sm d-flex justify-content-between">
    <span class="nav-arc" aria-hidden="true">
      <span v-if="showDotWave" class="nav-arc-wave app-dot-wave-layer"></span>
    </span>
    <RouterLink class="navbar-brand brand" to="/">
      <span class="brand-mark" aria-hidden="true"></span>
      <span class="brand-skill">Skill</span><span class="brand-scope">Scope</span>
    </RouterLink>
    <div class="d-flex align-items-center gap-2">
      <ThemeToggle :class="{ 'me-3': !showAccountIcon }" />
      <AccountIcon v-if="showAccountIcon" />
    </div>
  </nav>
</template>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Sora:wght@700;800&display=swap');

.app-nav {
  position: relative;
  /* Navbar jääb lehe täpilaine (DotWaveBackground) kohale */
  z-index: 1;
  background: var(--bs-body-bg);
  border-bottom: 1px solid var(--app-nav-border);
}

.nav-arc {
  position: absolute;
  top: 0;
  left: 0;
  width: 360px;
  height: 100%;
  /* Sama täpivõrk ja alatoon nagu lehe taustal (theme.css .layout) —
     fixed-kinnitusega joonduvad täpid lehe taustaga. Kaare kuju tuleb maskist. */
  background-color: var(--app-nav-arc-bg);
  background-image: radial-gradient(var(--app-nav-arc-dot) 1px, transparent 1px);
  background-size: 22px 22px;
  background-attachment: fixed;
  -webkit-mask: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 100 100' preserveAspectRatio='none'%3E%3Cpath d='M0 0 H40 Q52 45 85 100 H0 Z' fill='black'/%3E%3C/svg%3E")
    no-repeat 0 0 / 100% 100%;
  mask: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 100 100' preserveAspectRatio='none'%3E%3Cpath d='M0 0 H40 Q52 45 85 100 H0 Z' fill='black'/%3E%3C/svg%3E")
    no-repeat 0 0 / 100% 100%;
  pointer-events: none;
}

/* Laine täidab kogu kaare; kaare mask lõikab selle kaare kujuliseks */
.nav-arc-wave {
  position: absolute;
  inset: 0;
}

/* keep the nav content above the curve */
.app-nav > * {
  position: relative;
  z-index: 1;
}

.app-nav > .nav-arc {
  position: absolute;
  z-index: 0;
}

.brand {
  display: inline-flex;
  align-items: center;
  font-family: 'Sora', system-ui, sans-serif;
  font-size: 1.6rem;
  letter-spacing: -0.06em;
  text-decoration: none;
}

.brand-skill {
  font-weight: 800;
  color: var(--app-ink);
}

/* Rohelised toonid on tuletatud aaloe illustratsiooni mündirohelisest (#92e3a9) */
.brand-scope {
  font-weight: 800;
  background: linear-gradient(90deg, var(--brand-green), #3fbf74);
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
}

/* little "scope" target icon */
.brand-mark {
  width: 1.1rem;
  height: 1.1rem;
  border-radius: 50%;
  margin-right: 0.35rem;
  border: 3px solid var(--brand-green);
  position: relative;
}
.brand-mark::after {
  content: '';
  position: absolute;
  inset: 3px;
  border-radius: 50%;
  background: #92e3a9;
}

.brand:hover .brand-mark {
  transform: rotate(90deg) scale(1.1);
  transition: transform 0.25s ease;
}
</style>
