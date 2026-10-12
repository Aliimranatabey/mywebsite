<template>
  <div class="cover" :style="{ aspectRatio: ratio }">
    <img
      v-if="cover.src && !failed"
      :src="cover.src"
      :alt="post.title"
      :loading="eager ? 'eager' : 'lazy'"
      decoding="async"
      class="cover-img"
      @error="failed = true"
    />
    <!-- Resim yoksa ya da açılmazsa: başlığa özgü geçişli kapak -->
    <div v-else class="cover-generated" :style="{ background: cover.gradient }">
      <span class="cover-letter">{{ initial }}</span>
    </div>
  </div>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import { resolveCover } from '@/utils/cover'

const props = defineProps({
  post: { type: Object, required: true },
  ratio: { type: String, default: '16 / 9' },
  eager: { type: Boolean, default: false },
})

const failed = ref(false)
const cover = computed(() => resolveCover(props.post))
const initial = computed(() => (props.post.title || '?').trim().charAt(0).toLocaleUpperCase('tr-TR'))

// Kapak değişirse (düzenleme sonrası) eski hata durumunu unut
watch(() => cover.value.src, () => (failed.value = false))
</script>

<style scoped>
.cover {
  position: relative;
  width: 100%;
  overflow: hidden;
  background: var(--bg-subtle);
}

.cover-img {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.6s var(--ease);
}

.cover-generated {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: flex-end;
  justify-content: flex-end;
  padding: 1rem 1.25rem;
  transition: transform 0.6s var(--ease);
}

.cover-letter {
  font-family: var(--font-display);
  font-size: clamp(3.5rem, 9vw, 6rem);
  font-weight: 400;
  line-height: 0.8;
  letter-spacing: -0.03em;
  color: rgba(255, 255, 255, 0.85);
  text-shadow: 0 4px 30px rgba(0, 0, 0, 0.18);
  user-select: none;
}
</style>
