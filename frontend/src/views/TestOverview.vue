<script>
import TestService from '@/services/TestService.js'
import CompetenceService from '@/services/CompetenceService.js'
import NavigationService from '@/services/NavigationService.js'
import Status from '@/Status.js'
import PreviewCard from '@/components/PreviewCard.vue'
import LoadingText from '@/components/LoadingText.vue'
import MainTitle from '@/components/MainTitle.vue'
import FilterDropdown from '@/components/FilterDropdown.vue'
import AlertDanger from '@/components/AlertDanger.vue'

export default {
  name: 'TestOverview',
  components: { LoadingText, PreviewCard, MainTitle, FilterDropdown, AlertDanger },

  beforeMount() {
    this.getCompetences()
    this.getAllTests()
  },

  data() {
    return {
      isLoading: true,
      selectedCompetenceId: null,
      competences: [],
      errorMessage: '',
      testStatus: Status,
      testSummaries: [],
    }
  },
  computed: {
    competenceOptions() {
      return this.competences.map((competence) => ({
        value: competence.competenceId,
        label: competence.competenceName,
      }))
    },
  },
  methods: {
    getCompetences() {
      CompetenceService.getCompetencesRequest()
        .then((response) => (this.competences = response.data))
        .catch()
    },
    selectCompetence(competenceId) {
      this.selectedCompetenceId = competenceId
      this.getAllTests()
    },
    getAllTests() {
      this.errorMessage = ''
      TestService.getAllTests(this.selectedCompetenceId)
        .then((response) => this.handleGetAllTests(response))
        .catch(() => (this.errorMessage = 'Testide laadimine ebaõnnestus'))
        .finally(() => (this.isLoading = false))
    },
    handleGetAllTests(response) {
      this.testSummaries = response.data
    },
    navigateToTestDetail(testId) {
      NavigationService.navigateToTestDetail(testId)
    },
    assignTestToUser() {},
    // tühi meetod nupuvajutuse näitamiseks
  },
}
</script>

<template>
  <div class="container py-4">
    <LoadingText v-if="isLoading" />
    <div v-else>
      <MainTitle title="Testid">
        <FilterDropdown
          :options="competenceOptions"
          :selected-value="selectedCompetenceId"
          :all-option="{ value: null, label: 'Kõik kompetentsid' }"
          @event-option-selected="selectCompetence"
        />
      </MainTitle>
      <AlertDanger v-if="errorMessage" :error-message="errorMessage" />
      <p v-else-if="testSummaries.length === 0" class="text-body-secondary">Teste ei leitud</p>
      <div v-else class="preview-test-card-grid">
        <PreviewCard
          v-for="testSummary in testSummaries"
          :key="testSummary.testId"
          :status-badge="testStatus[testSummary.testStatus]"
        >
          <template #title>{{ testSummary.testName }}</template>
          <template #description>{{ testSummary.testShortDescription }}</template>
          <template #menu>
            <li>
              <button class="dropdown-item" @click="assignTestToUser(testSummary.testId)">
                Määra test
              </button>
            </li>
          </template>
          <template #actions>
            <button
              class="btn btn-primary fw-bold rounded-2"
              @click="navigateToTestDetail(testSummary.testId)"
            >
              Vaata testi
            </button>
            <button class="btn btn-light fw-bold rounded-2">Vaata tulemusi</button>
          </template>
        </PreviewCard>
      </div>
    </div>
  </div>
</template>

<style scoped>
.preview-test-card-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(min(22rem, 100%), 1fr));
  gap: 1rem;
}
</style>
