// Loob docs/database/2_create.sql põhjal andmebaasi skeemi pildi src/assets/db-schema.svg.
// Käivitus: npm run db-schema (pärast igat 2_create.sql muudatust).
// Värvid tulevad CSS klassidest (components/DatabaseSchema.vue), et pilt järgiks heledat ja tumedat teemat.
import { readFileSync, writeFileSync } from 'node:fs'

const sql = readFileSync(new URL('../../docs/database/2_create.sql', import.meta.url), 'utf8')
const outFile = new URL('../src/assets/db-schema.svg', import.meta.url)

// Tabelite vasaku ülanurga asukohad (x, y) pikslites, GRID-i kordsed. Leitud katsetades, kus jooned on
// lühikesed ja ristuvad vähe. Uus tabel, mida siin pole, läheb skeemi alla — lisa see siia vabasse kohta
// ja vaata väljundist ristumiste arvu.
const POSITIONS = {
  test_question: [1460, 0],
  role: [890, 100],
  competence: [1950, 100],
  result: [890, 260],
  user: [0, 340],
  user_test: [360, 360],
  question: [1420, 360],
  competence_level: [1950, 420],
  level: [1950, 620],
  group: [520, 640],
  question_answer: [1420, 700],
  competence_file: [1950, 820],
  profile: [360, 880],
  test_question_result: [1420, 960],
  ai_question: [1950, 1040],
  group_member: [360, 1140],
  test_question_answer: [1420, 1200],
  group_manager: [360, 1380],
  ai_question_answer: [1950, 1420],
  question_type: [1420, 1440],
  invitation: [360, 1580],
  test: [360, 1840],
}

// Kõik mõõdud on GRID-i kordsed, et veerureade keskpunktid jääksid joonte ruudustikule
const GRID = 10
const TABLE_WIDTH = 290
const HEADER_HEIGHT = 30
const ROW_HEIGHT = 20
const TABLE_MARGIN = 60 // vähim vahe kahe tabeli vahel
const PADDING = 120 // vaba ruum skeemi ümber, et jooned saaksid tabelitest väljastpoolt ringi minna
const FAN_IN = 60 // nii mitu pikslit enne sihtkohta tohivad sama veeru jooned üheks ühineda
const CORNER_RADIUS = 6

// Joonte marsruutimise "hinnad": algoritm valib kõige odavama tee
const COST = {
  step: 1, // üks ruut edasi
  sharedStep: 0.5, // ühine lõik sama sihtveeru joonega viimastel FAN_IN pikslitel
  bend: 4, // pööre
  nearTable: 3, // tabelile lähemal kui 2 ruutu
  adjacent: 5, // teise joonega kõrvuti (1 ruudu kaugusel) sõitmine — jooned sulaksid vähendatult kokku
  crossing: 30, // teise joone ristumine
  overlap: 60, // teise joonega kattumine või tema pöördepunkti puudutamine
}

const tables = new Map()
for (const [, name, body] of sql.matchAll(/CREATE TABLE "?(\w+)"? \(([\s\S]*?)\n\);/g)) {
  const columns = []
  for (const line of body.split('\n')) {
    const column = line.match(/^\s*"?(\w+)"?\s+(\w+(?:\(\d+(?:,\d+)?\))?)\s+(NOT NULL|NULL)/)
    if (column) columns.push({ name: column[1], type: column[2], nullable: column[3] === 'NULL' })
  }
  const uniques = [...body.matchAll(/UNIQUE \((\w+)\)/g)].map((match) => match[1])
  const height = HEADER_HEIGHT + columns.length * ROW_HEIGHT + GRID
  tables.set(name, { name, columns, uniques, height })
}

const foreignKeys = [
  ...sql.matchAll(/ALTER TABLE "?(\w+)"? ADD CONSTRAINT \w+\s+FOREIGN KEY \((\w+)\)\s+REFERENCES "?(\w+)"? \((\w+)\)/g),
].map(([, from, column, to, toColumn]) => ({ from, column, to, toColumn }))

// Tabelite asukohad: POSITIONS-ist, puuduvad tabelid ritta skeemi alla
for (const name of Object.keys(POSITIONS)) {
  if (!tables.has(name)) throw new Error(`POSITIONS-is on tabel "${name}", mida 2_create.sql-is pole`)
}
const placedTables = [...tables.values()].filter((table) => POSITIONS[table.name])
let bottom = Math.max(...placedTables.map((table) => POSITIONS[table.name][1] + table.height))
let nextX = 0
for (const table of tables.values()) {
  const position = POSITIONS[table.name] ?? [nextX, bottom + TABLE_MARGIN * 2]
  if (!POSITIONS[table.name]) nextX += TABLE_WIDTH + TABLE_MARGIN * 2
  table.x = Math.round(position[0] / GRID) * GRID
  table.y = Math.round(position[1] / GRID) * GRID
}
for (const a of tables.values()) {
  for (const b of tables.values()) {
    const tooClose =
      a.name < b.name &&
      a.x < b.x + TABLE_WIDTH + TABLE_MARGIN &&
      b.x < a.x + TABLE_WIDTH + TABLE_MARGIN &&
      a.y < b.y + b.height + TABLE_MARGIN &&
      b.y < a.y + a.height + TABLE_MARGIN
    if (tooClose) throw new Error(`Tabelid "${a.name}" ja "${b.name}" on POSITIONS-is üksteisele liiga lähedal`)
  }
}
// Nihuta kõik nii, et skeemi ümber jääks PADDING
const minX = Math.min(...[...tables.values()].map((table) => table.x))
const minY = Math.min(...[...tables.values()].map((table) => table.y))
for (const table of tables.values()) {
  table.x += PADDING - minX
  table.y += PADDING - minY
}
const width = Math.max(...[...tables.values()].map((table) => table.x + TABLE_WIDTH)) + PADDING
const height = Math.max(...[...tables.values()].map((table) => table.y + table.height)) + PADDING

const rowY = (table, columnName) =>
  table.y + HEADER_HEIGHT + table.columns.findIndex((column) => column.name === columnName) * ROW_HEIGHT + ROW_HEIGHT / 2

const escape = (text) => text.replace(/&/g, '&amp;').replace(/</g, '&lt;')

// ---------- Joonte marsruutimine ----------
// Ruudustik GRID-pikslise sammuga. Jooned liiguvad ainult horisontaalselt/vertikaalselt (A* otsing),
// tabelitest läbi ei saa ning teiste joonte ristumine/kattumine on kallis.
const columnsCount = width / GRID + 1
const rowsCount = height / GRID + 1
const cellOf = (x, y) => (y / GRID) * columnsCount + x / GRID
const cellX = (cell) => (cell % columnsCount) * GRID
const cellY = (cell) => Math.floor(cell / columnsCount) * GRID
const blocked = new Uint8Array(columnsCount * rowsCount)
const nearTable = new Uint8Array(columnsCount * rowsCount)
for (const table of tables.values()) {
  for (let x = table.x - 2 * GRID; x <= table.x + TABLE_WIDTH + 2 * GRID; x += GRID) {
    for (let y = table.y - 2 * GRID; y <= table.y + table.height + 2 * GRID; y += GRID) {
      const inside = x >= table.x && x <= table.x + TABLE_WIDTH && y >= table.y && y <= table.y + table.height
      if (inside) blocked[cellOf(x, y)] = 1
      else nearTable[cellOf(x, y)] = 1
    }
  }
}

// Suunad: 0 paremale, 1 vasakule, 2 alla, 3 üles
const DX = [1, -1, 0, 0]
const DY = [0, 0, 1, -1]
const isHorizontal = (direction) => direction < 2
// Iga ruudu kohta: millised jooned (sihtveeru võti) seda horisontaalselt, vertikaalselt või pöördena kasutavad
const horizontalOwners = new Map()
const verticalOwners = new Map()
const bendOwners = new Map()
const hasOther = (owners, cell, key) => [...(owners.get(cell) ?? [])].some((owner) => owner !== key)
const addOwner = (owners, cell, key) => owners.set(cell, (owners.get(cell) ?? new Set()).add(key))
// Kas kõrvalruudus (horisontaalse joone puhul üleval/all, vertikaalse puhul vasakul/paremal) jookseb teine joon paralleelselt
const hasParallelNeighbour = (cell, horizontal, key) => {
  const [owners, offset] = horizontal ? [horizontalOwners, columnsCount] : [verticalOwners, 1]
  return hasOther(owners, cell - offset, key) || hasOther(owners, cell + offset, key)
}

function stepCost(cell, direction, key, distanceToGoal) {
  const horizontal = isHorizontal(direction)
  const [same, other] = horizontal ? [horizontalOwners, verticalOwners] : [verticalOwners, horizontalOwners]
  let cost = COST.step
  // Sama sihtveeru joonega ühinemine on lubatud ainult sihtkoha lähedal, mujal on see nagu kattumine
  if (same.get(cell)?.has(key)) cost = distanceToGoal <= FAN_IN ? COST.sharedStep : COST.step + COST.overlap
  if (hasOther(same, cell, key) || hasOther(bendOwners, cell, key)) cost += COST.overlap
  if (hasOther(other, cell, key)) cost += COST.crossing
  if (hasParallelNeighbour(cell, horizontal, key)) cost += COST.adjacent
  if (nearTable[cell]) cost += COST.nearTable
  return cost
}

// Väike min-kuhi A* jaoks
class MinHeap {
  items = []
  push(priority, value) {
    const items = this.items
    items.push([priority, value])
    for (let i = items.length - 1; i > 0; ) {
      const parent = (i - 1) >> 1
      if (items[parent][0] <= items[i][0]) break
      ;[items[parent], items[i]] = [items[i], items[parent]]
      i = parent
    }
  }
  pop() {
    const items = this.items
    const top = items[0]
    const last = items.pop()
    if (items.length) {
      items[0] = last
      for (let i = 0; ; ) {
        const left = 2 * i + 1
        const right = left + 1
        let smallest = i
        if (left < items.length && items[left][0] < items[smallest][0]) smallest = left
        if (right < items.length && items[right][0] < items[smallest][0]) smallest = right
        if (smallest === i) break
        ;[items[smallest], items[i]] = [items[i], items[smallest]]
        i = smallest
      }
    }
    return top[1]
  }
  get size() {
    return this.items.length
  }
}

// Tabeli külje "port": ruut vahetult tabeli kõrval veeru real. Joon väljub/siseneb vasakult või paremalt.
const ports = (table, columnName) => {
  const y = rowY(table, columnName)
  return [
    { x: table.x - GRID, y, edgeX: table.x, outward: 1, inward: 0 },
    { x: table.x + TABLE_WIDTH + GRID, y, edgeX: table.x + TABLE_WIDTH, outward: 0, inward: 1 },
  ]
}

let totalLength = 0
let totalBends = 0

function routeEdge(foreignKey) {
  const key = `${foreignKey.to}.${foreignKey.toColumn}`
  const startPorts = ports(tables.get(foreignKey.from), foreignKey.column)
  const goalPorts = ports(tables.get(foreignKey.to), foreignKey.toColumn)
  const goalByCell = new Map(goalPorts.map((port) => [cellOf(port.x, port.y), port]))
  const distanceToGoal = (cell) =>
    Math.min(...goalPorts.map((port) => Math.abs(port.x - cellX(cell)) + Math.abs(port.y - cellY(cell))))
  const heuristic = (cell) => (distanceToGoal(cell) / GRID) * COST.sharedStep

  const stateCount = columnsCount * rowsCount * 4
  const cost = new Float64Array(stateCount).fill(Infinity)
  const parent = new Int32Array(stateCount).fill(-1)
  const heap = new MinHeap()
  for (const port of startPorts) {
    const state = cellOf(port.x, port.y) * 4 + port.outward
    cost[state] = 0
    heap.push(heuristic(cellOf(port.x, port.y)), state)
  }

  while (heap.size) {
    const state = heap.pop()
    const cell = state >> 2
    const direction = state & 3
    const goal = goalByCell.get(cell)
    // Sihtporti tuleb jõuda tabeli poole liikudes
    if (goal && direction === goal.inward) return buildPath(state, parent, startPorts, goal, key)
    for (let next = 0; next < 4; next++) {
      if (next === (direction ^ 1)) continue // tagasi ei pöörata
      const x = (cell % columnsCount) + DX[next]
      const y = Math.floor(cell / columnsCount) + DY[next]
      if (x < 0 || y < 0 || x >= columnsCount || y >= rowsCount) continue
      const nextCell = y * columnsCount + x
      if (blocked[nextCell]) continue
      const nextState = nextCell * 4 + next
      const nextCost =
        cost[state] + stepCost(nextCell, next, key, distanceToGoal(nextCell)) + (next === direction ? 0 : COST.bend)
      if (nextCost < cost[nextState]) {
        cost[nextState] = nextCost
        parent[nextState] = state
        heap.push(nextCost + heuristic(nextCell), nextState)
      }
    }
  }
  return null
}

function buildPath(endState, parent, startPorts, goal, key) {
  const states = []
  for (let state = endState; state !== -1; state = parent[state]) states.unshift(state)
  const cells = states.map((state) => state >> 2)
  const points = cells.map((cell) => [cellX(cell), cellY(cell)])

  // Jäta kasutatud ruudud meelde, et järgmised jooned neid väldiksid
  states.forEach((state, index) => {
    const direction = state & 3
    if (index > 0) addOwner(isHorizontal(direction) ? horizontalOwners : verticalOwners, cells[index], key)
    if (index > 0 && direction !== (states[index - 1] & 3)) addOwner(bendOwners, cells[index - 1], key)
  })

  const start = startPorts.find((port) => port.x === points[0][0] && port.y === points[0][1])
  points.unshift([start.edgeX, start.y])
  points.push([goal.edgeX, goal.y])
  // Jäta alles ainult pöördepunktid
  const corners = points.filter((point, index) => {
    if (index === 0 || index === points.length - 1) return true
    const [previous, next] = [points[index - 1], points[index + 1]]
    return !(previous[0] === point[0] && point[0] === next[0]) && !(previous[1] === point[1] && point[1] === next[1])
  })
  totalLength += cells.length
  totalBends += corners.length - 2
  return roundedPath(corners)
}

// Murdjoon ümarate nurkadega: iga pöördepunkti asemel lühike kaar (raadius kuni pool lühemast lõigust)
function roundedPath(corners) {
  let path = `M${corners[0][0]},${corners[0][1]}`
  for (let i = 1; i < corners.length - 1; i++) {
    const [previous, point, next] = [corners[i - 1], corners[i], corners[i + 1]]
    const lengthIn = Math.abs(point[0] - previous[0]) + Math.abs(point[1] - previous[1])
    const lengthOut = Math.abs(next[0] - point[0]) + Math.abs(next[1] - point[1])
    const radius = Math.min(CORNER_RADIUS, lengthIn / 2, lengthOut / 2)
    const before = [point[0] - (Math.sign(point[0] - previous[0]) * radius), point[1] - (Math.sign(point[1] - previous[1]) * radius)]
    const after = [point[0] + (Math.sign(next[0] - point[0]) * radius), point[1] + (Math.sign(next[1] - point[1]) * radius)]
    path += ` L${before[0]},${before[1]} Q${point[0]},${point[1]} ${after[0]},${after[1]}`
  }
  const last = corners[corners.length - 1]
  return `${path} L${last[0]},${last[1]}`
}

// Lühemad seosed enne — neil on vaba ruumi rohkem
const tableDistance = (foreignKey) => {
  const [from, to] = [tables.get(foreignKey.from), tables.get(foreignKey.to)]
  return Math.abs(from.x - to.x) + Math.abs(rowY(from, foreignKey.column) - rowY(to, foreignKey.toColumn))
}
const routingOrder = [...foreignKeys].sort((a, b) => tableDistance(a) - tableDistance(b))

const edges = routingOrder.map((foreignKey) => {
  const path = routeEdge(foreignKey)
  if (!path) throw new Error(`Seosele ${foreignKey.from}.${foreignKey.column} ei leitud teed`)
  return `<path class="edge" data-from="${foreignKey.from}" data-to="${foreignKey.to}" d="${path}" marker-end="url(#arrow)"/>`
})

// ---------- Tabelid ----------
const tableElements = [...tables.values()].map((table) => {
  const foreignKeyColumns = foreignKeys.filter((foreignKey) => foreignKey.from === table.name).map((foreignKey) => foreignKey.column)
  const rows = table.columns.map((column) => {
    const y = rowY(table, column.name) + 4
    const key = column.name === 'id' ? 'PK' : foreignKeyColumns.includes(column.name) ? 'FK' : table.uniques.includes(column.name) ? 'UQ' : ''
    const nullable = column.nullable ? ' nullable' : ''
    return (
      (key ? `<text class="key key-${key.toLowerCase()}" x="${table.x + 10}" y="${y}">${key}</text>` : '') +
      `<text class="column${key === 'FK' ? ' column-fk' : ''}${nullable}" x="${table.x + 36}" y="${y}">${escape(column.name)}</text>` +
      `<text class="type${nullable}" x="${table.x + TABLE_WIDTH - 10}" y="${y}" text-anchor="end">${escape(column.type)}${column.nullable ? '?' : ''}</text>`
    )
  })
  return (
    `<g class="db-table" data-table="${table.name}">` +
    `<rect class="table-box" x="${table.x}" y="${table.y}" width="${TABLE_WIDTH}" height="${table.height}" rx="8"/>` +
    `<path class="table-header" d="M${table.x},${table.y + HEADER_HEIGHT} v-${HEADER_HEIGHT - 8} a8,8 0 0 1 8,-8 h${TABLE_WIDTH - 16} a8,8 0 0 1 8,8 v${HEADER_HEIGHT - 8} z"/>` +
    `<text class="table-name" x="${table.x + 10}" y="${table.y + 20}">${escape(table.name)}</text>` +
    rows.join('') +
    `</g>`
  )
})

const svg = `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 ${width} ${height}" width="${width}" height="${height}">
<!-- Genereeritud: npm run db-schema (frontend/scripts/generate-db-schema.mjs). Käsitsi ära muuda. -->
<defs><marker id="arrow" viewBox="0 0 8 8" refX="8" refY="4" markerWidth="7" markerHeight="7" orient="auto"><path class="edge-arrow" d="M0,0 L8,4 L0,8 z"/></marker></defs>
<g class="edges">${edges.join('\n')}</g>
${tableElements.join('\n')}
</svg>
`

writeFileSync(outFile, svg)
// Kvaliteedi kontroll: ristumised (horisontaalne ja vertikaalne joon samas ruudus), eri joonte kattumised,
// kõrvuti jooksvad jooned, joonte kogupikkus (ruutudes) ja pöörded
const crossings = [...horizontalOwners.keys()].filter((cell) => verticalOwners.has(cell) && !bendOwners.has(cell)).length
const overlaps = [...horizontalOwners.values(), ...verticalOwners.values()].filter((owners) => owners.size > 1).length
const adjacentCells = [
  ...[...horizontalOwners].filter(([cell, owners]) => [...owners].some((key) => hasParallelNeighbour(cell, true, key))),
  ...[...verticalOwners].filter(([cell, owners]) => [...owners].some((key) => hasParallelNeighbour(cell, false, key))),
].length
console.log(
  `db-schema.svg: ${tables.size} tabelit, ${foreignKeys.length} seost, ${crossings} ristumist, ` +
    `${overlaps} kattuvat ruutu, ${adjacentCells} ruutu kõrvuti jooksvaid jooni, ` +
    `pikkus ${totalLength}, pöördeid ${totalBends}, ${width}x${height}`,
)
