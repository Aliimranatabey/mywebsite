<template>
  <div class="form-page">
    <div class="form-card surface">
      <h1 class="h2">Tekrar hoş geldin</h1>
      <p class="muted mt-2 mb-8">Yazılarını yönetmek için giriş yap.</p>

      <v-alert v-if="error" type="error" class="mb-6">{{ error }}</v-alert>

      <v-form @submit.prevent="submit">
        <label class="field-label">E-posta</label>
        <v-text-field
          v-model="email"
          type="email"
          autocomplete="email"
          :error-messages="fieldErrors.email"
          hide-details="auto"
          class="mb-5"
        />

        <label class="field-label">Şifre</label>
        <v-text-field
          v-model="password"
          type="password"
          autocomplete="current-password"
          :error-messages="fieldErrors.password"
          hide-details="auto"
          class="mb-7"
        />

        <button type="submit" class="btn btn-primary btn-block" :disabled="auth.loading">
          {{ auth.loading ? 'Giriş yapılıyor…' : 'Giriş yap' }}
        </button>
      </v-form>

      <p v-if="auth.registrationOpen" class="form-alt">
        Hesabın yok mu? <router-link to="/register">Kayıt ol</router-link>
      </p>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const auth = useAuthStore()
const router = useRouter()
const route = useRoute()

const email = ref('')
const password = ref('')
const error = ref('')
const fieldErrors = reactive({ email: [], password: [] })

async function submit() {
  error.value = ''
  fieldErrors.email = []
  fieldErrors.password = []

  try {
    await auth.login(email.value, password.value)
    router.push(route.query.redirect || '/blog')
  } catch (e) {
    error.value = e.message
    if (e.fieldErrors) {
      fieldErrors.email = e.fieldErrors.email ? [e.fieldErrors.email] : []
      fieldErrors.password = e.fieldErrors.password ? [e.fieldErrors.password] : []
    }
  }
}
</script>
