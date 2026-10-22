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
    // Vormiga modalis false — kogemata taustaklõps ei tohi sisestatud teksti kaotada
    isBackdropClosable: {
      type: Boolean,
      default: true,
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
    closeOnBackdrop() {
      if (this.isBackdropClosable) this.close()
    },
  },
}
</script>

<template>
  <div v-if="isOpen">
    <div class="modal d-block" tabindex="-1" @click.self="closeOnBackdrop">
      <!-- Tsentreeritud dialoog katab kogu kõrguse, seega taustaklõps tuleb püüda ka siin -->
      <div
        class="modal-dialog modal-dialog-centered modal-dialog-scrollable"
        :class="sizeClass"
        @click.self="closeOnBackdrop"
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

/* Läbipaistvus on värvis, mitte opacity's — opacity teeks ka blur'i poolläbipaistvaks
   ja terav leht paistaks läbi */
.base-modal-backdrop {
  --bs-backdrop-bg: rgba(28, 38, 43, 0.55);
  --bs-backdrop-opacity: 1;
  backdrop-filter: blur(4px);
}

/* Tumedas teemas oleks moodul sama värvi kui leht — tõstame pinna heledamaks (elevatsioon)
   ja teeme tausta tumedamaks, et moodul eristuks */
[data-bs-theme='dark'] .base-modal-content {
  background-color: var(--bs-secondary-bg);
  border: 1px solid rgba(220, 240, 238, 0.12);
  box-shadow: 0 1.5rem 3.5rem rgba(0, 0, 0, 0.6);
}

[data-bs-theme='dark'] .base-modal-backdrop {
  --bs-backdrop-bg: rgba(0, 0, 0, 0.6);
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
