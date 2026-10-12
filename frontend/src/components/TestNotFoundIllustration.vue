<script>
import aloeVeraImage from '@/assets/images/aloe-vera-cuate-green.svg'
// Tumeda režiimi variant: taustamaastiku hallid on tumedale pinnale sobivad, vari tumedam, põrandajoon heledam
import aloeVeraImageDark from '@/assets/images/aloe-vera-cuate-green-dark.svg'

export default {
  name: 'TestNotFoundIllustration',
  props: {
    message: String,
  },
  data() {
    return {
      aloeVeraImage,
      aloeVeraImageDark,
    }
  },
}
</script>

<template>
  <div class="not-found text-center my-4">
    <!-- Õige variant valitakse CSS-iga (theme.css .theme-light-only / .theme-dark-only), nii vahetub pilt
         kohe teemalülitiga. loading="lazy" — peidetud pilti brauser alla ei laadi. -->
    <img
      :src="aloeVeraImage"
      alt="Aaloe illustratsioon"
      class="not-found-image theme-light-only mb-3"
      loading="lazy"
    />
    <img
      :src="aloeVeraImageDark"
      alt="Aaloe illustratsioon"
      class="not-found-image theme-dark-only mb-3"
      loading="lazy"
    />
    <h1 class="h3 fw-semibold mb-2">Testi ei leitud</h1>
    <p class="text-body-secondary mb-0">{{ message }}</p>
  </div>
</template>

<style scoped>
.not-found {
  width: 100%;
  max-width: 36rem;
}

.not-found-image {
  width: 100%;
  max-width: 28rem;
  height: auto;
}
</style>
