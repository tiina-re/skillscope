<script>
import CompetenceService from '@/services/CompetenceService.js'
import CompetenceLevelService from '@/services/CompetenceLevelService.js'
import LoadingText from '@/components/LoadingText.vue'
import TestNotFoundIllustration from '@/components/TestNotFoundIllustration.vue'
import { PhArrowLeft } from '@phosphor-icons/vue'

export default {
  name: 'CompetenceDetailView',
  components: { LoadingText, TestNotFoundIllustration, PhArrowLeft },
  props: {
    competenceId: { type: Number, required: true },
  },
  data() {
    return {
      isLoading: true,
      errorMessage: '',
      competence: {
        competenceId: 0,
        competenceName: '',
        competenceShortDescription: '',
        competenceDescription: '',
      },
      competenceLevels: [],
    }
  },
  methods: {
    getCompetenceDetail() {
      Promise.all([
        CompetenceService.getCompetenceDetailRequest(this.competenceId),
        CompetenceLevelService.getCompetenceLevelsRequest(this.competenceId),
      ])
        .then(([competenceResponse, levelsResponse]) => {
          this.competence = competenceResponse.data
          this.competenceLevels = levelsResponse.data
        })
        .catch((error) => this.handleGetCompetenceDetailErrorResponse(error))
        .finally(() => (this.isLoading = false))
    },
    handleGetCompetenceDetailErrorResponse(error) {
      if (error.response?.status === 404) {
        this.errorMessage = 'Sellist kompetentsi ei leitud'
      } else {
        this.errorMessage = 'Kompetentsi andmete laadimine ebaõnnestus'
      }
    },
    navigateToCompetenceView() {
      this.$router.push({ name: 'competenceView' })
    },
  },
  beforeMount() {
    this.getCompetenceDetail()
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
        @click="navigateToCompetenceView"
      >
        <PhArrowLeft :size="16" />
        Kompetentsid
      </button>

      <div class="card app-surface p-4 detail-card">
        <div class="card-body">
          <p class="eyebrow text-brand mb-2">Kompetents</p>
          <h1 class="competence-title h3 fw-bold mb-4">
            {{ competence.competenceName }}
          </h1>

          <p class="lead text-body-secondary mb-4">{{ competence.competenceShortDescription }}</p>

          <div class="d-flex flex-wrap gap-2 mb-4">
            <span
              v-for="level in competenceLevels"
              :key="level.competenceLevelId"
              class="badge rounded-pill badge-neutral"
              >{{ level.levelName }}</span
            >
          </div>

          <hr class="my-4" />

          <h2 class="h6 text-uppercase text-body-secondary fw-semibold mb-2">
            Kompetentsi kirjeldus
          </h2>
          <p class="description mb-0">{{ competence.competenceDescription }}</p>
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

.competence-title {
  overflow-wrap: anywhere;
}

.lead {
  font-size: 1.125rem;
  line-height: 1.6;
}

.description {
  line-height: 1.7;
  white-space: pre-line;
}
</style>
