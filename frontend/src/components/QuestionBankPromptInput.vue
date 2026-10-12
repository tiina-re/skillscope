<script>
import AlertDanger from '@/components/AlertDanger.vue'
import { PhPaperPlaneRight } from '@phosphor-icons/vue'

export default {
  name: 'QuestionBankPromptInput',
  components: {AlertDanger, PhPaperPlaneRight},
  props: {
    canSend: {},
    errorMessage: {},
    handleEnterKey: {},
    inputPlaceholder: {},
    isCompetenceLevelSelected: {},
    maxInputLength: {},
    questionCount: {},
    selectedCompetenceName: {},
    selectedLevelName: {},
    sendMessage: {},
    // v-model väärtus - prop'i ei tohi otse muuta, muudatus saadetakse vanemale emitiga
    modelValue: { type: String, default: '' },
  },
  emits: ['update:modelValue'],
}
</script>

<template>
  <div class="card app-surface rounded-4">
    <div class="card-body generator-card-body">
      <AlertDanger :error-message="errorMessage" class="mb-3"/>
      <div v-if="isCompetenceLevelSelected" class="d-flex flex-wrap gap-2 mb-3">
        <span class="badge rounded-pill badge-neutral">{{ selectedLevelName }}</span>
        <span class="badge rounded-pill badge-competence">{{ selectedCompetenceName }}</span>
        <span class="badge rounded-pill badge-neutral">
            {{ questionCount }} {{ questionCount === 1 ? 'küsimus' : 'küsimust' }}
          </span>
      </div>
      <label for="inputGeneratorPrompt" class="visually-hidden">Juhis AI-le</label>
      <div class="d-flex gap-2 align-items-end">
          <textarea
              id="inputGeneratorPrompt"
              :value="modelValue"
              @input="$emit('update:modelValue', $event.target.value)"
              class="form-control"
              rows="3"
              :maxlength="maxInputLength"
              :placeholder="inputPlaceholder"
              :disabled="!isCompetenceLevelSelected"
              @keydown.enter="handleEnterKey"
          ></textarea>
        <button
            type="button"
            class="btn btn-primary rounded-2 send-button"
            :disabled="!canSend"
            aria-label="Saada juhis"
            @click="sendMessage"
        >
          <PhPaperPlaneRight :size="20"/>
        </button>
      </div>
      <div class="d-flex justify-content-between small text-secondary mt-1">
        <span>Enter saadab, Shift+Enter teeb uue rea</span>
        <span>{{ modelValue.length }}/{{ maxInputLength }}</span>
      </div>
    </div>
  </div>
</template>

<style scoped>
.generator-card-body {
  padding: 1.75rem 2rem;
}

/* Samad mullid nagu QuestionBankCard-is */
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

.send-button {
  height: 3rem;
  width: 3rem;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
</style>