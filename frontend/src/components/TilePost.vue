<template>
  <!-- Görsel kutunun tamamı; üstünde sadece başlık. Kutunun kendisi bağlantı. -->
  <router-link :to="`/blog/${post.id}`" class="tile">
    <PostCover :post="post" ratio="4 / 3" />
    <h3 class="tile-title">{{ post.title }}</h3>
  </router-link>
</template>

<script setup>
import PostCover from '@/components/PostCover.vue'

defineProps({
  post: { type: Object, required: true },
})
</script>

<style scoped>
.tile {
  position: relative;
  display: block;
  overflow: hidden;
  color: #fff;
}

.tile:hover {
  text-decoration: none;
}

/* Alttan hafif karartma: beyaz zeminli görsellerde de başlık okunsun */
.tile::after {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(to top, rgba(0, 0, 0, 0.72) 0%, rgba(0, 0, 0, 0.4) 32%, transparent 62%);
  pointer-events: none;
}

.tile :deep(.cover-img),
.tile :deep(.cover-generated) {
  transition: transform 0.8s var(--ease);
}

.tile:hover :deep(.cover-img),
.tile:hover :deep(.cover-generated) {
  transform: scale(1.04);
}

/* Üretilmiş kapaktaki baş harf başlıkla yarışmasın */
.tile :deep(.cover-letter) {
  display: none;
}

.tile-title {
  position: absolute;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 1;
  max-width: 34rem;
  margin: 0 auto;
  padding: 0 1.5rem clamp(1.5rem, 3.5vw, 2.5rem);
  font-family: var(--font-display);
  font-optical-sizing: auto;
  font-size: clamp(1.5rem, 2.6vw, 2.25rem);
  font-weight: 450;
  line-height: 1.1;
  letter-spacing: -0.02em;
  text-align: center;
  text-wrap: balance;
  text-shadow: 0 1px 16px rgba(0, 0, 0, 0.45);
}
</style>
