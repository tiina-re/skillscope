<script>
// Pilt genereeritakse docs/database/2_create.sql põhjal: npm run db-schema
import schemaSvg from '@/assets/db-schema.svg?raw'
import { PhArrowsOut, PhMagnifyingGlassMinus, PhMagnifyingGlassPlus } from '@phosphor-icons/vue'

const MIN_SCALE = 0.15
const MAX_SCALE = 2.5
const ZOOM_STEP = 1.2

export default {
  name: 'DatabaseSchema',
  components: { PhArrowsOut, PhMagnifyingGlassMinus, PhMagnifyingGlassPlus },
  data() {
    return {
      schemaSvg,
      scale: 1,
      x: 0,
      y: 0,
      dragStart: null,
    }
  },
  mounted() {
    this.fitToWidth()
  },
  methods: {
    svgSize() {
      const svg = this.$refs.canvas.querySelector('svg')
      return { width: svg.width.baseVal.value, height: svg.height.baseVal.value }
    },

    fitToWidth() {
      this.scale = this.$refs.viewport.clientWidth / this.svgSize().width
      this.x = 0
      this.y = 0
    },

    fitToView() {
      const { clientWidth, clientHeight } = this.$refs.viewport
      const { width, height } = this.svgSize()
      this.scale = Math.min(clientWidth / width, clientHeight / height)
      this.x = (clientWidth - width * this.scale) / 2
      this.y = (clientHeight - height * this.scale) / 2
    },

    // Suumib nii, et punkt (pointX, pointY) jääb ekraanil paigale
    zoomTo(newScale, pointX, pointY) {
      const clampedScale = Math.min(MAX_SCALE, Math.max(MIN_SCALE, newScale))
      const ratio = clampedScale / this.scale
      this.x = pointX - (pointX - this.x) * ratio
      this.y = pointY - (pointY - this.y) * ratio
      this.scale = clampedScale
    },

    zoomAtPointer(event) {
      const rect = this.$refs.viewport.getBoundingClientRect()
      const factor = event.deltaY < 0 ? ZOOM_STEP : 1 / ZOOM_STEP
      this.zoomTo(this.scale * factor, event.clientX - rect.left, event.clientY - rect.top)
    },

    zoomAtCenter(factor) {
      const { clientWidth, clientHeight } = this.$refs.viewport
      this.zoomTo(this.scale * factor, clientWidth / 2, clientHeight / 2)
    },

    startDrag(event) {
      if (event.button !== 0) return
      this.dragStart = { x: event.clientX - this.x, y: event.clientY - this.y }
      this.$refs.viewport.setPointerCapture(event.pointerId)
    },

    drag(event) {
      if (!this.dragStart) return
      this.x = event.clientX - this.dragStart.x
      this.y = event.clientY - this.dragStart.y
    },

    endDrag() {
      this.dragStart = null
    },

    // Hiirega tabeli kohal: tõsta esile tabeli enda seosed ja nendega seotud tabelid
    highlightTable(event) {
      const tableName = event.target.closest('.db-table')?.dataset.table
      const canvas = this.$refs.canvas
      canvas.classList.toggle('has-highlight', Boolean(tableName))
      canvas
        .querySelectorAll('.highlight')
        .forEach((element) => element.classList.remove('highlight'))
      if (!tableName) return
      canvas.querySelector(`.db-table[data-table="${tableName}"]`).classList.add('highlight')
      canvas
        .querySelectorAll(`.edge[data-from="${tableName}"], .edge[data-to="${tableName}"]`)
        .forEach((edge) => {
          edge.classList.add('highlight')
          canvas
            .querySelector(`.db-table[data-table="${edge.dataset.from}"]`)
            .classList.add('highlight')
          canvas
            .querySelector(`.db-table[data-table="${edge.dataset.to}"]`)
            .classList.add('highlight')
        })
    },
  },
}
</script>

<template>
  <div>
    <div class="d-flex flex-wrap align-items-center justify-content-between gap-2 mb-3">
      <p class="text-body-secondary small mb-0">
        Rullikuga suumid, lohistades liigutad. Vii hiir tabelile, et näha selle seoseid.<br />
        PK = primaarvõti, FK = välisvõti, UQ = unikaalne, <code>?</code> = võib olla tühi.
      </p>
      <div class="btn-group btn-group-sm">
        <button
          class="btn btn-outline-primary"
          type="button"
          aria-label="Vähenda"
          @click="zoomAtCenter(1 / 1.2)"
        >
          <PhMagnifyingGlassMinus :size="16" />
        </button>
        <span class="btn btn-outline-primary disabled zoom-value">
          {{ Math.round(scale * 100) }}%
        </span>
        <button
          class="btn btn-outline-primary"
          type="button"
          aria-label="Suurenda"
          @click="zoomAtCenter(1.2)"
        >
          <PhMagnifyingGlassPlus :size="16" />
        </button>
        <button
          class="btn btn-outline-primary"
          type="button"
          aria-label="Näita tervet skeemi"
          title="Näita tervet skeemi"
          @click="fitToView"
        >
          <PhArrowsOut :size="16" />
        </button>
      </div>
    </div>
    <div
      ref="viewport"
      class="schema-viewport"
      :class="{ dragging: dragStart }"
      @wheel.prevent="zoomAtPointer"
      @pointerdown="startDrag"
      @pointermove="drag"
      @pointerup="endDrag"
      @pointercancel="endDrag"
    >
      <!-- eslint-disable-next-line vue/no-v-html -- oma genereeritud SVG fail, mitte kasutaja sisend -->
      <div
        ref="canvas"
        class="schema-canvas"
        :style="{ transform: `translate(${x}px, ${y}px) scale(${scale})` }"
        @mouseover="highlightTable"
        @mouseleave="highlightTable"
        v-html="schemaSvg"
      ></div>
    </div>
  </div>
</template>

<style scoped>
/* Aken, milles skeemi suumitakse (transform: scale) ja lohistatakse (transform: translate) */
.schema-viewport {
  height: 70vh;
  overflow: hidden;
  cursor: grab;
  touch-action: none;
  user-select: none;
  border: 1px solid var(--app-surface-border);
  border-radius: 1rem;
  background: var(--bs-body-bg);
}

.schema-viewport.dragging {
  cursor: grabbing;
}

.schema-canvas {
  width: max-content;
  transform-origin: 0 0;
}

.zoom-value {
  min-width: 3.5rem;
  opacity: 1;
}

/* SVG sisu tuleb v-html'iga, seepärast :deep() */
.schema-canvas :deep(svg) {
  display: block;
  font-family: inherit;
}

.schema-canvas :deep(.table-box) {
  fill: var(--bs-body-bg);
  stroke: var(--app-surface-border);
  stroke-width: 1.5;
}

.schema-canvas :deep(.table-header) {
  fill: rgba(var(--bs-primary-rgb), 0.12);
}

.schema-canvas :deep(.table-name) {
  fill: var(--app-ink);
  font-size: 14px;
  font-weight: 700;
}

.schema-canvas :deep(.column) {
  fill: var(--bs-body-color);
  font-size: 12.5px;
}

.schema-canvas :deep(.column-fk) {
  font-weight: 600;
}

.schema-canvas :deep(.type) {
  fill: var(--bs-secondary-color);
  font-size: 11.5px;
  font-family: var(--bs-font-monospace);
}

.schema-canvas :deep(.nullable) {
  font-style: italic;
}

.schema-canvas :deep(.key) {
  font-size: 10px;
  font-weight: 700;
}

.schema-canvas :deep(.key-pk) {
  fill: var(--bs-warning);
}

.schema-canvas :deep(.key-fk) {
  fill: var(--bs-primary);
}

.schema-canvas :deep(.key-uq) {
  fill: var(--bs-secondary-color);
}

.schema-canvas :deep(.edge) {
  fill: none;
  stroke: var(--bs-primary);
  stroke-width: 1.5;
  opacity: 0.45;
}

.schema-canvas :deep(.edge-arrow) {
  fill: var(--bs-primary);
}

/* Esiletõst: teised tabelid ja seosed tuhmuvad */
.schema-canvas.has-highlight :deep(.db-table:not(.highlight)) {
  opacity: 0.35;
}

.schema-canvas.has-highlight :deep(.edge:not(.highlight)) {
  opacity: 0.08;
}

.schema-canvas :deep(.edge.highlight) {
  opacity: 1;
  stroke-width: 2.5;
}

.schema-canvas :deep(.db-table.highlight .table-box) {
  stroke: var(--bs-primary);
}
</style>
