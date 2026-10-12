<script>
export default {
  name: 'BaseModal',
  props: {
    isOpen: {
      type: Boolean,
      default: false,
    },
    // Bootstrapi modali suurus: '' (vaikimisi), 'sm', 'lg' või 'xl'
    size: {
      type: String,
      default: '',
    },
  },
  emits: ['event-modal-closed'],
  computed: {
    sizeClass() {
      return this.size ? 'modal-' + this.size : ''
    },
  },
  methods: {
    close() {
      this.$emit('event-modal-closed')
    },
  },
}
</script>

<template>
  <div v-if="isOpen">
    <div class="modal d-block" tabindex="-1" @click.self="close">
      <!-- Tsentreeritud dialoog katab kogu kõrguse, seega taustaklõps tuleb püüda ka siin -->
      <div
        class="modal-dialog modal-dialog-centered modal-dialog-scrollable"
        :class="sizeClass"
        @click.self="close"
      >
        <div class="modal-content base-modal-content">
          <div class="modal-header">
            <h4 class="modal-title fw-bold">
              <slot name="title"></slot>
            </h4>
            <button type="button" class="btn-close" aria-label="Sulge" @click="close" />
          </div>
          <div class="modal-body">
            <slot name="body"></slot>
          </div>
          <div v-if="$slots.buttons" class="modal-footer">
            <slot name="buttons"></slot>
          </div>
        </div>
      </div>
    </div>
    <div class="modal-backdrop show base-modal-backdrop" />
  </div>
</template>

<style scoped>
.base-modal-content {
  border: none;
  border-radius: 1.5rem;
  box-shadow: 0 1.5rem 3.5rem rgba(28, 38, 43, 0.35);
  animation: modal-appear 0.2s ease-out;
}

.base-modal-content .modal-header {
  border-bottom: none;
  padding: 2rem 2.5rem 0.5rem;
  color: var(--bs-primary);
}

.base-modal-content .modal-body {
  padding: 1rem 2.5rem;
}

.base-modal-content .modal-footer {
  border-top: none;
  padding: 0.5rem 2.5rem 2rem;
  gap: 0.5rem;
}

/* Footeri nupud on omavahel gap'iga, Bootstrapi vaikimisi margin pole vaja */
.base-modal-content .modal-footer > :deep(*) {
  margin: 0;
}

@media (max-width: 575.98px) {
  .base-modal-content .modal-header,
  .base-modal-content .modal-body,
  .base-modal-content .modal-footer {
    padding-left: 1.5rem;
    padding-right: 1.5rem;
  }
}

.base-modal-backdrop {
  --bs-backdrop-bg: #1c262b;
  --bs-backdrop-opacity: 0.55;
  backdrop-filter: blur(3px);
}

@keyframes modal-appear {
  from {
    opacity: 0;
    transform: translateY(1rem) scale(0.98);
  }
  to {
    opacity: 1;
    transform: none;
  }
}
</style>
