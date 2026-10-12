<template>
  <div class="container list page">
    <header class="head">
      <div>
        <h1 class="h1">Yazılar</h1>
        <p class="lead mt-3">Yazılım, veri ve öğrenme üzerine notlar.</p>
      </div>

      <router-link v-if="auth.isLoggedIn" to="/blog/new" class="btn btn-primary">
        <span class="mdi mdi-pencil-outline" aria-hidden="true"></span>
        Yeni yazı
      </router-link>
      <router-link v-else to="/login" class="btn btn-secondary">
        Yazmak için giriş yap
      </router-link>
    </header>

    <v-alert v-if="error" type="error" class="mb-8">{{ error }}</v-alert>

    <div v-if="loading" class="state">
      <v-progress-circular indeterminate color="primary" size="28" width="2" />
    </div>

    <div v-else-if="posts.length === 0" class="state">
      <p class="h2">Henüz yazı yok</p>
      <p class="muted mt-2">İlk yazıyı sen yaz.</p>
    </div>

    <div v-else class="rows">
      <PostCard v-for="post in posts" :key="post.id" :post="post" />
    </div>

    <nav v-if="totalPages > 1" class="pager">
      <button class="btn btn-secondary btn-sm" :disabled="currentPage === 1" @click="goTo(currentPage - 1)">
        <span class="mdi mdi-arrow-left" aria-hidden="true"></span> Önceki
      </button>
      <span class="muted">{{ currentPage }} / {{ totalPages }}</span>
      <button class="btn btn-secondary btn-sm" :disabled="currentPage === totalPages" @click="goTo(currentPage + 1)">
        Sonraki <span class="mdi mdi-arrow-right" aria-hidden="true"></span>
      </button>
    </nav>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import api from '@/services/api'
import { useAuthStore } from '@/stores/auth'
import PostCard from '@/components/PostCard.vue'

const auth = useAuthStore()

const posts = ref([])
const loading = ref(true)
const error = ref('')
// Sayfalar kullanıcıya 1'den başlar, backend 0'dan
const currentPage = ref(1)
const totalPages = ref(1)

const fetchPosts = async () => {
  loading.value = true
  error.value = ''
  try {
    const { data } = await api.getBlogs(currentPage.value - 1, 12)
    posts.value = data.content ?? []
    totalPages.value = data.totalPages || 1
  } catch (e) {
    error.value = e.message
  } finally {
    loading.value = false
  }
}

const goTo = (page) => {
  currentPage.value = page
  window.scrollTo({ top: 0, behavior: 'smooth' })
  fetchPosts()
}

onMounted(fetchPosts)
</script>

<style scoped>
.list {
  max-width: 56rem;
}

.head {
  display: flex;
  flex-wrap: wrap;
  align-items: flex-end;
  justify-content: space-between;
  gap: 1.5rem;
  margin-bottom: 2.5rem;
}

.rows {
  border-bottom: 1px solid var(--border);
}

.state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 4rem 1rem;
  text-align: center;
}

.pager {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 1.25rem;
  margin-top: 3rem;
}
</style>
