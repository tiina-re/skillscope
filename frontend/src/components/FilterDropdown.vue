<script>
import { PhCaretDown } from '@phosphor-icons/vue'

// Vaate päise filtri rippmenüü (küsimuste pank, testid, minu testid).
// Valik antakse edasi sündmusega, filtreerimise teeb vaade ise.
export default {
  name: 'FilterDropdown',
  components: { PhCaretDown },
  props: {
    // [{ value, label }]
    options: {
      type: Array,
      required: true,
    },
    selectedValue: {
      type: [Number, String],
      default: null,
    },
    // Valikuline "Kõik ..." valik nimekirja alguses eraldajaga, nt { value: null, label: 'Kõik kompetentsid' }
    allOption: {
      type: Object,
      default: null,
    },
  },
  emits: ['event-option-selected'],
  computed: {
    selectedLabel() {
      const selectedOption = [this.allOption, ...this.options].find(
        (option) => option && option.value === this.selectedValue,
      )
      return selectedOption ? selectedOption.label : ''
    },
  },
}
</script>

<template>
  <div class="dropdown">
    <button
      class="btn btn-light border fw-bold rounded-2 d-flex align-items-center gap-2"
      data-bs-toggle="dropdown"
      aria-expanded="false"
    >
      <span>{{ selectedLabel }}</span>
      <PhCaretDown :size="14" />
    </button>
    <ul class="dropdown-menu dropdown-menu-custom dropdown-menu-end">
      <template v-if="allOption">
        <li>
          <button
            class="dropdown-item"
            :class="{ active: selectedValue === allOption.value }"
            @click="$emit('event-option-selected', allOption.value)"
          >
            {{ allOption.label }}
          </button>
        </li>
        <li><hr class="dropdown-divider" /></li>
      </template>
      <li v-for="option in options" :key="option.value">
        <button
          class="dropdown-item"
          :class="{ active: selectedValue === option.value }"
          @click="$emit('event-option-selected', option.value)"
        >
          {{ option.label }}
        </button>
      </li>
    </ul>
  </div>
</template>

<style scoped>
.dropdown-menu-custom {
  --bs-dropdown-min-width: 12rem;
  --bs-dropdown-bg: var(--bs-gray-100);
  --bs-dropdown-border-radius: 0.75rem;
  --bs-dropdown-link-color: var(--bs-emphasis-color);
}
</style>
