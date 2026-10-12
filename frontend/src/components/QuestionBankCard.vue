<script>
import { PhCaretDown, PhDotsThree, PhListChecks, PhStar } from '@phosphor-icons/vue'

export default {
  name: 'QuestionBankCard',
  components: { PhCaretDown, PhDotsThree, PhListChecks, PhStar },
  props: {
    statusBadge: { type: Object, default: null },
    competenceName: { type: String, default: '' },
    questionTypeLabel: { type: String, default: '' },
    competenceLevelName: { type: String, default: '' },
    score: { type: Number, default: null },
    isInitiallyOpen: { type: Boolean, default: false },
  },
  data() {
    return {
      isOpen: this.isInitiallyOpen,
    }
  },
  methods: {
    toggleOpen() {
      this.isOpen = !this.isOpen
    },
  },
}
</script>

<template>
  <div class="card question-card app-surface rounded-4">
    <div class="card-body question-card-body">
      <div class="d-flex align-items-start gap-3 question-card-header" @click="toggleOpen">
        <div class="question-card-heading">
          <h5 class="card-title mb-0">
            <slot name="title"></slot>
          </h5>
          <div class="header-row header-badges">
            <span v-if="competenceLevelName" class="badge rounded-pill badge-neutral">
              {{ competenceLevelName }}
            </span>
            <span v-if="competenceName" class="badge rounded-pill badge-competence">
              {{ competenceName }}
            </span>
            <span v-if="statusBadge" class="badge rounded-pill" :class="statusBadge.badgeClass">{{
              statusBadge.name
            }}</span>
          </div>
        </div>
        <div v-if="$slots.menu" class="header-row dropdown flex-shrink-0" @click.stop>
          <button
            class="btn btn-sm menu-button"
            data-bs-toggle="dropdown"
            aria-expanded="false"
            aria-label="Küsimuse tegevused"
          >
            <PhDotsThree :size="18" />
          </button>
          <ul class="dropdown-menu dropdown-menu-custom dropdown-menu-end">
            <slot name="menu"></slot>
          </ul>
        </div>
        <button
          class="header-row btn btn-sm p-0 flex-shrink-0"
          :aria-expanded="isOpen"
          aria-label="Ava või sulge küsimus"
          @click.stop="toggleOpen"
        >
          <PhCaretDown :size="20" class="caret" :class="{ open: isOpen }" />
        </button>
      </div>

      <div v-if="isOpen" class="question-card-content mt-4 pt-4">
        <div class="d-flex flex-wrap gap-2 mb-4">
          <span v-if="questionTypeLabel" class="badge rounded-pill badge-neutral badge-icon">
            <PhListChecks :size="12" weight="bold" />
            {{ questionTypeLabel }}
          </span>
          <span v-if="score !== null" class="badge rounded-pill badge-neutral badge-icon">
            <PhStar :size="12" weight="bold" />
            Punktid: {{ score }}
          </span>
        </div>

        <p class="card-text">
          <slot name="description"></slot>
        </p>

        <h6 class="section-label text-secondary text-uppercase fw-semibold mt-4 mb-3">
          Vastusevariandid
        </h6>
        <div class="answers-grid">
          <slot name="answers"></slot>
        </div>

        <div v-if="$slots.actions" class="mt-4">
          <slot name="actions"></slot>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.question-card {
  /* Paigutus muutub kaardi enda laiuse järgi, mitte akna laiuse järgi (külgmenüü võtab osa ruumist) */
  container: question-card / inline-size;
  min-width: 0;
  transition: box-shadow 0.15s ease;
}

.question-card:hover {
  box-shadow: var(--app-surface-shadow-hover);
}

.question-card-header {
  cursor: pointer;
  min-width: 0;
}

.question-card-body {
  padding: 1.75rem 2rem;
}

@container question-card (min-width: 800px) {
  .question-card-body {
    padding: 2rem 3.5rem;
  }
}

@container question-card (max-width: 599.98px) {
  .question-card-body {
    padding: 1.5rem 1.5rem;
  }
}

.question-card-header {
  --header-row-height: 1.875rem;
}

/* Pealkiri vasakul, kõik mullid (tase, kompetents, staatus) paremal ühes grupis */
.question-card-heading {
  flex: 1 1 0;
  min-width: 0;
  display: flex;
  align-items: flex-start;
  column-gap: 1rem;
  row-gap: 0.5rem;
}

/* Pealkirja esimene rida ja kõik mullid/nupud on sama kõrgusega, et need joonduksid ülevalt */
.question-card-heading .card-title {
  flex: 0 1 auto;
  min-width: 0;
  line-height: var(--header-row-height);
}

.header-badges {
  margin-left: auto;
  flex-shrink: 0;
  flex-wrap: wrap;
  gap: 0.5rem;
}

/* Kitsal kaardil liiguvad kõik mullid pealkirja alla */
@container question-card (max-width: 599.98px) {
  .question-card-heading {
    flex-wrap: wrap;
    column-gap: 0.5rem;
  }

  .question-card-heading .card-title {
    flex-basis: 100%;
  }

  .header-badges {
    margin-left: 0;
    flex-shrink: 1;
  }
}

.header-row {
  min-height: var(--header-row-height);
  display: inline-flex;
  align-items: center;
}

.card-title,
.card-text {
  overflow-wrap: anywhere;
}

.card-text {
  white-space: pre-line;
  line-height: 1.6;
}

.question-card-content {
  border-top: 1px solid var(--bs-border-color);
}

/* Vastusevariandid kahes võrdse laiusega tulbas, kitsal kaardil ühes tulbas */
.answers-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 0.5rem;
}

@container question-card (max-width: 599.98px) {
  .answers-grid {
    grid-template-columns: 1fr;
  }
}

.section-label {
  font-size: 0.75rem;
  letter-spacing: 0.05em;
}

/* Mullid on sama kujuga nagu staatuse mullid (theme.css .badge-active jt) */
.badge-competence {
  background-color: var(--bs-info-bg-subtle);
  color: var(--bs-info-text-emphasis);
  border: var(--bs-border-width) solid var(--bs-info-border-subtle);
}

.badge-neutral {
  background-color: var(--bs-gray-100);
  color: var(--bs-gray-700);
  border: var(--bs-border-width) solid var(--bs-gray-300);
}

.badge-icon {
  display: inline-flex;
  align-items: center;
  gap: 0.25rem;
}

.caret {
  transition: transform 0.2s ease;
}

.caret.open {
  transform: rotate(180deg);
}

/* Ääristeta kolme täpiga nupp, hall taust ainult hiirega peale minnes või menüü avatuna */
.menu-button {
  padding: 0.125rem 0.25rem;
  border: 0;
  border-radius: 0.5rem;
  color: var(--bs-gray-600);
}

.menu-button:hover,
.menu-button.show {
  background-color: var(--bs-gray-200);
  color: var(--bs-body-color);
}

.dropdown-menu-custom {
  --bs-dropdown-min-width: 10rem;
  --bs-dropdown-bg: var(--bs-gray-100);
  --bs-dropdown-border-radius: 0.75rem;
  --bs-dropdown-link-color: var(--bs-emphasis-color);
}
</style>
