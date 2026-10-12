<script>
import TestService from '@/services/TestService.js'
import NavigationService from '@/services/NavigationService.js'
import LoadingText from '@/components/LoadingText.vue'
import TestNotFoundIllustration from '@/components/TestNotFoundIllustration.vue'
import {
  PhClock,
  PhHouse,
  PhListChecks,
  PhPlant,
  PhSkull,
  PhTarget,
  PhTrophy,
} from '@phosphor-icons/vue'

const STATUS_PASSED = 'P'
const LOW_SCORE_PERCENTAGE = 30
const CONFETTI_COLORS = ['#00b894', '#92e3a9', '#1f8a4c', '#ffd166', '#ff8fab', '#74c0fc']
const CONFETTI_PIECE_COUNT = 90
const CONFETTI_DURATION_MS = 6000

export default {
  name: 'TestResultView',
  components: {
    TestNotFoundIllustration,
    LoadingText,
    PhTrophy,
    PhPlant,
    PhSkull,
    PhTarget,
    PhListChecks,
    PhClock,
    PhHouse,
  },

  data() {
    return {
      isLoading: true,
      userTestId: Number(this.$route.query.userTestId),
      confettiPieces: [],
      errorResponse: {
        message: '',
        errorCode: '',
      },
      errorMessage: '',
      result: {
        id: 0,
        userTestId: 0,
        userTestOpensAt: '',
        userTestCreatedAt: '',
        status: '',
        maxScore: '',
        scoreTotal: '',
        completedAt: '',
        startedAt: '',
        totalQuestions: 0,
        questionsAnswered: 0,
        userAchievedScorePercentage: 0,
      },
    }
  },
  computed: {
    isPassed() {
      return this.result.status === STATUS_PASSED
    },
    isLowScore() {
      return this.scorePercentage < LOW_SCORE_PERCENTAGE
    },
    scorePercentage() {
      return Number(this.result.userAchievedScorePercentage)
    },
    ringPercentage() {
      return Math.min(Math.max(this.scorePercentage, 0), 100)
    },
    timeSpent() {
      const milliseconds = new Date(this.result.completedAt) - new Date(this.result.startedAt)
      if (Number.isNaN(milliseconds) || milliseconds < 0) {
        return '–'
      }
      const totalSeconds = Math.round(milliseconds / 1000)
      const minutes = Math.floor(totalSeconds / 60)
      const seconds = totalSeconds % 60
      return minutes > 0 ? `${minutes} min ${seconds} s` : `${seconds} s`
    },
    completedAtFormatted() {
      if (!this.result.completedAt) {
        return ''
      }
      return new Date(this.result.completedAt).toLocaleString('et-EE', {
        dateStyle: 'medium',
        timeStyle: 'short',
      })
    },
  },
  methods: {
    getTestResult() {
      TestService.getTestResult(this.userTestId)
        .then((response) => this.handleResultResponse(response))
        .catch((error) => this.handleResultErrorResponse(error))
        .finally(() => (this.isLoading = false))
    },
    handleResultResponse(response) {
      this.result = response.data
      if (this.isPassed) {
        this.launchConfetti()
      }
    },
    handleResultErrorResponse(error) {
      this.errorResponse = error.response.data

      console.log(error.response)

      if (error.response.status === 403 && this.errorResponse.errorCode === 'NO_RESULT_FOUND') {
        this.errorMessage = 'Tulemust ei leitud'
      }
    },
    launchConfetti() {
      const prefersReducedMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches
      if (prefersReducedMotion) {
        return
      }
      this.confettiPieces = Array.from({ length: CONFETTI_PIECE_COUNT }, (_, index) =>
        this.createConfettiPiece(index),
      )
      setTimeout(() => (this.confettiPieces = []), CONFETTI_DURATION_MS)
    },
    createConfettiPiece(index) {
      const color = CONFETTI_COLORS[index % CONFETTI_COLORS.length]
      return {
        id: index,
        style: {
          left: `${Math.random() * 100}%`,
          backgroundColor: color,
          width: `${6 + Math.random() * 6}px`,
          height: `${10 + Math.random() * 8}px`,
          borderRadius: index % 3 === 0 ? '50%' : '2px',
          animationDelay: `${Math.random() * 1.2}s`,
          animationDuration: `${2.8 + Math.random() * 2}s`,
          '--confetti-drift': `${(Math.random() - 0.5) * 200}px`,
          '--confetti-spin': `${360 + Math.random() * 720}deg`,
        },
      }
    },
    goToTestsView() {
      NavigationService.navigateToTestsView()
    },
  },

  beforeMount() {
    this.getTestResult()
  },
}
</script>

<template>
  <div class="container d-flex flex-column align-items-center">
    <div v-if="confettiPieces.length" class="confetti" aria-hidden="true">
      <span
        v-for="piece in confettiPieces"
        :key="piece.id"
        class="confetti-piece"
        :style="piece.style"
      ></span>
    </div>

    <LoadingText v-if="isLoading" />

    <TestNotFoundIllustration v-else-if="errorMessage.length" :message="errorMessage" />

    <div v-else class="card my-4 app-surface result-card" :class="isPassed ? 'is-passed' : 'is-failed'">
      <div class="result-header text-center px-4 pt-4 pb-5">
        <div class="header-icon mx-auto mb-3">
          <PhTrophy v-if="isPassed" :size="32" weight="fill" />
          <PhSkull v-else-if="isLowScore" :size="32" weight="fill" />
          <PhPlant v-else :size="32" weight="fill" class="text-success" />
        </div>
        <p class="eyebrow mb-1">Testi tulemus</p>
        <h1 class="h3 fw-bold mb-1">
          <span v-if="isPassed">Palju õnne! Test on sooritatud</span>
          <span v-else>Seekord ei õnnestunud</span>
        </h1>
        <p class="mb-0 header-subtitle">
          <span v-if="isPassed">Suurepärane töö — sinu vastused on salvestatud.</span>
          <span v-else-if="isLowScore">Mine koju ära 🙂</span>
          <span v-else>Sinu vastused on salvestatud. Iga katse viib sind sammu edasi!</span>
        </p>
      </div>

      <div class="card-body px-4 pb-4 pt-0">
        <div class="score-ring mx-auto">
          <div class="ring ring-track"></div>
          <div class="ring ring-progress" :style="{ '--ring-percentage': ringPercentage }"></div>
          <div class="score-ring-label">
            <span class="score-value">{{ scorePercentage }}%</span>
            <span
              class="badge rounded-pill mt-1"
              :class="isPassed ? 'badge-active' : 'badge-inactive'"
            >
              {{ isPassed ? 'Sooritatud' : 'Sooritamata' }}
            </span>
          </div>
        </div>

        <div class="row g-3 my-4">
          <div class="col-12 col-sm-4">
            <div class="stat-box rounded-3 p-3 h-100 text-center">
              <PhTarget :size="24" class="stat-icon mb-1" />
              <div class="fw-semibold">{{ result.scoreTotal }} / {{ result.maxScore }}</div>
              <small class="text-body-secondary">Punkti</small>
            </div>
          </div>
          <div class="col-12 col-sm-4">
            <div class="stat-box rounded-3 p-3 h-100 text-center">
              <PhListChecks :size="24" class="stat-icon mb-1" />
              <div class="fw-semibold">
                {{ result.questionsAnswered }} / {{ result.totalQuestions }}
              </div>
              <small class="text-body-secondary">Vastatud küsimust</small>
            </div>
          </div>
          <div class="col-12 col-sm-4">
            <div class="stat-box rounded-3 p-3 h-100 text-center">
              <PhClock :size="24" class="stat-icon mb-1" />
              <div class="fw-semibold">{{ timeSpent }}</div>
              <small class="text-body-secondary">Aega kulus</small>
            </div>
          </div>
        </div>

        <p v-if="completedAtFormatted" class="text-center text-body-secondary small mb-4">
          Lõpetatud {{ completedAtFormatted }}
        </p>

        <button
          @click="goToTestsView"
          class="btn btn-lg w-100 d-flex align-items-center justify-content-center gap-2"
          :class="isPassed ? 'btn-success' : 'btn-primary'"
        >
          <PhHouse :size="20" weight="fill" />
          Tagasi testide lehele
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.result-card {
  --result-accent: var(--bs-primary);
  --result-accent-rgb: var(--bs-primary-rgb);
  width: 100%;
  max-width: 36rem;
  border: none;
  border-radius: 1rem;
  overflow: hidden;
}

.result-card.is-passed {
  --result-accent: var(--bs-success);
  --result-accent-rgb: var(--bs-success-rgb);
}

/* Päis: eduka sooritamise korral rõõmsam münt-türkiis üleminek, muidu rahulik hall */
.result-header {
  background: var(--app-result-header-bg);
  color: var(--bs-primary-text-emphasis);
}

.is-passed .result-header {
  background: linear-gradient(160deg, #00b894 0%, #4fd1a5 55%, var(--brand-mint) 100%);
  color: #fff;
}

.header-icon {
  width: 3.5rem;
  height: 3.5rem;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background-color: rgba(var(--bs-primary-rgb), 0.1);
  color: var(--bs-primary);
}

.is-passed .header-icon {
  background-color: rgba(255, 255, 255, 0.25);
  color: #fff;
  animation: pop-in 0.6s cubic-bezier(0.34, 1.56, 0.64, 1) both;
}

.header-subtitle {
  opacity: 0.85;
}

.eyebrow {
  font-size: 0.75rem;
  font-weight: 600;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  opacity: 0.8;
}

/* Tulemuse ring kattub osaliselt päisega */
.score-ring {
  position: relative;
  width: 10rem;
  height: 10rem;
  margin-top: -3rem;
  background-color: var(--bs-body-bg);
  border-radius: 50%;
  box-shadow: 0 0.5rem 1.5rem rgba(var(--bs-primary-rgb), 0.12);
}

/* Registreeritud muutuja, et brauser oskaks protsenti animeerida (0 → tulemus) */
@property --ring-percentage {
  syntax: '<number>';
  inherits: false;
  initial-value: 0;
}

/* Ringi kuju tehakse maskiga: radial-gradient lõikab keskelt augu välja */
.ring {
  --ring-shape: radial-gradient(
    farthest-side,
    transparent calc(100% - 11px),
    #000 calc(100% - 10px)
  );
  position: absolute;
  inset: 8px;
  border-radius: 50%;
  -webkit-mask: var(--ring-shape);
  mask: var(--ring-shape);
}

.ring-track {
  background-color: var(--app-ring-track);
}

/* Gradient kulgeb üle kogu ringi tumehallist mündiroheliseks, teine mask näitab sellest
   ainult osa kuni tulemuse protsendini — mida suurem tulemus, seda heledamaks ring läheb */
.ring-progress {
  --ring-reveal: conic-gradient(#000 calc(var(--ring-percentage) * 1%), transparent 0);
  background: conic-gradient(var(--brand-slate), var(--brand-mint));
  -webkit-mask: var(--ring-shape), var(--ring-reveal);
  -webkit-mask-composite: source-in;
  mask: var(--ring-shape), var(--ring-reveal);
  mask-composite: intersect;
  animation: ring-fill 1.2s ease-out both;
}

.score-ring-label {
  position: absolute;
  inset: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.score-value {
  font-size: 2rem;
  font-weight: 700;
  line-height: 1;
  color: var(--result-accent);
}

.badge {
  font-weight: 500;
  padding: 0.35em 0.8em;
}

.stat-box {
  background-color: rgba(var(--result-accent-rgb), 0.06);
}

.stat-icon {
  color: var(--result-accent);
}

/* Konfetti */
.confetti {
  position: fixed;
  inset: 0;
  overflow: hidden;
  pointer-events: none;
  z-index: 1050;
}

/* Vaikeväärtused — iga tüki juhuslikud väärtused tulevad createConfettiPiece() kaudu */
.confetti-piece {
  --confetti-drift: 0px;
  --confetti-spin: 360deg;
  position: absolute;
  top: -2rem;
  opacity: 0;
  animation-name: confetti-fall;
  animation-timing-function: cubic-bezier(0.25, 0.6, 0.5, 1);
  animation-fill-mode: forwards;
}

@keyframes confetti-fall {
  0% {
    opacity: 1;
    transform: translate3d(0, 0, 0) rotate(0deg);
  }
  85% {
    opacity: 1;
  }
  100% {
    opacity: 0;
    transform: translate3d(var(--confetti-drift), 105vh, 0) rotate(var(--confetti-spin));
  }
}

@keyframes ring-fill {
  from {
    --ring-percentage: 0;
  }
}

@keyframes pop-in {
  from {
    opacity: 0;
    transform: scale(0.4);
  }
}

@media (prefers-reduced-motion: reduce) {
  .ring-progress,
  .is-passed .header-icon {
    animation: none;
  }
}
</style>
