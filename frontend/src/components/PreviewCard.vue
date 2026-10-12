<script>
import { PhDotsThree } from '@phosphor-icons/vue'

export default {
  name: 'PreviewCard',
  components: { PhDotsThree },
  props: {
    statusBadge: { type: Object, default: null },
  },
}
</script>

<template>
  <div class="card preview-card app-surface rounded-4">
    <div class="card-body d-flex flex-column">
      <div class="d-flex align-items-start gap-4 mt-1 mb-3">
        <h4 class="card-title flex-grow-1 mb-0">
          <slot name="title"></slot>
        </h4>
        <span
          class="badge rounded-pill flex-shrink-0 mt-1"
          v-if="statusBadge"
          :class="statusBadge.badgeClass"
          >{{ statusBadge.name }}
        </span>
        <div v-if="$slots.menu" class="dropdown flex-shrink-0">
          <button class="btn btn-sm p-0" data-bs-toggle="dropdown">
            <PhDotsThree :size="22" />
          </button>
          <ul class="dropdown-menu dropdown-menu-custom dropdown-menu-end">
            <slot name="menu"></slot>
          </ul>
        </div>
      </div>
      <p class="card-text flex-grow-1"><slot name="description"></slot></p>
      <div class="d-flex flex-wrap gap-2 mt-auto">
        <slot name="actions"></slot>
      </div>
    </div>
  </div>
</template>

<style scoped>
.card {
  min-width: 0;
  transition:
    transform 0.15s ease,
    box-shadow 0.15s ease;
}

.preview-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--app-surface-shadow-hover);
}

.card-title,
.card-text {
  min-width: 0;
  overflow-wrap: anywhere;
}

/* Pikk pealkiri lõigatakse kahe rea järel "…"-ga, täispealkiri on näha testi vaates */
.card-title {
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  overflow: hidden;
}

.card-text {
  min-height: 4.5rem;
}

.dropdown-menu-custom {
  --bs-dropdown-min-width: 10rem;
  --bs-dropdown-bg: var(--bs-gray-100);
  --bs-dropdown-border-radius: 0.75rem;
  --bs-dropdown-link-color: var(--bs-emphasis-color);
}
</style>
