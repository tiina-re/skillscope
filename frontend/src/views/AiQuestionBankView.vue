<script>
import AiQuestionService from '@/services/AiQuestionService.js'
import LoadingText from '@/components/LoadingText.vue'
import QuestionBankTabs from '@/components/QuestionBankTabs.vue'
import QuestionBankCard from '@/components/QuestionBankCard.vue'
import QuestionAnswerOption from '@/components/QuestionAnswerOption.vue'
import StarRating from '@/components/StarRating.vue'
import SuccessToast from '@/components/SuccessToast.vue'
import AlertDanger from '@/components/AlertDanger.vue'
import { PhCaretDown, PhCheck, PhClipboardText, PhSparkle, PhX } from '@phosphor-icons/vue'

export default {
  name: 'AiQuestionBankView',
  components: {
    LoadingText,
    QuestionBankTabs,
    QuestionBankCard,
    QuestionAnswerOption,
    StarRating,
    SuccessToast,
    AlertDanger,
    PhCaretDown,
    PhCheck,
    PhClipboardText,
    PhSparkle,
    PhX,
  },

  beforeMount() {
    this.getAiQuestions()
  },

  data() {
    return {
      isLoading: true,
      selectedStatus: 'P',
      questionToReview: null,
      isReviewing: false,
      reviewErrorMessage: '',
      successMessage: '',
      aiQuestionReviewRequestDto: {
        score: null,
        feedback: '',
        approved: null,
      },
      // ai_question staatused: P = ootab ülevaatust, A = kinnitatud (kopeeritud panka), R = tagasi lükatud
      aiQuestionStatuses: [
        { code: 'P', name: 'Ootab ülevaatust', badgeClass: 'badge-in-progress' },
        { code: 'A', name: 'Kinnitatud', badgeClass: 'badge-active' },
        { code: 'R', name: 'Tagasi lükatud', badgeClass: 'badge-inactive' },
      ],
      questionTypeLabels: {
        SINGLE_CHOICE: 'Ühe õige vastusega',
        MULTIPLE_CHOICE: 'Mitme õige vastusega',
        TRUE_FALSE: 'Tõene või väär',
      },
      aiQuestions: [
        {
          aiQuestionId: 0,
          questionTitle: '',
          questionDescription: '',
          questionTypeName: '',
          competenceId: 0,
          competenceName: '',
          competenceLevelName: '',
          score: null,
          aiQuestionStatus: '',
          feedback: null,
          createdAt: '',
          answers: [
            {
              aiQuestionAnswerId: 0,
              answerText: '',
              correctChoice: false,
            },
          ],
        },
      ],
    }
  },

  computed: {
    selectedStatusName() {
      const selectedStatus = this.getAiQuestionStatusBy(this.selectedStatus)
      return selectedStatus ? selectedStatus.name : 'Kõik staatused'
    },
  },

  methods: {
    getAiQuestions() {
      AiQuestionService.getAiQuestionsRequest(this.selectedStatus)
        .then((response) => this.handleGetAiQuestions(response))
        .catch()
        .finally(() => (this.isLoading = false))
    },
    handleGetAiQuestions(response) {
      this.aiQuestions = response.data
    },
    selectStatus(status) {
      this.selectedStatus = status
      this.getAiQuestions()
    },
    getAiQuestionStatusBy(code) {
      return this.aiQuestionStatuses.find((status) => status.code === code)
    },
    openReviewForm(aiQuestion) {
      this.successMessage = ''
      this.reviewErrorMessage = ''
      this.questionToReview = aiQuestion
      this.aiQuestionReviewRequestDto = { score: null, feedback: '', approved: null }
    },
    closeReviewForm() {
      this.questionToReview = null
    },
    reviewAiQuestion(approved) {
      if (this.aiQuestionReviewRequestDto.score === null) {
        this.reviewErrorMessage = 'Vali hinnang 1–5 tärni'
        return
      }
      this.aiQuestionReviewRequestDto.approved = approved
      this.isReviewing = true
      AiQuestionService.reviewAiQuestionRequest(
        this.questionToReview.aiQuestionId,
        this.aiQuestionReviewRequestDto,
      )
        .then(() => this.handleReviewAiQuestion(approved))
        .catch(
          () => (this.reviewErrorMessage = 'Ülevaatuse salvestamine ebaõnnestus. Proovi uuesti.'),
        )
        .finally(() => (this.isReviewing = false))
    },
    handleReviewAiQuestion(approved) {
      this.successMessage = approved
        ? `Küsimus "${this.questionToReview.questionTitle}" on kinnitatud ja lisatud küsimuste panka`
        : `Küsimus "${this.questionToReview.questionTitle}" on tagasi lükatud`
      this.closeReviewForm()
      this.getAiQuestions()
      this.$refs.questionBankTabs.getPendingAiQuestionCount()
    },
  },
}
</script>

<template>
  <div class="container-fluid py-4">
    <LoadingText v-if="isLoading" />
    <div v-else class="question-bank-content mx-auto">
      <div class="d-flex flex-wrap align-items-center justify-content-between gap-3 mb-4">
        <h1 class="text-uppercase fw-bold h3 mb-0">Küsimuste pank</h1>
        <div class="dropdown">
          <button
            class="btn btn-light border fw-bold rounded-2 d-flex align-items-center gap-2"
            data-bs-toggle="dropdown"
            aria-expanded="false"
          >
            <span>{{ selectedStatusName }}</span>
            <PhCaretDown :size="14" />
          </button>
          <ul class="dropdown-menu dropdown-menu-custom dropdown-menu-end">
            <li v-for="status in aiQuestionStatuses" :key="status.code">
              <button
                class="dropdown-item"
                :class="{ active: selectedStatus === status.code }"
                @click="selectStatus(status.code)"
              >
                {{ status.name }}
              </button>
            </li>
            <li><hr class="dropdown-divider" /></li>
            <li>
              <button
                class="dropdown-item"
                :class="{ active: selectedStatus === null }"
                @click="selectStatus(null)"
              >
                Kõik staatused
              </button>
            </li>
          </ul>
        </div>
      </div>

      <QuestionBankTabs ref="questionBankTabs" />

      <div v-if="!aiQuestions.length" class="card app-surface rounded-4 p-5 text-center">
        <PhSparkle :size="40" class="empty-icon mx-auto mb-3" />
        <h2 class="h5 fw-bold mb-2">AI küsimusi ei leitud</h2>
        <p class="text-secondary mb-0">
          AI chatiga loodud ja salvestatud küsimused ilmuvad siia ülevaatamiseks.
        </p>
      </div>

      <div class="d-flex flex-column gap-4">
        <QuestionBankCard
          v-for="aiQuestion in aiQuestions"
          :key="aiQuestion.aiQuestionId"
          :status-badge="getAiQuestionStatusBy(aiQuestion.aiQuestionStatus)"
          :competence-name="aiQuestion.competenceName"
          :question-type-label="questionTypeLabels[aiQuestion.questionTypeName]"
          :competence-level-name="aiQuestion.competenceLevelName"
        >
          <template #title>{{ aiQuestion.questionTitle }}</template>
          <template v-if="aiQuestion.aiQuestionStatus === 'P'" #menu>
            <li>
              <button
                class="dropdown-item d-flex align-items-center gap-2"
                @click="openReviewForm(aiQuestion)"
              >
                <PhClipboardText :size="16" />
                Vaata üle
              </button>
            </li>
          </template>
          <template #description>
            {{ aiQuestion.questionDescription }}
            <span v-if="aiQuestion.score" class="review-summary d-block mt-3 p-3 rounded-3">
              <span class="d-flex align-items-center gap-2 fw-semibold">
                Hinnang:
                <StarRating :model-value="aiQuestion.score" readonly :size="16" />
              </span>
              <span v-if="aiQuestion.feedback" class="d-block mt-1 text-secondary">
                {{ aiQuestion.feedback }}
              </span>
            </span>
          </template>
          <template #answers>
            <QuestionAnswerOption
              v-for="answer in aiQuestion.answers"
              :key="answer.aiQuestionAnswerId"
              :answer-text="answer.answerText"
              :correct-choice="answer.correctChoice"
            />
          </template>
        </QuestionBankCard>
      </div>
    </div>

    <template v-if="questionToReview">
      <div class="modal d-block" tabindex="-1" role="dialog" aria-modal="true">
        <div class="modal-dialog modal-dialog-centered">
          <div class="modal-content rounded-4 border-0 shadow">
            <div class="modal-body p-4">
              <h5 class="fw-bold mb-3">Vaata AI küsimus üle</h5>
              <p class="mb-4">
                <strong>{{ questionToReview.questionTitle }}</strong>
              </p>
              <AlertDanger :error-message="reviewErrorMessage" class="mb-3" />

              <div class="mb-3">
                <span class="form-label fw-semibold d-block">Hinnang</span>
                <StarRating v-model="aiQuestionReviewRequestDto.score" />
              </div>

              <div class="mb-3">
                <label for="review-feedback" class="form-label fw-semibold">Tagasiside</label>
                <textarea
                  id="review-feedback"
                  v-model="aiQuestionReviewRequestDto.feedback"
                  class="form-control"
                  rows="3"
                  maxlength="255"
                  placeholder="Mis oli hea, mida võiks parandada?"
                ></textarea>
                <div class="form-text text-end">
                  {{ aiQuestionReviewRequestDto.feedback.length }} / 255
                </div>
              </div>

              <p class="text-secondary small mb-0">
                Kinnitamisel lisatakse küsimus koos vastusevariantidega küsimuste panka.
              </p>
            </div>
            <div class="modal-footer border-0 px-4 pb-4 pt-0 gap-2">
              <button
                type="button"
                class="btn btn-light fw-bold rounded-2 me-auto"
                :disabled="isReviewing"
                @click="closeReviewForm"
              >
                Tühista
              </button>
              <button
                type="button"
                class="btn btn-outline-danger fw-bold rounded-2 d-flex align-items-center gap-2"
                :disabled="isReviewing"
                @click="reviewAiQuestion(false)"
              >
                <PhX :size="16" weight="bold" />
                Lükka tagasi
              </button>
              <button
                type="button"
                class="btn btn-primary fw-bold rounded-2 d-flex align-items-center gap-2"
                :disabled="isReviewing"
                @click="reviewAiQuestion(true)"
              >
                <PhCheck :size="16" weight="bold" />
                Kinnita
              </button>
            </div>
          </div>
        </div>
      </div>
      <div class="modal-backdrop fade show"></div>
    </template>

    <SuccessToast :success-message="successMessage" @event-close="successMessage = ''" />
  </div>
</template>

<style scoped>
@media (min-width: 992px) {
  .question-bank-content {
    width: 70%;
  }
}

.review-summary {
  background-color: var(--bs-gray-100);
}

.empty-icon {
  color: var(--bs-primary);
}

.dropdown-menu-custom {
  --bs-dropdown-min-width: 12rem;
  --bs-dropdown-bg: var(--bs-gray-100);
  --bs-dropdown-border-radius: 0.75rem;
  --bs-dropdown-link-color: var(--bs-emphasis-color);
}
</style>
