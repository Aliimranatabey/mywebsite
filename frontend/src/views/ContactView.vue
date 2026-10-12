<template>
  <div class="form-page">
    <div class="form-card form-card--wide surface">
      <h1 class="h2">Bana yaz</h1>
      <p class="muted mt-2 mb-8">
        Soru, öneri ya da birlikte çalışma fikri, hepsine açığım. Genelde birkaç gün içinde dönüyorum.
      </p>

      <v-alert v-if="successMessage" type="success" class="mb-6">{{ successMessage }}</v-alert>
      <v-alert v-if="errorMessage" type="error" class="mb-6">{{ errorMessage }}</v-alert>

      <v-form @submit.prevent="submitForm">
        <div class="field-row">
          <div>
            <label class="field-label">Adın</label>
            <v-text-field v-model="formData.name" hide-details="auto" />
          </div>
          <div>
            <label class="field-label">E-postan</label>
            <v-text-field v-model="formData.email" type="email" hide-details="auto" />
          </div>
        </div>

        <label class="field-label mt-5">Mesajın</label>
        <v-textarea v-model="formData.message" rows="6" hide-details="auto" class="mb-7" />

        <button type="submit" class="btn btn-primary btn-block" :disabled="loading">
          {{ loading ? 'Gönderiliyor…' : 'Gönder' }}
        </button>
      </v-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import api from '@/services/api'

const formData = reactive({ name: '', email: '', message: '' })

const loading = ref(false)
const successMessage = ref('')
const errorMessage = ref('')

const submitForm = async () => {
  loading.value = true
  successMessage.value = ''
  errorMessage.value = ''

  try {
    const response = await api.sendMessage(formData)
    // Backend mailin gerçekten gidip gitmediğini söylüyor; olduğu gibi aktarıyoruz
    successMessage.value = response.data?.message || 'Mesajın iletildi.'
    formData.name = ''
    formData.email = ''
    formData.message = ''
  } catch (e) {
    errorMessage.value = e.message
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.field-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1rem;
}

@media (max-width: 600px) {
  .field-row {
    grid-template-columns: 1fr;
  }
}
</style>
