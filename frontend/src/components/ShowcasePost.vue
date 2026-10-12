<template>
  <section class="showcase" :class="`tone-${tone}`">
    <div class="container showcase-text">
      <p class="showcase-meta">
        <span v-if="post.category" class="showcase-category">{{ post.category }}</span>
        <time :datetime="post.createdAt">{{ formatDate(post.createdAt) }}</time>
      </p>
      <h2 class="showcase-title">{{ post.title }}</h2>
      <p class="showcase-excerpt">{{ excerpt(post.content, 110) }}</p>
      <router-link :to="`/blog/${post.id}`" class="btn btn-primary">Yazıyı oku</router-link>
    </div>

    <!-- Görsel, ekrana girerken hafifçe büyüyüp netleşerek yerine oturur -->
    <router-link
      ref="mediaLink"
      :to="`/blog/${post.id}`"
      class="container showcase-media"
      tabindex="-1"
      aria-hidden="true"
    >
      <div class="showcase-frame">
        <PostCover :post="post" ratio="16 / 9" :eager="eager" />
      </div>
    </router-link>
  </section>
</template>

<script setup>
import { computed, ref } from 'vue'
import PostCover from '@/components/PostCover.vue'
import { useScrollProgress } from '@/composables/useScrollProgress'
import { excerpt, formatDate } from '@/utils/text'

defineProps({
  post: { type: Object, required: true },
  /** Blok zemini: 'a' (beyaz/siyah), 'b' (açık gri/koyu gri), 'inverse' (ters renk) */
  tone: { type: String, default: 'a' },
  eager: { type: Boolean, default: false },
})

const mediaLink = ref(null)
const media = computed(() => mediaLink.value?.$el ?? null)
useScrollProgress(media)
</script>

<style scoped>
.showcase {
  padding: clamp(3.5rem, 8vw, 5.5rem) 0 clamp(2.5rem, 6vw, 4rem);
  overflow: hidden;
  text-align: center;
}

.tone-a {
  background: var(--tone-a);
}

.tone-b {
  background: var(--tone-b);
}

.showcase-text {
  display: flex;
  flex-direction: column;
  align-items: center;
  max-width: 48rem;
}

.showcase-meta {
  display: flex;
  gap: 0.75rem;
  font-size: 0.875rem;
  color: var(--text-muted);
  margin: 0;
}

.showcase-category {
  font-weight: 600;
  color: var(--accent);
}

.showcase-title {
  font-family: var(--font-display);
  font-optical-sizing: auto;
  font-size: clamp(2.25rem, 6vw, 4.25rem);
  font-weight: 450;
  line-height: 1.04;
  letter-spacing: -0.028em;
  color: var(--text);
  margin: 0.75rem 0 0;
  text-wrap: balance;
}

.showcase-excerpt {
  font-size: clamp(1.0625rem, 2vw, 1.3125rem);
  line-height: 1.45;
  color: var(--text-muted);
  max-width: 36rem;
  margin: 1rem 0 1.75rem;
  text-wrap: pretty;
}

.showcase-media {
  display: block;
  max-width: 76rem;
  margin-top: clamp(2.5rem, 6vw, 3.5rem);
}

.showcase-media:hover {
  text-decoration: none;
}

/* --p: 0 = görsel ekranın altına yeni değdi, ~0.35 = tam yerine oturdu.
   Hareket azaltma açıksa --p hiç yazılmaz, varsayılan 1 ile sabit durur. */
.showcase-frame {
  border-radius: var(--radius-lg);
  overflow: hidden;
  transform: scale(clamp(0.88, calc(0.88 + var(--p, 1) * 0.34), 1));
  opacity: clamp(0.35, calc(0.35 + var(--p, 1) * 2), 1);
  filter: blur(clamp(0px, calc((0.3 - var(--p, 1)) * 24px), 8px));
  will-change: transform, opacity, filter;
}

@media (prefers-reduced-motion: reduce) {
  .showcase-frame {
    transform: none;
    opacity: 1;
    filter: none;
  }
}
</style>
