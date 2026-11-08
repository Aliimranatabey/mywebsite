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

      <!-- KVKK md. 10 aydınlatma: kim, ne için, kime aktarılıyor, hakların neler -->
      <p class="privacy">
        <strong>Kişisel verilerin hakkında:</strong> Adın, e-postan ve mesajın, veri sorumlusu
        olarak Ali İmran Atabey tarafından yalnızca sana dönüş yapmak için işlenir. Mesaj bu
        sitenin veritabanında en fazla 1 yıl saklanıp silinir ve Google'ın e-posta hizmeti (Gmail) üzerinden bana
        iletilir; bu nedenle verin yurt dışındaki sunuculara aktarılabilir. Başka hiçbir amaçla
        kullanılmaz, kimseyle paylaşılmaz. Formu göndererek buna onay vermiş olursun.
        KVKK'nın 11. maddesindeki haklarını (öğrenme, düzeltme, silme vb.) kullanmak için bana
        bu form üzerinden ya da e-postayla yazabilirsin.
      </p>
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
.privacy {
  margin: 1.5rem 0 0;
  font-size: 0.75rem;
  line-height: 1.55;
  color: var(--text-muted);
}

.privacy strong {
  color: var(--text);
  font-weight: 600;
}

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
