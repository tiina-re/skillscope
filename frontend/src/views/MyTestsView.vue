<script>
import PreviewCard from '@/components/PreviewCard.vue'
import LoadingText from '@/components/LoadingText.vue'
import UserTestService from '@/services/UserTestService.js'
import Status from '@/Status.js'
import TestNotFoundCard from '@/components/TestNotFoundCard.vue'
import AlertDanger from '@/components/AlertDanger.vue'
import MainTitle from '@/components/MainTitle.vue'
import NavigationService from '@/services/NavigationService.js'

export default {
  name: 'MyTestsView',
  components: { MainTitle, AlertDanger, TestNotFoundCard, LoadingText, PreviewCard },
  beforeMount() {
    this.getUserTests()
  },

  data() {
    return {
      errorMessage: '',
      userTestStatus: Status,
      isLoading: true,
      userTestSummaries: [
        {
          userTestId: 0,
          testId: 0,
          testName: '',
          testShortDescription: '',
          userTestStatus: '',
        },
      ],
    }
  },

  methods: {
    getUserTests() {
      UserTestService.getUserTests()
        .then((response) => this.handleGetUserTests(response))
        .catch(() => this.handleErrorMessage())
        .finally(() => (this.isLoading = false))
    },

    handleGetUserTests(response) {
      this.userTestSummaries = response.data
    },

    handleErrorMessage() {
      this.errorMessage = 'Testide laadimine ebaõnnestus'
    },
    goToTestStartView(testId) {
      NavigationService.navigateToTestStartView(testId)
    },
  },
}
</script>

<template>
  <div class="container py-4">
    <LoadingText v-if="isLoading" />
    <div v-else-if="errorMessage" class="text-center">
      <AlertDanger :error-message="errorMessage" />
    </div>
    <div v-else-if="userTestSummaries.length === 0">
      <TestNotFoundCard :message="'Sulle pole ühtki testi määratud'" />
    </div>
    <div v-else>
      <MainTitle title="Minu testid" />
      <div class="preview-test-card-grid">
        <PreviewCard
          v-for="userTestSummary in userTestSummaries"
          :key="userTestSummary.userTestId"
          :status-badge="userTestStatus[userTestSummary.userTestStatus]"
        >
          <template #title>{{ userTestSummary.testName }}</template>
          <template #description>{{ userTestSummary.testShortDescription }}</template>
          <template #actions>
            <button
              v-if="userTestSummary.userTestStatus === 'O'"
              class="btn btn-primary fw-bold rounded-2"
              @click="goToTestStartView(userTestSummary.testId)"
            >
              Soorita test
            </button>
            <button
              v-if="userTestSummary.userTestStatus === 'C'"
              class="btn btn-light fw-bold rounded-2"
            >
              Vaata tulemusi
            </button>
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
