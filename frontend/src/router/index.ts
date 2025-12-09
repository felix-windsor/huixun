import { createRouter, createWebHistory, type NavigationGuardNext, type RouteLocationNormalized } from 'vue-router'
import Login from '../pages/Login.vue'
import Dashboard from '../pages/Dashboard.vue'
import PdfUpload from '../pages/PdfUpload.vue'
import Fragments from '../pages/Fragments.vue'
import Courses from '../pages/Courses.vue'
import QuizManage from '../pages/QuizManage.vue'
import QuestionEdit from '../pages/QuestionEdit.vue'
import Attempt from '../pages/Attempt.vue'
import Analytics from '../pages/Analytics.vue'
import ComponentsDemo from '../pages/ComponentsDemo.vue'
import { useAuthStore } from '../stores/auth'

const routes = [
  { path: '/', redirect: '/dashboard' },
  { path: '/login', component: Login },
  { path: '/dashboard', component: Dashboard },
  { path: '/pdf-upload', component: PdfUpload },
  { path: '/fragments/:id', component: Fragments },
  { path: '/courses', component: Courses },
  { path: '/quiz/:id', component: QuizManage },
  { path: '/questions/:quizId', component: QuestionEdit },
  { path: '/attempt/:quizId', component: Attempt },
  { path: '/analytics', component: Analytics },
  { path: '/components-demo', component: ComponentsDemo }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to: RouteLocationNormalized, from: RouteLocationNormalized, next: NavigationGuardNext) => {
  const store = useAuthStore()
  const publicPaths = ['/login', '/components-demo']
  if (!publicPaths.includes(to.path) && !store.token) return next('/login')
  next()
})

export default router
