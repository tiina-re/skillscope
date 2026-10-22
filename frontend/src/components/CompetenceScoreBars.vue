<script>
export default {
  name: 'CompetenceScoreBars',
  props: {
    competenceScores: { type: Array, required: true },
  },
  methods: {
    roundPercent(value) {
      return Math.round(value ?? 0)
    },
  },
}
</script>

<template>
  <ul class="list-unstyled d-flex flex-column gap-3 mb-0">
    <li v-for="competenceScore in competenceScores" :key="competenceScore.competenceName">
      <div class="d-flex justify-content-between small mb-1">
        <span class="fw-semibold">{{ competenceScore.competenceName }}</span>
        <span class="fw-bold">{{ roundPercent(competenceScore.averageScorePercent) }}%</span>
      </div>
      <div
        class="score-track rounded-pill"
        role="progressbar"
        :aria-label="competenceScore.competenceName"
        :aria-valuenow="roundPercent(competenceScore.averageScorePercent)"
        aria-valuemin="0"
        aria-valuemax="100"
      >
        <div
          class="score-bar rounded-pill"
          :style="{ width: `${competenceScore.averageScorePercent}%` }"
        ></div>
      </div>
    </li>
  </ul>
</template>

<style scoped>
.score-track {
  height: 0.6rem;
  background-color: rgba(var(--bs-primary-rgb), 0.12);
  overflow: hidden;
}

.score-bar {
  height: 100%;
  background: linear-gradient(90deg, var(--bs-primary), var(--bs-success));
}
</style>
