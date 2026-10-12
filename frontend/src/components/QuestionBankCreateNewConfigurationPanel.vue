<script>
import { PhSparkle } from '@phosphor-icons/vue'

export default {
  name: 'QuestionBankCreateNewConfigurationPanel',
  components: { PhSparkle },
  props: {
    competenceId: {},
    competenceLevelId: {},
    competenceLevels: {},
    competences: {},
    questionCount: {},
    questionCountOptions: {},
    questionTypeId: {},
    questionTypeLabels: {},
    questionTypes: {}
  },
  // Valikud elavad vanemas (v-model:competence-id jne) - prop'i ei muudeta, muudatus saadetakse emitiga
  emits: ['update:competenceId', 'update:competenceLevelId', 'update:questionTypeId', 'update:questionCount'],
  computed: {
    // get/set vahendajad: natiivne v-model säilitab väärtuse tüübi (number/null), setter saadab selle vanemale
    selectedCompetenceId: {
      get() {
        return this.competenceId
      },
      set(competenceId) {
        this.$emit('update:competenceId', competenceId)
      },
    },
    selectedCompetenceLevelId: {
      get() {
        return this.competenceLevelId
      },
      set(competenceLevelId) {
        this.$emit('update:competenceLevelId', competenceLevelId)
      },
    },
    selectedQuestionTypeId: {
      get() {
        return this.questionTypeId
      },
      set(questionTypeId) {
        this.$emit('update:questionTypeId', questionTypeId)
      },
    },
    selectedQuestionCount: {
      get() {
        return this.questionCount
      },
      set(questionCount) {
        this.$emit('update:questionCount', questionCount)
      },
    },
  },
}
</script>

<template>
  <div class="card-body generator-card-body">
    <h2 class="h5 fw-bold d-flex align-items-center gap-2 mb-1">
      <PhSparkle :size="20"/>
      Loo küsimusi AI-ga
    </h2>
    <p class="text-secondary mb-4">
      Vali kompetents ja tase, millele küsimusi lood. AI arvestab valikuga igas vastuses.
    </p>

    <div class="row g-3">
      <div class="col-md-6 col-xl-3">
        <label for="selectGeneratorCompetence" class="form-label fw-semibold">
          Kompetents
        </label>
        <select
            id="selectGeneratorCompetence"
            v-model="selectedCompetenceId"
            class="form-select"
        >
          <option :value="0" disabled>Vali kompetents</option>
          <option
              v-for="competence in competences"
              :key="competence.competenceId"
              :value="competence.competenceId"
          >
            {{ competence.competenceName }}
          </option>
        </select>
      </div>
      <div class="col-md-6 col-xl-3">
        <label for="selectGeneratorLevel" class="form-label fw-semibold">Tase</label>
        <select
            id="selectGeneratorLevel"
            v-model="selectedCompetenceLevelId"
            class="form-select"
            :disabled="competenceId === 0"
        >
          <option :value="0" disabled>
            {{ competenceId === 0 ? 'Vali enne kompetents' : 'Vali tase' }}
          </option>
          <option
              v-for="competenceLevel in competenceLevels"
              :key="competenceLevel.competenceLevelId"
              :value="competenceLevel.competenceLevelId"
          >
            {{ competenceLevel.levelName }}
          </option>
        </select>
      </div>
      <div class="col-md-6 col-xl-3">
        <label for="selectGeneratorQuestionType" class="form-label fw-semibold">
          Küsimuse tüüp
        </label>
        <select id="selectGeneratorQuestionType" v-model="selectedQuestionTypeId" class="form-select">
          <option :value="null">Kõik tüübid (AI valib)</option>
          <option
              v-for="questionType in questionTypes"
              :key="questionType.questionTypeId"
              :value="questionType.questionTypeId"
          >
            {{
            questionTypeLabels[questionType.questionTypeName] ?? questionType.questionTypeName
            }}
          </option>
        </select>
      </div>
      <div class="col-md-6 col-xl-3">
            <span id="labelQuestionCount" class="form-label fw-semibold d-block">
              Küsimuste arv
            </span>
        <div
            class="btn-group question-count-group w-100"
            role="group"
            aria-labelledby="labelQuestionCount"
        >
          <template v-for="count in questionCountOptions" :key="count">
            <input
                :id="`questionCount${count}`"
                v-model="selectedQuestionCount"
                type="radio"
                class="btn-check"
                name="questionCount"
                :value="count"
                autocomplete="off"
            />
            <label class="btn btn-outline-primary fw-bold" :for="`questionCount${count}`">
              {{ count }}
            </label>
          </template>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.generator-card-body {
  padding: 1.75rem 2rem;
}

.question-count-group .btn {
  flex: 1 1 0;
}

/* Samad mullid nagu QuestionBankCard-is */

</style>