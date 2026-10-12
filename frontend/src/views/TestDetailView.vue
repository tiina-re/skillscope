<script>
import TestService from '@/services/TestService.js'
import NavigationService from '@/services/NavigationService.js'
import SessionStorageService from '@/services/SessionStorageService.js'
import Status from '@/Status.js'
import LoadingText from '@/components/LoadingText.vue'
import TestNotFoundIllustration from '@/components/TestNotFoundIllustration.vue'
import {
  PhArrowLeft,
  PhCalendarBlank,
  PhInfinity,
  PhInfo,
  PhListNumbers,
  PhTarget,
  PhTimer,
  PhTrophy,
  PhPenNib,
} from '@phosphor-icons/vue'

export default {
  name: 'TestDetailView',
  components: {
    LoadingText,
    TestNotFoundIllustration,
    PhArrowLeft,
    PhCalendarBlank,
    PhInfinity,
    PhInfo,
    PhListNumbers,
    PhTarget,
    PhTimer,
    PhTrophy,
    PhPenNib,
  },
  props: {
    testId: { type: Number, required: true },
  },
  data() {
    return {
      isLoading: true,
      errorMessage: '',
      isInfoOpen: false,
      test: {
        testId: 0,
        title: '',
        shortDescription: '',
        description: '',
        competence: '',
        competenceLevel: '',
        isTimed: false,
        timerMin: null,
        passPercent: 0,
        roundScoreUp: false,
        status: '',
        createdBy: '',
        createdAt: '',
        updatedAt: '',
        questionCount: 0,
        maxScore: 0,
      },
    }
  },
  computed: {
    // Pikk pealkiri suurtähtedes on raskesti loetav — siis tavaline kiri ja väiksem font
    isLongTitle() {
      return this.test.title.length > 60
    },
    // Aktiivse testi staatust ei kuvata (see on näha testide nimekirjas), muud staatused küll
    inactiveStatusBadge() {
      return this.test.status !== 'A' ? Status[this.test.status] : null
    },
    questionCountText() {
      return this.countText(this.test.questionCount, 'küsimus', 'küsimust')
    },
    timerText() {
      return this.countText(this.test.timerMin, 'minut', 'minutit')
    },
    maxScoreText() {
      return this.countText(this.test.maxScore, 'punkt', 'punkti')
    },
    // Backend saadab passPercent'i kujul 60.00 — kuvame ilma tarbetute nullideta
    passPercentText() {
      return `${Number(this.test.passPercent)}%`
    },
    // Loomise aeg ja ümardamise reegel on haldusinfo — tavakasutaja seda ei näe
    canSeeAdminInfo() {
      return SessionStorageService.hasRole(['ADMIN', 'HALDUR'])
    },
    roundingText() {
      return this.test.roundScoreUp ? 'üles' : 'alla'
    },
    createdAtText() {
      return new Date(this.test.createdAt).toLocaleDateString('et-EE')
    },
  },
  methods: {
    getTestDetail() {
      TestService.getTestDetail(this.testId)
        .then((response) => (this.test = response.data))
        .catch((error) => this.handleGetTestDetailErrorResponse(error))
        .finally(() => (this.isLoading = false))
    },
    handleGetTestDetailErrorResponse(error) {
      if (error.response?.status === 403) {
        this.errorMessage = error.response.data.message
      } else if (error.response?.status === 404) {
        this.errorMessage = 'Sellist testi ei leitud'
      } else {
        this.errorMessage = 'Testi andmete laadimine ebaõnnestus'
      }
    },
    // Eesti keeles: 1 küsimus, aga 0 / 2 / 20 küsimust
    countText(count, singular, plural) {
      return `${count} ${count === 1 ? singular : plural}`
    },
    showInfo() {
      this.isInfoOpen = true
    },
    hideInfo() {
      this.isInfoOpen = false
    },
    navigateToTestsView() {
      NavigationService.navigateToTestsView()
    },
  },
  beforeMount() {
    this.getTestDetail()
  },
}
</script>

<template>
  <div class="container d-flex flex-column align-items-center">
    <LoadingText v-if="isLoading" />

    <TestNotFoundIllustration v-else-if="errorMessage.length" :message="errorMessage" />

    <div v-else class="detail-wrapper my-4">
      <button
        class="btn btn-link back-link d-inline-flex align-items-center gap-1 px-0 mb-3"
        @click="navigateToTestsView"
      >
        <PhArrowLeft :size="16" />
        Testid
      </button>

      <div class="card app-surface p-4 detail-card">
        <div class="card-body">
          <p class="eyebrow text-brand mb-2">{{ test.competence }}</p>
          <div class="d-flex align-items-start gap-3 mb-4">
            <h1
              class="test-title fw-bold flex-grow-1 mb-0"
              :class="isLongTitle ? 'test-title-long h4' : 'text-uppercase h3'"
            >
              {{ test.title }}
            </h1>
            <span
              v-if="inactiveStatusBadge"
              class="badge rounded-pill flex-shrink-0 mt-1"
              :class="inactiveStatusBadge.badgeClass"
            >
              {{ inactiveStatusBadge.name }}
            </span>
            <span class="badge rounded-pill text-bg-light border flex-shrink-0 mt-1">
              {{ test.competenceLevel }}
            </span>
          </div>

          <p class="lead text-body-secondary mb-4">{{ test.shortDescription }}</p>

          <div class="stat-grid mb-4">
            <div class="info-box d-flex align-items-center gap-3 rounded-3 p-3">
              <PhListNumbers :size="28" class="text-primary flex-shrink-0" />
              <div>
                <div class="fw-semibold">{{ questionCountText }}</div>
                <small class="text-body-secondary">Küsimusi kokku</small>
              </div>
            </div>

            <div class="info-box d-flex align-items-center gap-3 rounded-3 p-3">
              <PhTimer v-if="test.isTimed" :size="28" class="text-primary flex-shrink-0" />
              <PhInfinity v-else :size="28" class="text-primary flex-shrink-0" />
              <div>
                <div class="fw-semibold">
                  <span v-if="test.isTimed">{{ timerText }}</span>
                  <span v-else>Ajapiirang puudub</span>
                </div>
                <small class="text-body-secondary">Aega läbimiseks</small>
              </div>
            </div>

            <div class="info-box d-flex align-items-center gap-3 rounded-3 p-3">
              <PhTarget :size="28" class="text-primary flex-shrink-0" />
              <div>
                <div class="fw-semibold">{{ passPercentText }}</div>
                <small class="text-body-secondary">Läbimiseks vajalik</small>
              </div>
            </div>

            <div class="info-box d-flex align-items-center gap-3 rounded-3 p-3">
              <PhTrophy :size="28" class="text-primary flex-shrink-0" />
              <div>
                <div class="fw-semibold">{{ maxScoreText }}</div>
                <small class="text-body-secondary">Maksimaalne tulemus</small>
              </div>
            </div>
          </div>

          <hr class="my-4" />

          <h2 class="h6 text-uppercase text-body-secondary fw-semibold mb-2">Testi kirjeldus</h2>
          <p class="description mb-0">{{ test.description }}</p>

          <div v-if="canSeeAdminInfo" class="d-flex justify-content-end mt-3">
            <div class="info-popover" @mouseenter="showInfo" @mouseleave="hideInfo">
              <button
                class="btn btn-sm p-0 info-button"
                :aria-expanded="isInfoOpen"
                aria-label="Testi lisainfo"
                @focus="showInfo"
                @blur="hideInfo"
                @keydown.esc="hideInfo"
              >
                <PhInfo :size="22" />
              </button>
              <div v-if="isInfoOpen" class="info-tooltip app-tooltip" role="tooltip">
                <div class="d-flex align-items-center gap-2 mb-1">
                  <PhPenNib :size="16" class="flex-shrink-0" aria-hidden="true" />
                  <span><span class="visually-hidden">Koostaja: </span>{{ test.createdBy }}</span>
                </div>
                <div class="d-flex align-items-center gap-2 mb-1">
                  <PhCalendarBlank :size="16" class="flex-shrink-0" aria-hidden="true" />
                  <span><span class="visually-hidden">Loodud </span>{{ createdAtText }}</span>
                </div>
                <div>Tulemus ümardatakse {{ roundingText }}</div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.detail-wrapper {
  width: 100%;
  max-width: 48rem;
}

.detail-card {
  border: none;
  border-radius: 1rem;
}

.back-link {
  color: var(--bs-gray-600);
  text-decoration: none;
  font-weight: 500;
}

.back-link:hover {
  color: var(--bs-primary);
  background-color: transparent;
}

.eyebrow {
  font-size: 0.75rem;
  font-weight: 600;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.test-title {
  min-width: 0;
  overflow-wrap: anywhere;
  text-wrap: balance;
}

.test-title-long {
  line-height: 1.4;
}

.lead {
  font-size: 1.125rem;
  line-height: 1.6;
}

.description {
  line-height: 1.7;
  white-space: pre-line;
}

.stat-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(min(14rem, 100%), 1fr));
  gap: 0.75rem;
}

.info-box {
  background-color: rgba(var(--bs-primary-rgb), 0.06);
}

.info-popover {
  position: relative;
}

.info-button {
  color: var(--bs-gray-600);
  line-height: 1;
}

.info-button:hover,
.info-button[aria-expanded='true'] {
  color: var(--bs-primary);
  background-color: transparent;
}

.info-tooltip {
  position: absolute;
  bottom: calc(100% + 0.4rem);
  right: 0;
  max-width: 16rem;
}

.badge {
  font-weight: 500;
  padding: 0.45em 0.9em;
}
</style>
