<script>
import { PhBackspace } from '@phosphor-icons/vue'

export default {
  name: 'TestQuestionCard',
  components: { PhBackspace },
  props: {
    question: {},
    totalQuestions: Number,
    questionNumber: Number,
  },
  computed: {
    completionPercentage() {
      return Math.round((this.questionNumber / this.totalQuestions) * 100)
    },
    isMultiple() {
      return this.question.questionTypeName === 'MULTIPLE_CHOICE'
    },
    isFirstQuestion() {
      return this.questionNumber === 1
    },
  },
  methods: {
    toggleAnswer(id) {
      console.log('answer id', id)
      let selectedAnswerIds
      const currentSelectedIds = this.question.selectedQuestionAnswerIds

      if (this.isMultiple) {
        selectedAnswerIds = currentSelectedIds.includes(id)
          ? currentSelectedIds.filter((x) => x !== id)
          : [...currentSelectedIds, id]
      } else {
        selectedAnswerIds = [id]
      }
      this.$emit('event-update-selectedIds', this.question.questionId, selectedAnswerIds)
    },
  },
  emits: ['event-submit-answer', 'event-go-to-previous'],
}
</script>

<template>
  <div class="card my-4 app-surface p-4" style="width: 36rem">
    <div class="card-body">
      <div>
        <div class="d-flex justify-content-between">
          <p style="margin-bottom: 4px; font-weight: lighter">
            <small>Küsimus {{ questionNumber }} / {{ totalQuestions }}</small>
          </p>
          <p style="margin-bottom: 4px; font-weight: lighter">
            <small>{{ completionPercentage }}%</small>
          </p>
        </div>
        <div
          class="progress"
          role="progressbar"
          aria-label="Progressbar"
          style="height: 8px"
          aria-valuenow="25"
          aria-valuemin="0"
          aria-valuemax="100"
        >
          <div class="progress-bar" :style="{ width: `${completionPercentage}%` }"></div>
        </div>
        <p class="mt-5 font-bold h5">
          <strong>{{ question.title }}</strong>
        </p>
        <div class="d-flex flex-column gap-2 my-5">
          <div
            v-for="answer in question.answers"
            :key="answer.questionAnswerId"
            class="answer-option card p-2 py-3"
            :class="{
              selected: this.question.selectedQuestionAnswerIds.includes(answer.questionAnswerId),
            }"
          >
            <label class="form-check">
              <input
                class="form-check-input"
                :type="isMultiple ? 'checkbox' : 'radio'"
                :name="`question-${question.questionId}`"
                :id="`answer-${answer.questionAnswerId}`"
                :value="answer.questionAnswerId"
                :checked="this.question.selectedQuestionAnswerIds.includes(answer.questionAnswerId)"
                @change="toggleAnswer(Number($event.target.value))"
              />
              <span class="form-check-label"> {{ answer.answerText }}</span>
            </label>
          </div>
        </div>
        <div class="d-flex justify-content-between">
          <button
            class="btn btn-outline-light d-flex align-items-center gap-1"
            :class="{ 'text-primary': !isFirstQuestion }"
            :disabled="isFirstQuestion"
            @click="$emit('event-go-to-previous')"
          >
            <PhBackspace :size="14" />
            <span>Tagasi</span>
          </button>
          <button
            class="btn btn-primary d-flex align-items-center"
            :disabled="!this.question.selectedQuestionAnswerIds.length"
            @click="$emit('event-submit-answer', question)"
          >
            <span v-if="questionNumber < totalQuestions">Edasi</span>
            <span v-else>Lõpeta test</span>
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.answer-option {
  transition:
    border-color 0.15s,
    box-shadow 0.15s;
}

.answer-option.selected {
  border-color: var(--bs-primary);
  box-shadow: 0 0 0 0.2rem rgba(var(--bs-primary-rgb), 0.25);
}
</style>
