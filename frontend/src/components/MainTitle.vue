<script>
export default {
  name: 'MainTitle',
  props: {
    title: String,
  },
}
</script>

<template>
  <!-- Vaate päis: pealkiri vasakul, valikulised nupud (default slot) paremal -->
  <div class="title-row d-flex flex-wrap align-items-center justify-content-between gap-3 mb-5">
    <h1 class="fw-bold h2 mb-0">{{ title }}</h1>
    <slot></slot>
  </div>
</template>

<style scoped>
/* Nupu kõrgune — pealkiri on samal kõrgusel nii nuppudega kui ilma vaadetes */
.title-row {
  min-height: 2.375rem;
}
</style>
