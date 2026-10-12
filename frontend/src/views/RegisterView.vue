<template>
  <div class="form-page">
    <div class="form-card surface">
      <h1 class="h2">Yazmaya başla</h1>
      <p class="muted mt-2 mb-8">
        Hesabını aç, yazılarını yayınla. Yazdıkların yalnızca sana ait; düzenlemek de silmek de senin elinde.
      </p>

      <v-alert v-if="error" type="error" class="mb-6">{{ error }}</v-alert>

      <v-form @submit.prevent="submit">
        <label class="field-label">Görünen ad</label>
        <v-text-field
          v-model="displayName"
          autocomplete="name"
          hint="Yazılarının altında bu isim görünür"
          persistent-hint
          :error-messages="fieldErrors.displayName"
          class="mb-5"
        />

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
          autocomplete="new-password"
          hint="En az 8 karakter"
          persistent-hint
          :error-messages="fieldErrors.password"
          class="mb-7"
        />

        <button type="submit" class="btn btn-primary btn-block" :disabled="auth.loading">
          {{ auth.loading ? 'Hesap açılıyor…' : 'Hesap aç' }}
        </button>
      </v-form>

      <p class="form-alt">
        Hesabın var mı? <router-link to="/login">Giriş yap</router-link>
      </p>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const auth = useAuthStore()
const router = useRouter()

const displayName = ref('')
const email = ref('')
const password = ref('')
const error = ref('')
const fieldErrors = reactive({ displayName: [], email: [], password: [] })

async function submit() {
  error.value = ''
  Object.keys(fieldErrors).forEach((key) => (fieldErrors[key] = []))

  try {
    await auth.register(email.value, displayName.value, password.value)
    router.push('/blog')
  } catch (e) {
    error.value = e.message
    if (e.fieldErrors) {
      Object.keys(fieldErrors).forEach((key) => {
        fieldErrors[key] = e.fieldErrors[key] ? [e.fieldErrors[key]] : []
      })
    }
  }
}
</script>
