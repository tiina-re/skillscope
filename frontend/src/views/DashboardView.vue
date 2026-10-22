<script>
import MainTitle from '@/components/MainTitle.vue'
import LoadingText from '@/components/LoadingText.vue'
import BaseModal from '@/components/modal/BaseModal.vue'
import CompetenceScoreBars from '@/components/CompetenceScoreBars.vue'
import DashboardService from '@/services/DashboardService.js'
import {
  PhArrowRight,
  PhClipboardText,
  PhFilePlus,
  PhQuestion,
  PhSparkle,
} from '@phosphor-icons/vue'

export default {
  name: 'DashboardView',
  components: { MainTitle, LoadingText, BaseModal, CompetenceScoreBars, PhArrowRight },
  data() {
    return {
      TOP_COMPETENCE_COUNT: 3,
      isLoading: true,
      isAllScoresModalOpen: false,
      errorMessage: '',
      dashboard: {
        assignedTestCount: 0,
        recentTestCount: 0,
        recentQuestionCount: 0,
        pendingAiQuestionCount: 0,
        recentDays: 7,
        competenceScores: [],
        averageScorePercent: null,
        passedResultCount: 0,
        failedResultCount: 0,
      },
    }
  },
  computed: {
    // Backend saadab kompetentsid parima tulemuse järgi sorteerituna
    topCompetenceScores() {
      return this.dashboard.competenceScores.slice(0, this.TOP_COMPETENCE_COUNT)
    },
    completedResultCount() {
      return this.dashboard.passedResultCount + this.dashboard.failedResultCount
    },
    // Donut-graafiku läbitud osa: SVG ringi pathLength on 100, seega protsent = kaare pikkus
    passedPercent() {
      if (!this.completedResultCount) return 0
      return (this.dashboard.passedResultCount / this.completedResultCount) * 100
    },
    recentPeriodText() {
      return `viimase ${this.dashboard.recentDays} päeva jooksul`
    },
    statCards() {
      return [
        {
          key: 'assignedTests',
          icon: PhClipboardText,
          count: this.dashboard.assignedTestCount,
          label: 'Mulle määratud testid',
          hint: 'Ootavad sooritamist',
          to: { name: 'myTestsRoute' },
        },
        {
          key: 'recentTests',
          icon: PhFilePlus,
          count: this.dashboard.recentTestCount,
          label: 'Uued testid',
          hint: this.recentPeriodText,
          to: { name: 'testsRoute' },
        },
        {
          key: 'recentQuestions',
          icon: PhQuestion,
          count: this.dashboard.recentQuestionCount,
          label: 'Uued küsimused',
          hint: this.recentPeriodText,
          to: { name: 'questionBankView' },
        },
        {
          key: 'pendingAiQuestions',
          icon: PhSparkle,
          count: this.dashboard.pendingAiQuestionCount,
          label: 'AI küsimused ootel',
          hint: 'Ootavad ülevaatust',
          to: { name: 'aiQuestionBankView' },
          // Ootel AI küsimused vajavad tegevust — tõstame kaardi esile, kui neid on
          isHighlighted: this.dashboard.pendingAiQuestionCount > 0,
        },
      ]
    },
  },
  methods: {
    roundPercent(value) {
      return Math.round(value ?? 0)
    },
    getDashboard() {
      DashboardService.getDashboard()
        .then((response) => (this.dashboard = response.data))
        .catch(() => (this.errorMessage = 'Töölaua andmete laadimine ebaõnnestus'))
        .finally(() => (this.isLoading = false))
    },
  },
  beforeMount() {
    this.getDashboard()
  },
}
</script>

<template>
  <div class="container py-4">
    <MainTitle title="Töölaud" />
    <LoadingText v-if="isLoading" />

    <div v-else-if="errorMessage.length" class="alert alert-danger">{{ errorMessage }}</div>

    <div v-else class="dashboard-grid">
      <section class="card app-surface rounded-4 chart-card">
        <div class="card-body p-4 d-flex flex-column">
          <h2 class="h6 fw-bold mb-1">Keskmine tulemus kompetentsi järgi</h2>
          <p class="small text-body-secondary mb-4">Kõigi sooritatud testide põhjal</p>
          <p v-if="!dashboard.competenceScores.length" class="text-body-secondary mb-0">
            Ühtegi testi pole veel sooritatud
          </p>
          <template v-else>
            <CompetenceScoreBars :competence-scores="topCompetenceScores" class="mb-4" />
            <button
              v-if="dashboard.competenceScores.length > TOP_COMPETENCE_COUNT"
              class="btn btn-light fw-bold rounded-2 align-self-start mt-auto"
              @click="isAllScoresModalOpen = true"
            >
              Vaata kõiki
            </button>
          </template>
        </div>
      </section>

      <section class="card app-surface rounded-4 chart-card">
        <div class="card-body p-4 d-flex flex-column">
          <h2 class="h6 fw-bold mb-1">Läbimise määr</h2>
          <p class="small text-body-secondary mb-4">{{ completedResultCount }} sooritatud testi</p>
          <div class="d-flex flex-wrap align-items-center justify-content-center gap-5 flex-grow-1">
            <div class="donut position-relative">
              <svg viewBox="0 0 42 42" class="d-block w-100 h-100" aria-hidden="true">
                <circle class="donut-failed" cx="21" cy="21" r="15.9" pathLength="100" />
                <circle
                  class="donut-passed"
                  cx="21"
                  cy="21"
                  r="15.9"
                  pathLength="100"
                  :stroke-dasharray="`${passedPercent} ${100 - passedPercent}`"
                />
              </svg>
              <div
                class="donut-center position-absolute top-50 start-50 translate-middle text-center"
              >
                <div class="donut-value fw-bold">
                  {{ roundPercent(dashboard.averageScorePercent) }}%
                </div>
                <small class="text-body-secondary">keskmine tulemus</small>
              </div>
            </div>
            <ul class="list-unstyled d-flex flex-column gap-3 mb-0">
              <li class="d-flex align-items-center gap-2">
                <span class="legend-dot legend-passed rounded-circle"></span>
                <span>
                  <span class="fw-bold">{{ dashboard.passedResultCount }}</span> läbitud
                  <span class="text-body-secondary">({{ roundPercent(passedPercent) }}%)</span>
                </span>
              </li>
              <li class="d-flex align-items-center gap-2">
                <span class="legend-dot legend-failed rounded-circle"></span>
                <span>
                  <span class="fw-bold">{{ dashboard.failedResultCount }}</span> läbikukkunud
                  <span class="text-body-secondary">
                    ({{ completedResultCount ? 100 - roundPercent(passedPercent) : 0 }}%)
                  </span>
                </span>
              </li>
            </ul>
          </div>
        </div>
      </section>

      <RouterLink
        v-for="statCard in statCards"
        :key="statCard.key"
        :to="statCard.to"
        class="card stat-card app-surface rounded-4 text-decoration-none"
        :class="{ 'stat-card-highlighted': statCard.isHighlighted }"
      >
        <div class="card-body d-flex flex-column gap-3 p-4">
          <div class="d-flex align-items-center justify-content-between">
            <span
              class="stat-icon d-inline-flex align-items-center justify-content-center rounded-3"
            >
              <component :is="statCard.icon" :size="24" />
            </span>
            <PhArrowRight :size="18" class="stat-arrow" />
          </div>
          <div>
            <div class="stat-count fw-bold">{{ statCard.count }}</div>
            <div class="fw-semibold text-body">{{ statCard.label }}</div>
            <small class="text-body-secondary">{{ statCard.hint }}</small>
          </div>
        </div>
      </RouterLink>
    </div>

    <BaseModal
      size="lg"
      :is-open="isAllScoresModalOpen"
      @event-modal-closed="isAllScoresModalOpen = false"
    >
      <template #title>Keskmine tulemus kompetentsi järgi</template>
      <template #body>
        <CompetenceScoreBars :competence-scores="dashboard.competenceScores" class="mb-2" />
      </template>
    </BaseModal>
  </div>
</template>

<style scoped>
/* Graafikud on kumbki kahe väikese kaardi laiused: 4 veergu → 2 veergu → 1 veerg */
.dashboard-grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: 1rem;
}

@media (min-width: 768px) {
  .dashboard-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .chart-card {
    grid-column: span 2;
  }
}

@media (min-width: 1200px) {
  .dashboard-grid {
    grid-template-columns: repeat(4, 1fr);
  }
}

.donut {
  width: 11rem;
  height: 11rem;
}

.donut circle {
  fill: none;
  stroke-width: 4;
}

/* Kaar algab kella 12 pealt */
.donut-passed {
  stroke: var(--bs-success);
  stroke-dashoffset: 25;
  stroke-linecap: round;
}

.donut-failed {
  stroke: rgba(var(--bs-danger-rgb), 0.55);
}

.donut-value {
  font-size: 2rem;
  line-height: 1.1;
}

.legend-dot {
  width: 0.75rem;
  height: 0.75rem;
}

.legend-passed {
  background-color: var(--bs-success);
}

.legend-failed {
  background-color: rgba(var(--bs-danger-rgb), 0.55);
}

.stat-card {
  transition:
    transform 0.15s ease,
    box-shadow 0.15s ease;
}

/* Kirjutab üle main.css globaalse a:hover rohelise tausta */
.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--app-surface-shadow-hover);
  background-color: var(--bs-card-bg);
}

.stat-icon {
  width: 2.75rem;
  height: 2.75rem;
  color: var(--bs-primary);
  background-color: rgba(var(--bs-primary-rgb), 0.08);
}

.stat-arrow {
  color: var(--bs-gray-500);
  transition: transform 0.15s ease;
}

.stat-card:hover .stat-arrow {
  transform: translateX(3px);
  color: var(--bs-primary);
}

.stat-count {
  font-size: 2.25rem;
  line-height: 1.1;
  color: var(--bs-body-color);
}

.stat-card-highlighted .stat-icon {
  color: var(--bs-warning-text-emphasis);
  background-color: var(--bs-warning-bg-subtle);
}

.stat-card-highlighted {
  border-color: var(--bs-warning-border-subtle);
}
</style>
