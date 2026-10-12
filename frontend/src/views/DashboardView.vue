<script>
import MainTitle from '@/components/MainTitle.vue'
import LoadingText from '@/components/LoadingText.vue'
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
  components: { MainTitle, LoadingText, PhArrowRight },
  data() {
    return {
      isLoading: true,
      errorMessage: '',
      dashboard: {
        assignedTestCount: 0,
        recentTestCount: 0,
        recentQuestionCount: 0,
        pendingAiQuestionCount: 0,
        recentDays: 7,
      },
    }
  },
  computed: {
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

    <div v-else class="stat-grid">
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
  </div>
</template>

<style scoped>
.stat-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(min(15rem, 100%), 1fr));
  gap: 1rem;
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
