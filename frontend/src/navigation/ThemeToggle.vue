<script>
import ThemeService from '@/services/ThemeService.js'
import { PhMoon, PhSun } from '@phosphor-icons/vue'

export default {
  name: 'ThemeToggle',
  components: { PhMoon, PhSun },

  data() {
    return {
      theme: ThemeService.getCurrentTheme(),
    }
  },

  computed: {
    isDark() {
      return this.theme === 'dark'
    },
    toggleLabel() {
      return this.isDark ? 'Lülita hele režiim' : 'Lülita tume režiim'
    },
  },

  methods: {
    toggleTheme() {
      this.theme = ThemeService.toggleTheme()
    },
  },
}
</script>

<template>
  <button
    type="button"
    class="btn p-1 theme-toggle"
    :title="toggleLabel"
    :aria-label="toggleLabel"
    @click="toggleTheme"
  >
    <PhSun v-if="isDark" :size="24" />
    <PhMoon v-else :size="24" />
  </button>
</template>

<style scoped>
.theme-toggle,
.theme-toggle:hover {
  color: var(--bs-primary);
}

.theme-toggle:hover {
  opacity: 0.75;
}
</style>
