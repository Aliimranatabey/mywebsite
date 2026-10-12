<template>
  <router-link :to="`/blog/${post.id}`" class="row">
    <div class="row-media">
      <PostCover :post="post" ratio="4 / 3" />
    </div>

    <div class="row-body">
      <p class="row-meta">
        <span v-if="post.category" class="row-category">{{ post.category }}</span>
        <time :datetime="post.createdAt">{{ formatDate(post.createdAt) }}</time>
      </p>
      <h3 class="row-title">{{ post.title }}</h3>
      <p class="row-excerpt">{{ excerpt(post.content, 160) }}</p>
      <p class="row-foot">{{ authorName }}, {{ readingTime(post.content) }} dk okuma</p>
    </div>
  </router-link>
</template>

<script setup>
import { computed } from 'vue'
import PostCover from '@/components/PostCover.vue'
import { excerpt, formatDate, readingTime } from '@/utils/text'

const props = defineProps({
  post: { type: Object, required: true },
})

const authorName = computed(() => props.post.authorDisplayName || 'Ali İmran Atabey')
</script>

<style scoped>
/* Apple Newsroom'daki gibi: küçük görsel solda, metin sağda, aralarında ince çizgi */
.row {
  display: grid;
  grid-template-columns: minmax(7rem, 14rem) 1fr;
  gap: clamp(1rem, 3vw, 2rem);
  align-items: center;
  padding: 1.5rem 0;
  color: inherit;
  border-top: 1px solid var(--border);
}

.row:hover {
  text-decoration: none;
}

.row-media {
  border-radius: var(--radius);
  overflow: hidden;
}

.row-media :deep(.cover-img),
.row-media :deep(.cover-generated) {
  transition: transform 0.6s var(--ease);
}

.row:hover .row-media :deep(.cover-img),
.row:hover .row-media :deep(.cover-generated) {
  transform: scale(1.03);
}

.row-media :deep(.cover-letter) {
  font-size: 2.5rem;
}

.row-body {
  min-width: 0;
}

.row-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 0.75rem;
  font-size: 0.8125rem;
  color: var(--text-muted);
  margin: 0;
}

.row-category {
  font-weight: 600;
  color: var(--accent);
}

.row-title {
  font-family: var(--font-display);
  font-size: clamp(1.25rem, 2.4vw, 1.6rem);
  font-weight: 450;
  line-height: 1.2;
  letter-spacing: -0.015em;
  color: var(--text);
  margin: 0.35rem 0 0;
  transition: color 0.2s ease;
}

.row:hover .row-title {
  color: var(--accent);
}

.row-excerpt {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  font-size: 0.9375rem;
  line-height: 1.5;
  color: var(--text-soft);
  margin: 0.5rem 0 0;
}

.row-foot {
  font-size: 0.8125rem;
  color: var(--text-muted);
  margin: 0.6rem 0 0;
}

@media (max-width: 600px) {
  .row {
    grid-template-columns: 6.5rem 1fr;
    align-items: start;
  }

  .row-excerpt,
  .row-foot {
    display: none;
  }
}
</style>
