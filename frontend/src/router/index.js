import { createRouter, createWebHistory } from 'vue-router'

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
      meta: { title: 'Yeni Blog' },
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
    { path: '/:pathMatch(.*)*', redirect: '/' },
  ],
  scrollBehavior() {
    return { top: 0 }
  },
})

router.afterEach((to) => {
  document.title = to.meta?.title ? `${to.meta.title} • Ali İmran` : 'Ali İmran'
})

export default router
