<template>
  <div>
    <!-- Kapak varsa önizleme -->
    <div v-if="modelValue" class="preview">
      <img :src="modelValue" alt="Kapak önizlemesi" />
      <div class="preview-actions">
        <button type="button" class="btn btn-secondary btn-sm" @click="pick" :disabled="uploading">
          Değiştir
        </button>
        <button type="button" class="btn btn-secondary btn-sm" @click="remove" :disabled="uploading">
          Kaldır
        </button>
      </div>
    </div>

    <!-- Kapak yoksa sürükle-bırak alanı -->
    <div
      v-else
      class="drop"
      :class="{ 'drop--active': dragging, 'drop--busy': uploading }"
      role="button"
      tabindex="0"
      @click="pick"
      @keydown.enter.prevent="pick"
      @keydown.space.prevent="pick"
      @dragover.prevent="dragging = true"
      @dragleave.prevent="dragging = false"
      @drop.prevent="onDrop"
    >
      <v-progress-circular v-if="uploading" indeterminate color="primary" size="28" />
      <template v-else>
        <span class="drop-icon mdi mdi-image-plus-outline" aria-hidden="true"></span>
        <p class="drop-title">Kapak görseli ekle</p>
        <p class="drop-hint">Sürükleyip bırak ya da tıkla · JPG, PNG, WEBP · en fazla 5 MB</p>
        <p class="drop-hint">Eklemezsen yazıdaki ilk resim kapak olur.</p>
      </template>
    </div>

    <p v-if="error" class="error">{{ error }}</p>

    <input
      ref="input"
      type="file"
      accept="image/jpeg,image/png,image/webp"
      class="d-none"
      @change="onSelect"
    />
  </div>
</template>

<script setup>
import { ref } from 'vue'
import api from '@/services/api'

defineProps({
  modelValue: { type: String, default: '' },
})
const emit = defineEmits(['update:modelValue'])

const MAX_BYTES = 5 * 1024 * 1024
const ALLOWED = ['image/jpeg', 'image/png', 'image/webp']

const input = ref(null)
const dragging = ref(false)
const uploading = ref(false)
const error = ref('')

const pick = () => input.value?.click()

const remove = () => {
  error.value = ''
  emit('update:modelValue', '')
}

const onSelect = (event) => {
  const file = event.target.files?.[0]
  event.target.value = '' // aynı dosya tekrar seçilebilsin
  if (file) upload(file)
}

const onDrop = (event) => {
  dragging.value = false
  const file = event.dataTransfer?.files?.[0]
  if (file) upload(file)
}

async function upload(file) {
  error.value = ''

  // Hızlı geri bildirim için önden kontrol; asıl kontrol sunucuda (dosya imzası)
  if (!ALLOWED.includes(file.type)) {
    error.value = 'Sadece JPG, PNG veya WEBP yükleyebilirsin.'
    return
  }
  if (file.size > MAX_BYTES) {
    error.value = 'Görsel en fazla 5 MB olabilir.'
    return
  }

  uploading.value = true
  try {
    const { data } = await api.uploadImage(file)
    emit('update:modelValue', data.url)
  } catch (e) {
    error.value = e.message
  } finally {
    uploading.value = false
  }
}
</script>

<style scoped>
.drop {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 0.25rem;
  min-height: 11rem;
  padding: 1.5rem;
  text-align: center;
  border: 1.5px dashed var(--border-strong);
  border-radius: var(--radius);
  background: var(--bg-subtle);
  cursor: pointer;
  transition:
    border-color 0.2s ease,
    background-color 0.2s ease;
}

.drop:hover,
.drop--active {
  border-color: var(--accent-solid);
  background: var(--accent-soft);
}

.drop--busy {
  cursor: progress;
}

.drop-icon {
  font-size: 2rem;
  color: var(--accent);
  margin-bottom: 0.25rem;
}

.drop-title {
  font-weight: 600;
  color: var(--text);
  margin: 0;
}

.drop-hint {
  font-size: 0.8125rem;
  color: var(--text-muted);
  margin: 0;
}

.preview {
  position: relative;
  border-radius: var(--radius);
  overflow: hidden;
  border: 1px solid var(--border);
}

.preview img {
  display: block;
  width: 100%;
  aspect-ratio: 16 / 9;
  object-fit: cover;
}

.preview-actions {
  position: absolute;
  right: 0.75rem;
  bottom: 0.75rem;
  display: flex;
  gap: 0.5rem;
}

.error {
  font-size: 0.875rem;
  color: var(--danger);
  margin: 0.6rem 0 0;
}
</style>
