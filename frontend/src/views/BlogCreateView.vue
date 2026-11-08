<template>
  <div class="container narrow page">
    <header class="head">
      <h1 class="h1">Yeni yazı</h1>
      <p class="lead mt-3">Başlığı net, girişi meraklandırıcı tut. Gerisi yazarken gelir.</p>
    </header>

    <v-alert v-if="error" type="error" class="mb-6">{{ error }}</v-alert>

    <v-form @submit.prevent="saveBlog">
      <label class="field-label">Kapak görseli</label>
      <CoverUploader v-model="form.coverImageUrl" class="mb-6" />

      <label class="field-label">Başlık</label>
      <v-text-field
        v-model="form.title"
        placeholder="Örn. Docker'ı neden sevdim"
        :error-messages="fieldErrors.title"
        hide-details="auto"
        class="mb-6"
      />

      <label class="field-label">Kategori</label>
      <v-text-field
        v-model="form.category"
        placeholder="Teknoloji, Yazılım, Hayat…"
        :error-messages="fieldErrors.category"
        hide-details="auto"
        class="mb-6"
      />

      <label class="field-label">İçerik</label>
      <div class="editor-box mb-8">
        <QuillEditor
          theme="snow"
          v-model:content="form.content"
          contentType="html"
          toolbar="full"
          placeholder="Yazmaya başla…"
        />
      </div>

      <div class="form-actions">
        <button type="submit" class="btn btn-primary" :disabled="saving">
          {{ saving ? 'Yayınlanıyor…' : 'Yayınla' }}
        </button>
        <router-link to="/blog" class="btn btn-ghost">Vazgeç</router-link>
      </div>
    </v-form>
  </div>
</template>

<script setup>
import QuillEditor from '@/components/QuillEditorLazy'
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import api from '@/services/api'
import CoverUploader from '@/components/CoverUploader.vue'

const router = useRouter()
const saving = ref(false)
const error = ref('')

const form = reactive({ title: '', category: '', content: '', coverImageUrl: '' })
const fieldErrors = reactive({ title: [], category: [], content: [] })

const saveBlog = async () => {
  saving.value = true
  error.value = ''
  Object.keys(fieldErrors).forEach((key) => (fieldErrors[key] = []))

  try {
    const { data } = await api.createBlog({ ...form, coverImageUrl: form.coverImageUrl || null })
    router.push(`/blog/${data.id}`)
  } catch (e) {
    error.value = e.message
    if (e.fieldErrors) {
      Object.keys(fieldErrors).forEach((key) => {
        fieldErrors[key] = e.fieldErrors[key] ? [e.fieldErrors[key]] : []
      })
    }
  } finally {
    saving.value = false
  }
}
</script>

<style scoped>
.narrow {
  max-width: calc(var(--measure) + 4rem);
}

.head {
  margin-bottom: 2.5rem;
}

.form-actions {
  display: flex;
  gap: 0.75rem;
}
</style>
