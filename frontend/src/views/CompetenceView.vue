<script>
import MainTitle from '@/components/MainTitle.vue'
import LoadingText from '@/components/LoadingText.vue'
import PreviewCard from '@/components/PreviewCard.vue'
import CompetenceService from '@/services/CompetenceService.js'
import CompetenceLevelService from '@/services/CompetenceLevelService.js'

export default {
  name: 'CompetenceView',
  components: { MainTitle, LoadingText, PreviewCard },
  data() {
    return {
      isLoading: true,
      errorMessage: '',
      competences: [],
      levelsByCompetenceId: {},
    }
  },
  methods: {
    getCompetences() {
      CompetenceService.getCompetencesRequest()
        .then((response) => {
          this.competences = response.data
          return this.getCompetenceLevels()
        })
        .catch(() => (this.errorMessage = 'Kompetentside laadimine ebaõnnestus'))
        .finally(() => (this.isLoading = false))
    },
    getCompetenceLevels() {
      return Promise.all(
        this.competences.map((competence) =>
          CompetenceLevelService.getCompetenceLevelsRequest(competence.competenceId).then(
            (response) => (this.levelsByCompetenceId[competence.competenceId] = response.data),
          ),
        ),
      )
    },
    navigateToCompetenceDetail(competenceId) {
      this.$router.push({ name: 'competenceDetailRoute', params: { competenceId } })
    },
  },
  beforeMount() {
    this.getCompetences()
  },
}
</script>

<template>
  <div class="container py-4">
    <MainTitle title="Kompetentsid" />
    <LoadingText v-if="isLoading" />

    <div v-else-if="errorMessage.length" class="alert alert-danger">{{ errorMessage }}</div>

    <div v-else class="competence-grid">
      <PreviewCard v-for="competence in competences" :key="competence.competenceId">
        <template #title>{{ competence.competenceName }}</template>
        <template #description>
          <span class="d-block mb-3">{{ competence.competenceShortDescription }}</span>
          <span class="d-flex flex-wrap gap-2">
            <span
              v-for="level in levelsByCompetenceId[competence.competenceId]"
              :key="level.competenceLevelId"
              class="badge rounded-pill badge-neutral"
              >{{ level.levelName }}</span
            >
          </span>
        </template>
        <template #actions>
          <button
            class="btn btn-primary fw-bold rounded-2"
            @click="navigateToCompetenceDetail(competence.competenceId)"
          >
            Vaata
          </button>
        </template>
      </PreviewCard>
    </div>
  </div>
</template>

<style scoped>
.competence-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(min(22rem, 100%), 1fr));
  gap: 1rem;
}
</style>
