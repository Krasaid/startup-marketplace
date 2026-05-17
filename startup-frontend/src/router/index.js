import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const routes = [
  {
    path: '/',
    name: 'feed',
    component: () => import('../views/FeedView.vue')
  },
  {
    path: '/stand/:slug',
    name: 'stand',
    component: () => import('../views/StandView.vue')
  },
  {
    path: '/login',
    name: 'login',
    component: () => import('../views/LoginView.vue'),
    meta: { guestOnly: true }
  },
  {
    path: '/registro',
    name: 'registro',
    component: () => import('../views/RegistroView.vue'),
    meta: { guestOnly: true }
  },
  {
    path: '/mi-stand',
    name: 'mi-stand',
    component: () => import('../views/MiStandView.vue'),
    meta: { requiresAuth: true, requiresEmprendedor: true }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior: () => ({ top: 0 })
})

// Guard global
router.beforeEach((to) => {
  const auth = useAuthStore()

  if (to.meta.requiresAuth && !auth.isAuthenticated) {
    return { name: 'login' }
  }
  if (to.meta.requiresEmprendedor && !auth.isEmprendedor && !auth.isAdmin) {
    return { name: 'feed' }
  }
  if (to.meta.guestOnly && auth.isAuthenticated) {
    return { name: 'feed' }
  }
})

export default router