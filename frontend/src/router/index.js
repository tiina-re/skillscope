import { createRouter, createWebHistory } from 'vue-router'
import MainLayout from '@/navigation/MainLayout.vue'
import DashboardView from '@/views/DashboardView.vue'
import LoginView from '@/views/LoginView.vue'
import TestOverview from '@/views/TestOverview.vue'
import TestResultView from '@/views/TestResultView.vue'
import TestAttemptView from '@/views/TestAttemptView.vue'
import TestStartView from '@/views/TestStartView.vue'
import TestDetailView from '@/views/TestDetailView.vue'
import CompetenceView from '@/views/CompetenceView.vue'
import MyTestsView from '@/views/MyTestsView.vue'
import UsersView from '@/views/UsersView.vue'

import TestCreateView from '@/views/TestCreateView.vue'
import QuestionBankView from '@/views/QuestionBankView.vue'
import AiQuestionBankView from '@/views/AiQuestionBankView.vue'
import SessionStorageService from '@/services/SessionStorageService.js'

const toNumber = (param) => (route) => ({ [param]: Number(route.params[param]) })

// Lehed, mida näevad ainult ADMIN ja HALDUR. Ilma meta.roles'ita lehed on kõigile sisselogitutele.
const STAFF = { roles: ['ADMIN', 'HALDUR'] }

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      component: MainLayout,
      children: [
        { path: 'dashboard', name: 'dashboardRoute', component: DashboardView, meta: STAFF },
        { path: 'tests/new', name: 'testCreateRoute', component: TestCreateView, meta: STAFF },
        { path: 'tests', name: 'testsRoute', component: TestOverview, meta: STAFF },
        { path: 'my-tests', name: 'myTestsRoute', component: MyTestsView },
        { path: 'test-result', name: 'testResultRoute', component: TestResultView },
        { path: 'competences', name: 'competenceView', component: CompetenceView, meta: STAFF },
        { path: 'questions', name: 'questionBankView', component: QuestionBankView, meta: STAFF },
        { path: 'users', name: 'usersRoute', component: UsersView, meta: STAFF },
        { path: 'questions/ai', name: 'aiQuestionBankView', component: AiQuestionBankView, meta: STAFF },
        {
          path: 'tests/:testId(\\d+)',
          name: 'testDetailRoute',
          component: TestDetailView,
          props: toNumber('testId'),
          meta: STAFF,
        },
        {
          path: 'tests/:testId/start',
          name: 'testStartRoute',
          component: TestStartView,
          props: toNumber('testId'),
        },
        {
          path: 'tests/:testId/attempt',
          name: 'testAttemptRoute',
          component: TestAttemptView,
          props: toNumber('testId'),
        },
      ],
    },
    {
      path: '/login',
      name: 'loginRoute',
      component: LoginView,
    },
  ],
})

router.beforeEach((to) => {
  if (to.name === 'loginRoute') return true
  if (!SessionStorageService.userIsLoggedIn()) return { name: 'loginRoute' }

  // Õigusteta lehelt suunatakse "Minu testid" lehele - see on kõigile sisselogitutele avatud
  if (!SessionStorageService.hasRole(to.meta.roles)) return { name: 'myTestsRoute' }
})

export default router
