<script>
import { PhWarningCircle } from '@phosphor-icons/vue'

export default {
  name: 'TestNotFoundCard',
  components: { PhWarningCircle },
  props: {
    message: String,
  },
}
</script>

<template>
  <div  class="card my-4 app-surface p-4 text-center start-card">
    <div class="card-body">
      <PhWarningCircle :size="48" class="text-secondary mb-3" />
      <h1 class="h4 fw-semibold mb-2">Testi ei leitud</h1>
      <p class="text-secondary mb-0">{{ message }}</p>
    </div>
  </div>
</template>

<style scoped></style>
