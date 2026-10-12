<template>
  <span class="avatar" :style="{ width: size, height: size, fontSize: `calc(${size} * 0.42)`, background }">
    {{ initials }}
  </span>
</template>

<script setup>
import { computed } from 'vue'
import { gradientFor } from '@/utils/cover'

const props = defineProps({
  name: { type: String, default: '' },
  size: { type: String, default: '2rem' },
})

const initials = computed(() =>
  (props.name || '?')
    .split(/\s+/)
    .filter(Boolean)
    .slice(0, 2)
    .map((part) => part.charAt(0).toLocaleUpperCase('tr-TR'))
    .join(''),
)

// Her yazar için hep aynı renk
const background = computed(() => gradientFor(props.name))
</script>

<style scoped>
.avatar {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  border-radius: 50%;
  color: #fff;
  font-weight: 700;
  letter-spacing: 0.02em;
}
</style>
