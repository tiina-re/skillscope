<script>
export default {
  name: 'DotWaveBackground',
}
</script>

<template>
  <!-- Laine ise (täpid, mask, animatsioon) on theme.css .app-dot-wave-layer klassis,
       sest sama laine jookseb ka navbari kaarel (AppNavbar.vue) -->
  <div class="dot-wave app-dot-wave-layer" aria-hidden="true"></div>
</template>

<style scoped>
/* Fixed element algab vaateakna nurgast, nii joonduvad täpid lehe taustaga (theme.css .layout) */
.dot-wave {
  position: fixed;
  inset: 0;
}
</style>
