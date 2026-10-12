<script>
import { PhPlus, PhX } from '@phosphor-icons/vue'
import AlertDanger from '@/components/AlertDanger.vue'
import CompetenceService from '@/services/CompetenceService.js'
import CompetenceLevelService from '@/services/CompetenceLevelService.js'
import QuestionTypeService from '@/services/QuestionTypeService.js'
import QuestionService from '@/services/QuestionService.js'

const TRUE_FALSE = 'TRUE_FALSE'
const MULTIPLE_CHOICE = 'MULTIPLE_CHOICE'

export default {
  name: 'QuestionCreateForm',
  components: { AlertDanger, PhPlus, PhX },

  emits: ['event-cancel', 'event-question-created'],

  beforeMount() {
    this.getCompetences()
    this.getQuestionTypes()
  },

  data() {
    return {
      errorMessage: '',

      competences: [],
      competenceLevels: [],
      questionTypes: [],
      questionTypeLabels: {
        SINGLE_CHOICE: 'Üks õige vastus',
        MULTIPLE_CHOICE: 'Mitu õiget vastust',
        TRUE_FALSE: 'Tõene või väär',
      },

      competenceId: 0,
      competenceLevelId: 0,
      title: '',
      description: '',
      questionTypeId: 0,
      score: null,
      answers: [
        { key: 0, answerText: '', isCorrect: false },
        { key: 1, answerText: '', isCorrect: false },
      ],
      nextAnswerKey: 2,
    }
  },

  computed: {
    selectedQuestionTypeName() {
      const questionType = this.questionTypes.find(
        (type) => type.questionTypeId === this.questionTypeId,
      )
      return questionType ? questionType.questionTypeName : ''
    },
    isTrueFalse() {
      return this.selectedQuestionTypeName === TRUE_FALSE
    },
    isMultipleChoice() {
      return this.selectedQuestionTypeName === MULTIPLE_CHOICE
    },
    isQuestionTypeSelected() {
      return this.questionTypeId !== 0
    },
    correctAnswerHint() {
      if (!this.isQuestionTypeSelected) {
        return 'Vali enne küsimuse tüüp, siis saad märkida õige vastuse'
      }
      return this.isMultipleChoice ? 'Märgi kõik õiged vastused' : 'Märgi õige vastus'
    },
  },

  methods: {
    getCompetences() {
      CompetenceService.getCompetencesRequest()
        .then((response) => (this.competences = response.data))
        .catch()
    },
    getQuestionTypes() {
      QuestionTypeService.getQuestionTypesRequest()
        .then((response) => (this.questionTypes = response.data))
        .catch()
    },
    getCompetenceLevels() {
      this.competenceLevelId = 0
      this.competenceLevels = []

      CompetenceLevelService.getCompetenceLevelsRequest(this.competenceId)
        .then((response) => (this.competenceLevels = response.data))
        .catch()
    },

    changeQuestionType() {
      if (this.isTrueFalse) {
        this.answers = [this.createAnswer('Tõene'), this.createAnswer('Väär')]
      } else if (this.hasTrueFalseAnswers()) {
        this.answers = [this.createAnswer(''), this.createAnswer('')]
      } else {
        this.answers.forEach((answer) => (answer.isCorrect = false))
      }
    },
    hasTrueFalseAnswers() {
      return (
        this.answers.length === 2 &&
        this.answers[0].answerText === 'Tõene' &&
        this.answers[1].answerText === 'Väär'
      )
    },

    createAnswer(answerText) {
      const answer = { key: this.nextAnswerKey, answerText: answerText, isCorrect: false }
      this.nextAnswerKey++
      return answer
    },
    addAnswerRow() {
      this.answers.push(this.createAnswer(''))
    },
    removeAnswerRow(index) {
      this.answers.splice(index, 1)
    },
    toggleCorrectAnswer(index) {
      if (!this.isQuestionTypeSelected) {
        return
      }
      if (this.isMultipleChoice) {
        this.answers[index].isCorrect = !this.answers[index].isCorrect
      } else {
        this.answers.forEach((answer, i) => (answer.isCorrect = i === index))
      }
    },

    createQuestion() {
      this.resetErrorMessage()
      this.checkQuestionFormForErrors()

      if (this.errorMessageIsEmpty()) {
        const questionCreateRequest = {
          competenceLevelId: this.competenceLevelId,
          title: this.title,
          description: this.description,
          questionTypeId: this.questionTypeId,
          score: this.score,
          answers: this.answers.map((answer) => ({
            answerText: answer.answerText,
            isCorrect: answer.isCorrect,
          })),
        }

        QuestionService.postNewQuestion(questionCreateRequest)
          .then((response) => this.handleCreateQuestionResponse(response))
          .catch((error) => this.handleCreateQuestionError(error))
      }
    },
    handleCreateQuestionResponse(response) {
      this.$emit('event-question-created', { questionId: response.data, title: this.title })
    },
    handleCreateQuestionError(error) {
      this.errorMessage = error.response?.data?.message ?? 'Küsimuse loomine ebaõnnestus'
    },

    checkQuestionFormForErrors() {
      if (this.title.trim() === '') {
        this.errorMessage = 'Lisa küsimusele pealkiri'
      } else if (this.description.trim() === '') {
        this.errorMessage = 'Lisa küsimusele kirjeldus'
      } else if (this.competenceId === 0) {
        this.errorMessage = 'Vali küsimusele kompetents'
      } else if (this.competenceLevelId === 0) {
        this.errorMessage = 'Vali küsimusele tase'
      } else if (this.questionTypeId === 0) {
        this.errorMessage = 'Vali küsimuse tüüp'
      } else if (!(this.score > 0)) {
        this.errorMessage = 'Lisa küsimusele punktid (vähemalt 1)'
      } else if (this.answers.some((answer) => answer.answerText.trim() === '')) {
        this.errorMessage = 'Täida kõik vastusevariandid'
      } else if (!this.answers.some((answer) => answer.isCorrect)) {
        this.errorMessage = 'Märgi õige vastus'
      }
    },

    cancel() {
      this.$emit('event-cancel')
    },
    errorMessageIsEmpty() {
      return this.errorMessage === ''
    },
    resetErrorMessage() {
      this.errorMessage = ''
    },
  },
}
</script>

<template>
  <form class="question-create-form" @submit.prevent="createQuestion">
    <div class="mb-3">
      <label for="inputQuestionTitle" class="form-label">Pealkiri</label>
      <input v-model="title"
        type="text"
        class="form-control"
        maxlength="100"
        placeholder="Sisesta küsimus"
      />
    </div>

    <div class="mb-3">
      <label for="inputQuestionDescription" class="form-label">Kirjeldus</label>
      <textarea
        id="inputQuestionDescription"
        v-model="description"
        class="form-control"
        rows="3"
        maxlength="1000"
        placeholder="Täpsusta küsimust või anna vastajale juhis"
      ></textarea>
    </div>

    <div class="row g-3 mb-3">
      <div class="col-sm-6">
        <label for="selectQuestionCompetence" class="form-label">Kompetents</label>
        <select
          id="selectQuestionCompetence"
          v-model="competenceId"
          class="form-select"
          @change="getCompetenceLevels"
        >
          <option :value="0" disabled>Vali kompetents</option>
          <option
            v-for="competence in competences"
            :key="competence.competenceId"
            :value="competence.competenceId"
          >
            {{ competence.competenceName }}
          </option>
        </select>
      </div>
      <div class="col-sm-6">
        <label for="selectQuestionLevel" class="form-label">Tase</label>
        <select
          id="selectQuestionLevel"
          v-model="competenceLevelId"
          class="form-select"
          :disabled="competenceId === 0"
        >
          <option :value="0" disabled>
            {{ competenceId === 0 ? 'Vali enne kompetents': 'Vali tase' }}
          </option>
          <option
            v-for="competenceLevel in competenceLevels"
            :key="competenceLevel.competenceLevelId"
            :value="competenceLevel.competenceLevelId"
          >
            {{ competenceLevel.levelName }}
          </option>
        </select>
      </div>
    </div>

    <div class="row g-3 mb-4">
      <div class="col-sm-8">
        <label for="selectQuestionType" class="form-label">Küsimuse tüüp</label>
        <select
          id="selectQuestionType"
          v-model="questionTypeId"
          class="form-select"
          @change="changeQuestionType"
        >
          <option :value="0" disabled>Vali tüüp</option>
          <option
            v-for="questionType in questionTypes"
            :key="questionType.questionTypeId"
            :value="questionType.questionTypeId"
          >
            {{ questionTypeLabels[questionType.questionTypeName] ?? questionType.questionTypeName }}
          </option>
        </select>
      </div>
      <div class="col-sm-4">
        <label for="inputQuestionScore" class="form-label">Punktid</label>
        <input
          id="inputQuestionScore"
          v-model.number="score"
          type="number"
          min="1"
          step="1"
          class="form-control"
          placeholder="nt 10"
        />
      </div>
    </div>

    <fieldset class="mb-3">
      <legend class="form-label mb-1">Vastusevariandid</legend>
      <p class="answer-hint mb-2">{{ correctAnswerHint }}</p>

      <div class="d-flex flex-column gap-2">
        <div
          v-for="(answer, index) in answers"
          :key="answer.key"
          class="answer-option d-flex align-items-center gap-2 rounded-3 ps-3 pe-2 py-2"
          :class="{ correct: answer.isCorrect, disabled: !isQuestionTypeSelected }"
        >
          <input
            :id="'checkCorrectAnswer' + answer.key"
            class="form-check-input flex-shrink-0 m-0"
            :type="isMultipleChoice ? 'checkbox' : 'radio'"
            name="correctAnswer"
            :checked="answer.isCorrect"
            :disabled="!isQuestionTypeSelected"
            :aria-label="'Õige vastus: variant ' + (index + 1)"
            @change="toggleCorrectAnswer(index)"
          />
          <input
            v-model="answer.answerText"
            type="text"
            class="form-control answer-input"
            :readonly="isTrueFalse"
            :placeholder="'Variant ' + (index + 1)"
            :aria-label="'Vastusevariant ' + (index + 1)"
          />
          <span v-if="answer.isCorrect" class="correct-label flex-shrink-0">Õige</span>
          <button
            v-if="!isTrueFalse && answers.length > 2"
            type="button"
            class="btn btn-sm remove-button flex-shrink-0"
            title="Eemalda variant"
            @click="removeAnswerRow(index)"
          >
            <PhX :size="16" />
          </button>
        </div>
      </div>

      <button
        v-if="!isTrueFalse"
        type="button"
        class="btn add-answer-button w-100 mt-2 d-flex align-items-center justify-content-center gap-2"
        @click="addAnswerRow"
      >
        <PhPlus :size="16" weight="bold" />
        <span>Lisa variant</span>
      </button>
    </fieldset>

    <AlertDanger :error-message="errorMessage" />

    <div class="d-flex justify-content-end gap-2 pt-3">
      <button type="button" class="btn btn-light rounded-3 px-4" @click="cancel">Tühista</button>
      <button type="submit" class="btn btn-primary rounded-3 px-4 fw-bold">Loo küsimus</button>
    </div>
  </form>
</template>

<style scoped>
.form-label {
  font-size: 0.875rem;
  font-weight: 600;
  color: var(--bs-primary);
}

/* Heledad väljad nagu kujunduses; fookuses muutuvad valgeks */
.question-create-form .form-control,
.question-create-form .form-select {
  background-color: var(--bs-gray-100);
  border-color: var(--bs-gray-300);
  border-radius: 0.75rem;
  padding: 0.6rem 0.9rem;
}

.question-create-form .form-select {
  padding-right: 2.25rem;
}

.question-create-form .form-control:focus,
.question-create-form .form-select:focus {
  background-color: var(--bs-body-bg);
}

.question-create-form .form-select:disabled {
  color: var(--bs-gray-500);
}

.answer-hint {
  font-size: 0.8125rem;
  color: var(--bs-secondary-color);
}

/* Vastusevariandi kast; õige vastus saab sama rohelise tooni nagu küsimuste pangas */
.answer-option {
  border: 1px solid var(--bs-gray-300);
  background-color: var(--bs-gray-100);
  transition:
    background-color 0.15s ease,
    border-color 0.15s ease;
}

.answer-option.correct {
  border-color: var(--bs-success);
  background-color: var(--bs-success-bg-subtle);
  box-shadow: 0 0 0 1px var(--bs-success);
}

.answer-option .form-check-input {
  width: 1.25rem;
  height: 1.25rem;
  cursor: pointer;
}

.answer-option.disabled .form-check-input {
  cursor: not-allowed;
}

.answer-option.correct .form-check-input:checked {
  background-color: var(--bs-success);
  border-color: var(--bs-success);
}

/* Variandi tekstiväli sulandub kasti sisse */
.question-create-form .answer-input {
  background-color: transparent;
  border-color: transparent;
  padding: 0.35rem 0.5rem;
}

.question-create-form .answer-input:focus {
  background-color: var(--bs-body-bg);
  border-color: var(--bs-gray-300);
}

.question-create-form .answer-input[readonly] {
  cursor: default;
  box-shadow: none;
}

.correct-label {
  font-size: 0.75rem;
  font-weight: 600;
  color: var(--bs-success-text-emphasis);
}

.remove-button {
  color: var(--bs-gray-600);
  border: none;
}

.remove-button:hover {
  color: var(--bs-danger);
  background-color: var(--bs-danger-bg-subtle);
}

.add-answer-button {
  border: 1px dashed var(--bs-gray-500);
  border-radius: 0.75rem;
  color: var(--bs-primary);
  font-size: 0.875rem;
  font-weight: 600;
}

.add-answer-button:hover {
  border-color: var(--bs-primary);
  background-color: var(--bs-primary-bg-subtle);
}
</style>
