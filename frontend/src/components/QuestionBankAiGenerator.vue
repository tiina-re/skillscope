<script>
import AiQuestionService from '@/services/AiQuestionService.js'
import CompetenceService from '@/services/CompetenceService.js'
import CompetenceLevelService from '@/services/CompetenceLevelService.js'
import QuestionTypeService from '@/services/QuestionTypeService.js'
import QuestionBankCard from '@/components/QuestionBankCard.vue'
import QuestionAnswerOption from '@/components/QuestionAnswerOption.vue'
import { PhSparkle } from '@phosphor-icons/vue'
import QuestionBankCreateNewConfigurationPanel from '@/components/QuestionBankCreateNewConfigurationPanel.vue'
import QuestionBankPromptInput from '@/components/QuestionBankPromptInput.vue'

const MAX_INPUT_LENGTH = 500

// Backendi veateated, mida võib kasutajale otse näidata
const STATUSES_WITH_USER_MESSAGE = [400, 403]

export default {
  name: 'QuestionBankAiGenerator',
  components: {
    QuestionBankPromptInput,
    QuestionBankCreateNewConfigurationPanel,
    QuestionBankCard,
    QuestionAnswerOption,
    PhSparkle,
  },

  beforeMount() {
    this.getCompetences()
    this.getQuestionTypes()
  },

  data() {
    return {
      maxInputLength: MAX_INPUT_LENGTH,
      questionCountOptions: [1, 2, 3, 4, 5],
      isLoading: false,
      errorMessage: '',

      competences: [],
      competenceLevels: [],
      questionTypes: [],
      competenceId: 0,
      competenceLevelId: 0,
      // null = tüüp pole piiratud, AI valib küsimustele sobivad tüübid ise
      questionTypeId: null,
      questionCount: 3,
      userInput: '',

      // Ekraanil kuvatavad sõnumid; AI sõnumil võivad olla kaasas loodud küsimused
      messages: [],

      questionTypeLabels: {
        SINGLE_CHOICE: 'Ühe õige vastusega',
        MULTIPLE_CHOICE: 'Mitme õige vastusega',
        TRUE_FALSE: 'Tõene või väär',
      },
      questionStatusBadges: {
        saved: { name: 'Salvestatud, ootab ülevaatust', badgeClass: 'badge-active' },
        rejected: { name: 'Ei salvestatud', badgeClass: 'badge-inactive' },
      },
    }
  },

  computed: {
    isCompetenceLevelSelected() {
      return this.competenceLevelId !== 0
    },
    canSend() {
      return this.isCompetenceLevelSelected && !this.isLoading
    },
    selectedCompetenceName() {
      const competence = this.competences.find(
        (competence) => competence.competenceId === this.competenceId,
      )
      return competence ? competence.competenceName : ''
    },
    selectedLevelName() {
      const competenceLevel = this.competenceLevels.find(
        (competenceLevel) => competenceLevel.competenceLevelId === this.competenceLevelId,
      )
      return competenceLevel ? competenceLevel.levelName : ''
    },
    inputPlaceholder() {
      if (!this.isCompetenceLevelSelected) {
        return 'Vali enne kompetents ja tase'
      }
      return `Kirjelda, milliseid küsimusi soovid — nt "massiivide ja tsüklite kohta, üks võiks olla tõene/väär." (Pole kohustuslik)`
    },
  },

  watch: {
    // Kompetentsi valik tuleb paneelist v-modeliga - tasemed laetakse siin, kui väärtus muutub
    competenceId() {
      this.getCompetenceLevels()
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

    sendMessage() {
      if (!this.canSend) {
        return
      }
      const userText = this.userInput.trim()
      this.userInput = ''
      this.errorMessage = ''
      this.addMessage('user', userText)
      this.generateQuestions(userText)
    },

    // Viimane juhis saadetakse eraldi, varasemad sõnumid kaasa, et AI saaks aru täpsustustest
    generateQuestions(userText) {
      this.isLoading = true
      const aiQuestionGenerateRequestDto = {
        competenceId: this.competenceId,
        competenceLevelId: this.competenceLevelId,
        questionTypeId: this.questionTypeId,
        questionCount: this.questionCount,
        instructions: userText,
      }
      AiQuestionService.sendGenerateLevelQuestionsRequest(aiQuestionGenerateRequestDto)
        .then((response) => this.handleGenerateQuestionsResponse(response.data))
        .catch((error) => this.handleRequestError(error))
        .finally(() => (this.isLoading = false))
    },

    handleGenerateQuestionsResponse(generationResponse) {
      const questions = generationResponse.questions.map((question) => ({
        ...question,
        status: 'pending',
      }))
      const text =
        questions.length === 1
          ? 'Koostasin sellise küsimuse. Kas soovid selle ülevaatusele lisada?'
          : `Koostasin ${questions.length} küsimust. Vali, milliseid soovid ülevaatusele lisada.`
      this.addMessage('ai', text, questions)
    },

    addMessage(sender, text, questions = []) {
      this.messages.push({ sender, text, questions })
      this.$nextTick(() => this.scrollToInput())
    },

    scrollToInput() {
      this.$refs.promptInput?.$el?.scrollIntoView({ behavior: 'smooth', block: 'end' })
    },

    saveQuestion(question) {
      question.status = 'saving'
      AiQuestionService.sendSaveQuestionRequest({
        competenceLevelId: question.competenceLevelId,
        questionTypeId: question.questionTypeId,
        title: question.title,
        description: question.description,
        answers: question.answers,
      })
        .then(() => (question.status = 'saved'))
        .catch((error) => this.handleSaveQuestionError(error, question))
    },

    handleSaveQuestionError(error, question) {
      // Lubame uuesti proovida
      question.status = 'pending'
      this.handleRequestError(error)
    },

    rejectQuestion(question) {
      question.status = 'rejected'
    },

    handleRequestError(error) {
      // 401 korral suunab globaalne axios interceptor sisselogimise lehele
      const statusCode = error.response?.status
      if (statusCode === 401) {
        return
      }
      const backendMessage = error.response?.data?.message
      if (STATUSES_WITH_USER_MESSAGE.includes(statusCode) && backendMessage) {
        this.errorMessage = backendMessage
        return
      }
      this.errorMessage = 'Midagi läks valesti. Palun proovi uuesti.'
    },

    handleEnterKey(event) {
      // Enter saadab, Shift+Enter teeb uue rea
      if (!event.shiftKey) {
        event.preventDefault()
        this.sendMessage()
      }
    },
  },
}
</script>

<template>
  <div class="d-flex flex-column gap-4">
    <div class="card app-surface rounded-4">
      <QuestionBankCreateNewConfigurationPanel
        v-model:competence-id="competenceId"
        v-model:competence-level-id="competenceLevelId"
        v-model:question-count="questionCount"
        v-model:question-type-id="questionTypeId"
        :competence-levels="competenceLevels"
        :competences="competences"
        :question-count-options="questionCountOptions"
        :question-type-labels="questionTypeLabels"
        :question-types="questionTypes"
      />
    </div>

    <div v-if="!messages.length" class="card app-surface rounded-4 p-5 text-center">
      <PhSparkle :size="40" class="empty-icon mx-auto mb-3" />
      <h3 class="h5 fw-bold mb-2">Alusta juhisega</h3>
      <p class="text-secondary mb-0">
        Kirjuta allolevasse lahtrisse, millest küsimused võiksid olla. Loodud küsimused saad enne
        salvestamist üle vaadata — salvestatud küsimused ilmuvad vahekaardile „AI küsimused".
      </p>
    </div>

    <template v-for="(message, messageIndex) in messages" :key="messageIndex">
      <div
        class="generator-message d-flex"
        :class="message.sender === 'user' ? 'justify-content-end' : 'justify-content-start'"
      >
        <div class="generator-bubble rounded-4 py-2 px-3" :class="`from-${message.sender}`">
          {{ message.text }}
        </div>
      </div>

      <QuestionBankCard
        v-for="(question, questionIndex) in message.questions"
        :key="`${messageIndex}-${questionIndex}`"
        :status-badge="questionStatusBadges[question.status] ?? null"
        :competence-name="question.competenceName"
        :competence-level-name="question.levelName"
        :question-type-label="
          questionTypeLabels[question.questionTypeName] ?? question.questionTypeName
        "
        is-initially-open
      >
        <template #title>{{ question.title }}</template>
        <template #description>{{ question.description }}</template>
        <template #answers>
          <QuestionAnswerOption
            v-for="(answer, answerIndex) in question.answers"
            :key="answerIndex"
            :answer-text="answer.answerText"
            :correct-choice="answer.isCorrect"
          />
        </template>
        <template v-if="question.status === 'pending' || question.status === 'saving'" #actions>
          <div class="d-flex justify-content-end gap-2">
            <button
              type="button"
              class="btn btn-light fw-bold rounded-2"
              :disabled="question.status === 'saving'"
              @click="rejectQuestion(question)"
            >
              Ei soovi
            </button>
            <button
              type="button"
              class="btn btn-primary fw-bold rounded-2"
              :disabled="question.status === 'saving'"
              @click="saveQuestion(question)"
            >
              {{ question.status === 'saving' ? 'Salvestan…' : 'Lisa ülevaatusele' }}
            </button>
          </div>
        </template>
      </QuestionBankCard>
    </template>

    <div v-if="isLoading" class="generator-message d-flex justify-content-start">
      <div class="generator-bubble from-ai rounded-4 py-2 px-3 text-secondary fst-italic">
        AI mõtleb…
      </div>
    </div>

    <QuestionBankPromptInput
      ref="promptInput"
      v-model="userInput"
      :can-send="canSend"
      :error-message="errorMessage"
      :handle-enter-key="handleEnterKey"
      :input-placeholder="inputPlaceholder"
      :is-competence-level-selected="isCompetenceLevelSelected"
      :max-input-length="maxInputLength"
      :question-count="questionCount"
      :selected-competence-name="selectedCompetenceName"
      :selected-level-name="selectedLevelName"
      :send-message="sendMessage"
    />
  </div>
</template>

<style scoped>
.generator-bubble {
  max-width: 75%;
  white-space: pre-wrap;
  overflow-wrap: anywhere;
}

.generator-bubble.from-user {
  background: var(--brand-slate);
  color: #fff;
}

.generator-bubble.from-ai {
  background: var(--bs-primary-bg-subtle);
}

.empty-icon {
  color: var(--bs-primary);
}

/* Samad mullid nagu QuestionBankCard-is */
</style>
