import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: () => import('../views/HomeView.vue'),
      meta: { title: 'Anasayfa' },
    },
    {
      path: '/blog',
      name: 'blog-list',
      component: () => import('../views/BlogView.vue'),
      meta: { title: 'Blog' },
    },
    {
      path: '/blog/new',
      name: 'blog-create',
      component: () => import('../views/BlogCreateView.vue'),
      meta: { title: 'Yeni Blog', requiresAuth: true },
    },
    {
      path: '/blog/:id',
      name: 'blog-detail',
      component: () => import('../views/BlogPostView.vue'),
      meta: { title: 'Yazı Detayı' },
    },
    {
      path: '/resume',
      name: 'resume',
      component: () => import('../views/ResumeView.vue'),
      meta: { title: 'Özgeçmiş' },
    },
    {
      path: '/contact',
      name: 'contact',
      component: () => import('../views/ContactView.vue'),
      meta: { title: 'İletişim' },
    },
    {
      path: '/login',
      name: 'login',
      component: () => import('../views/LoginView.vue'),
      meta: { title: 'Giriş Yap', guestOnly: true },
    },
    {
      path: '/register',
      name: 'register',
      component: () => import('../views/RegisterView.vue'),
      meta: { title: 'Kayıt Ol', guestOnly: true },
    },
    { path: '/:pathMatch(.*)*', redirect: '/' },
  ],
  scrollBehavior(to, from, savedPosition) {
    if (savedPosition) return savedPosition
    if (to.hash) {
      // Sayfa geçişi (fade, out-in) bitmeden hedef bölüm DOM'da olmuyor; kısa bekle
      const delay = to.path === from.path ? 0 : 320
      return new Promise((resolve) =>
        setTimeout(() => resolve({ el: to.hash, top: 64, behavior: 'smooth' }), delay),
      )
    }
    return { top: 0 }
  },
})

/**
 * Rota koruması.
 *
 * Not: bu sadece arayüz kolaylığı — gerçek koruma backend'de.
 * Birisi guard'ı atlasa bile API 401/403 döner.
 */
router.beforeEach(async (to) => {
  const auth = useAuthStore()

  // Sayfa ilk açılışında oturumu sunucudan öğren
  await auth.restore()

  if (to.meta.requiresAuth && !auth.isLoggedIn) {
    return { name: 'login', query: { redirect: to.fullPath } }
  }

  // Girişliyken giriş/kayıt sayfasına gitmenin anlamı yok
  if (to.meta.guestOnly && auth.isLoggedIn) {
    return { name: 'blog-list' }
  }

  return true
})

router.afterEach((to) => {
  document.title = to.meta?.title ? `${to.meta.title} • Ali İmran` : 'Ali İmran'
})

export default router
