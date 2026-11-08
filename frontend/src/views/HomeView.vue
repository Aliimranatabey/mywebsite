<template>
  <div class="home">
    <!-- Giriş: tek portre, tek cümle, bol boşluk -->
    <section class="intro">
      <div class="container intro-inner">
        <img
          :src="portrait"
          alt="Ali İmran Atabey"
          width="168"
          height="168"
          fetchpriority="high"
          class="intro-portrait"
        />
        <p class="intro-name">Ali İmran Atabey</p>
        <h1 class="intro-title" lang="en">Software Engineer</h1>
        <p class="lead intro-lead" lang="en">Software &amp; Database Specialist at Türk Telekom</p>
        <div class="intro-actions">
          <router-link to="/blog" class="btn btn-primary">Yazıları oku</router-link>
          <router-link to="/resume" class="btn btn-secondary">Hakkımda</router-link>
        </div>
      </div>
    </section>

    <div v-if="loading" class="state">
      <v-progress-circular indeterminate color="primary" size="28" width="2" />
    </div>

    <div v-else-if="posts.length === 0" class="container state">
      <h2 class="h2">Henüz yazı yok</h2>
      <p class="muted mt-2">İlk yazıyı sen yaz.</p>
      <router-link v-if="auth.writeLink" :to="auth.writeLink" class="btn btn-primary mt-6">
        Yazmaya başla
      </router-link>
    </div>

    <template v-else>
      <!-- Vitrin: görseli olan son yazılar, tam genişlik bloklar -->
      <ShowcasePost
        v-for="(post, i) in showcase"
        :key="post.id"
        :post="post"
        :tone="SHOWCASE_TONES[i]"
        :eager="i === 0"
      />

      <!-- Ardından 2×2 kutular; tüm liste "Yazılar" sayfasında -->
      <section v-if="tiles.length" class="tiles">
        <div class="tiles-grid">
          <TilePost v-for="post in tiles" :key="post.id" :post="post" />
        </div>
        <div class="tiles-more">
          <router-link to="/blog" class="btn btn-secondary">Tüm yazılar</router-link>
        </div>
      </section>
    </template>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import api from '@/services/api'
import { useAuthStore } from '@/stores/auth'
import { hasRealCover } from '@/utils/cover'
import ShowcasePost from '@/components/ShowcasePost.vue'
import TilePost from '@/components/TilePost.vue'
import portrait from '@/assets/portrait.jpg'

const SHOWCASE_TONES = ['b', 'inverse']
const TILE_COUNT = 4

const auth = useAuthStore()
const posts = ref([])
const loading = ref(true)

// Vitrine sadece gerçek görseli olanlar çıkar; üretilmiş kapak büyük ekranda sönük kalıyor
const showcase = computed(() => posts.value.filter(hasRealCover).slice(0, SHOWCASE_TONES.length))
// Kutular: vitrinde olmayanlar, görseli olanlar önce
const tiles = computed(() => {
  const shown = new Set(showcase.value.map((p) => p.id))
  const others = posts.value.filter((p) => !shown.has(p.id))
  const withCover = others.filter(hasRealCover)
  const without = others.filter((p) => !hasRealCover(p))
  return [...withCover, ...without].slice(0, TILE_COUNT)
})

onMounted(async () => {
  try {
    const { data } = await api.getBlogs(0, 12)
    posts.value = data.content ?? []
  } catch {
    posts.value = []
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
/* Ana sayfa blokları kendi zeminini taşıyor; alt bilgiyle arada boşluk kalmasın */
.home {
  margin-bottom: calc(-1 * clamp(4rem, 10vw, 7rem));
}

/* --- Giriş --- */
.intro {
  background: var(--tone-a);
  padding: clamp(3.5rem, 9vw, 6rem) 0 clamp(3.5rem, 9vw, 6rem);
  text-align: center;
}

.intro-inner {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.intro-portrait {
  width: clamp(7.5rem, 18vw, 10.5rem);
  height: auto;
  aspect-ratio: 1;
  object-fit: cover;
  border-radius: 50%;
  box-shadow: 0 0 0 1px var(--border);
}

.intro-name {
  font-size: 0.9375rem;
  font-weight: 500;
  color: var(--text-muted);
  margin: 1.5rem 0 0;
}

.intro-title {
  max-width: 50rem;
  font-family: var(--font-display);
  font-optical-sizing: auto;
  font-size: clamp(2.25rem, 5.5vw, 4rem);
  font-weight: 400;
  line-height: 1.06;
  letter-spacing: -0.028em;
  color: var(--text);
  margin: 0.6rem 0 0;
  text-wrap: balance;
}

.intro-lead {
  max-width: 34rem;
  margin-top: 1.25rem;
  text-wrap: pretty;
}

.intro-actions {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 0.75rem;
  margin-top: 2rem;
}

/* Açılıştaki tek koreografi: portre yerine oturur, ardından metin */
.intro-portrait {
  animation: settle 1s var(--ease) both;
}

.intro-name,
.intro-title,
.intro-lead,
.intro-actions {
  animation: rise 0.9s var(--ease) both;
  animation-delay: 0.15s;
}

.intro-lead,
.intro-actions {
  animation-delay: 0.25s;
}

@keyframes settle {
  from {
    opacity: 0;
    transform: scale(0.94);
  }
}

@keyframes rise {
  from {
    opacity: 0;
    transform: translateY(12px);
  }
}

/* --- Kutular --- */
.tiles {
  background: var(--tone-a);
  padding: 12px 12px clamp(4rem, 10vw, 7rem);
}

.tiles-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.tiles-more {
  display: flex;
  justify-content: center;
  margin-top: clamp(2.5rem, 6vw, 3.5rem);
}

@media (max-width: 760px) {
  .tiles-grid {
    grid-template-columns: 1fr;
  }
}

.state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 5rem 1rem 9rem;
  text-align: center;
}
</style>
