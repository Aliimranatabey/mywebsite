<template>
  <div class="page">
    <div v-if="loading" class="state">
      <v-progress-circular indeterminate color="primary" size="28" width="2" />
    </div>

    <!-- Okuma -->
    <article v-else-if="blog && !editMode">
      <header class="container narrow head">
        <router-link to="/blog" class="back">
          <span class="mdi mdi-arrow-left" aria-hidden="true"></span> Yazılar
        </router-link>

        <v-alert v-if="error" type="error" class="mb-6">{{ error }}</v-alert>

        <div class="meta-top">
          <span v-if="blog.category" class="badge">{{ blog.category }}</span>
          <span class="muted">{{ formatDate(blog.createdAt) }}</span>
        </div>

        <h1 class="h1 title">{{ blog.title }}</h1>

        <div class="byline">
          <AuthorAvatar :name="authorName" size="2.6rem" />
          <div>
            <p class="byline-name">{{ authorName }}</p>
            <p class="byline-sub">
              {{ readingTime(blog.content) }} dk okuma
              <template v-if="wasUpdated"> · {{ formatDate(blog.updatedAt) }} tarihinde güncellendi</template>
            </p>
          </div>

          <!-- Yalnızca yazının sahibine görünür -->
          <div v-if="blog.canEdit" class="owner-tools">
            <button class="btn btn-secondary btn-sm" @click="enableEditMode">
              <span class="mdi mdi-pencil-outline" aria-hidden="true"></span> Düzenle
            </button>
            <button class="btn btn-ghost btn-sm danger" @click="confirmDelete = true">
              <span class="mdi mdi-trash-can-outline" aria-hidden="true"></span> Sil
            </button>
          </div>
        </div>
      </header>

      <!-- Kapak: yüklenmiş kapak, yoksa (içerikte resim de yoksa) üretilmiş kapak.
           İçerikteki ilk resim zaten metinde göründüğü için burada tekrar edilmez. -->
      <div v-if="showBanner" ref="bannerEl" class="container wide banner">
        <PostCover :post="bannerPost" ratio="2 / 1" eager />
      </div>

      <!-- İçerik backend'de temizlenerek kaydediliyor (XSS koruması) -->
      <div class="container narrow">
        <div class="prose body" v-html="blog.content"></div>

        <footer class="foot">
          <div class="author-card surface">
            <AuthorAvatar :name="authorName" size="3rem" />
            <div>
              <p class="muted small">Yazan</p>
              <p class="byline-name">{{ authorName }}</p>
            </div>
            <router-link to="/blog" class="btn btn-secondary btn-sm ml-auto">Diğer yazılar</router-link>
          </div>
        </footer>
      </div>
    </article>

    <!-- Düzenleme -->
    <div v-else-if="blog && editMode" class="container narrow">
      <h1 class="h1 mb-8">Yazıyı düzenle</h1>

      <v-alert v-if="error" type="error" class="mb-6">{{ error }}</v-alert>

      <v-form @submit.prevent="saveBlog">
        <label class="field-label">Kapak görseli</label>
        <CoverUploader v-model="editForm.coverImageUrl" class="mb-6" />

        <label class="field-label">Başlık</label>
        <v-text-field v-model="editForm.title" :error-messages="fieldErrors.title" hide-details="auto" class="mb-6" />

        <label class="field-label">Kategori</label>
        <v-text-field v-model="editForm.category" :error-messages="fieldErrors.category" hide-details="auto" class="mb-6" />

        <label class="field-label">İçerik</label>
        <div class="editor-box mb-8">
          <QuillEditor theme="snow" v-model:content="editForm.content" contentType="html" toolbar="full" />
        </div>

        <div class="form-actions">
          <button type="submit" class="btn btn-primary" :disabled="saving">
            {{ saving ? 'Kaydediliyor…' : 'Kaydet' }}
          </button>
          <button type="button" class="btn btn-ghost" @click="cancelEdit" :disabled="saving">Vazgeç</button>
        </div>
      </v-form>
    </div>

    <div v-else class="state">
      <p class="h2">{{ error || 'Yazı bulunamadı.' }}</p>
      <router-link to="/blog" class="btn btn-secondary mt-6">Yazılara dön</router-link>
    </div>

    <!-- Silme onayı -->
    <v-dialog v-model="confirmDelete" max-width="420">
      <div class="dialog surface">
        <h2 class="h2">Yazıyı sil?</h2>
        <p class="muted mt-3">"{{ blog?.title }}" kalıcı olarak silinecek. Bu işlem geri alınamaz.</p>
        <div class="dialog-actions">
          <button class="btn btn-ghost" @click="confirmDelete = false" :disabled="deleting">Vazgeç</button>
          <button class="btn btn-danger" @click="deleteBlog" :disabled="deleting">
            {{ deleting ? 'Siliniyor…' : 'Sil' }}
          </button>
        </div>
      </div>
    </v-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import api from '@/services/api'
import { formatDate, readingTime } from '@/utils/text'
import { firstImage } from '@/utils/cover'
import PostCover from '@/components/PostCover.vue'
import AuthorAvatar from '@/components/AuthorAvatar.vue'
import CoverUploader from '@/components/CoverUploader.vue'
import { useScrollProgress } from '@/composables/useScrollProgress'

const route = useRoute()
const router = useRouter()

const blog = ref(null)
const bannerEl = ref(null)
useScrollProgress(bannerEl)
const loading = ref(true)
const editMode = ref(false)
const saving = ref(false)
const deleting = ref(false)
const confirmDelete = ref(false)
const error = ref('')

const editForm = reactive({ title: '', category: '', content: '', coverImageUrl: '' })
const fieldErrors = reactive({ title: [], category: [], content: [] })

const authorName = computed(() => blog.value?.authorDisplayName || 'Ali İmran Atabey')

const wasUpdated = computed(() => {
  if (!blog.value?.updatedAt || !blog.value?.createdAt) return false
  return new Date(blog.value.updatedAt) - new Date(blog.value.createdAt) > 1000
})

// İçerikte resim varsa ve kapak yüklenmemişse banner gösterme (resim iki kez görünmesin)
const showBanner = computed(() => !!blog.value?.coverImageUrl || !firstImage(blog.value?.content))
// Banner sadece yüklenen kapağı ya da üretilmiş kapağı kullanır, içerik resmini değil
const bannerPost = computed(() => ({ ...blog.value, content: '' }))

const fetchBlog = async () => {
  try {
    const { data } = await api.getBlog(route.params.id)
    blog.value = data
  } catch (e) {
    error.value = e.message
  } finally {
    loading.value = false
  }
}

const enableEditMode = () => {
  editForm.title = blog.value.title
  editForm.category = blog.value.category
  editForm.content = blog.value.content
  editForm.coverImageUrl = blog.value.coverImageUrl || ''
  editMode.value = true
  window.scrollTo({ top: 0 })
}

const cancelEdit = () => {
  editMode.value = false
  error.value = ''
}

const saveBlog = async () => {
  saving.value = true
  error.value = ''
  Object.keys(fieldErrors).forEach((key) => (fieldErrors[key] = []))

  try {
    const { data } = await api.updateBlog(blog.value.id, {
      title: editForm.title,
      category: editForm.category,
      content: editForm.content,
      coverImageUrl: editForm.coverImageUrl || null,
    })
    blog.value = data
    editMode.value = false
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

const deleteBlog = async () => {
  deleting.value = true
  error.value = ''
  try {
    await api.deleteBlog(blog.value.id)
    router.push('/blog')
  } catch (e) {
    error.value = e.message
    confirmDelete.value = false
  } finally {
    deleting.value = false
  }
}

onMounted(fetchBlog)
</script>

<style scoped>
.narrow {
  max-width: calc(var(--measure) + 4rem);
}

.wide {
  max-width: 76rem;
}

.back {
  display: inline-flex;
  align-items: center;
  gap: 0.35rem;
  font-size: 0.9375rem;
  font-weight: 500;
  color: var(--text-muted);
  margin-bottom: 2rem;
}

.back:hover {
  color: var(--accent);
}

.meta-top {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  font-size: 0.875rem;
}

.title {
  margin-top: 1rem;
}

.byline {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 0.85rem;
  margin-top: 1.75rem;
}

.byline-name {
  font-weight: 700;
  color: var(--text);
  margin: 0;
}

.byline-sub {
  font-size: 0.875rem;
  color: var(--text-muted);
  margin: 0;
}

.owner-tools {
  display: flex;
  gap: 0.5rem;
  margin-left: auto;
}

.danger {
  color: var(--danger) !important;
}

.banner {
  margin-top: 2.5rem;
}

.banner :deep(.cover) {
  border-radius: var(--radius-lg);
}

/* Hafif parallax: görsel çerçevesinden biraz büyük, kaydırdıkça yavaşça kayar.
   --p yoksa (hareket azaltma) 0.5 ile tam ortada, hareketsiz durur. */
.banner :deep(.cover-img),
.banner :deep(.cover-generated) {
  transform: translateY(calc((var(--p, 0.5) - 0.5) * 10%)) scale(1.12);
  will-change: transform;
}

@media (prefers-reduced-motion: reduce) {
  .banner :deep(.cover-img),
  .banner :deep(.cover-generated) {
    transform: none;
  }
}

.body {
  margin-top: 2.75rem;
}

.foot {
  margin-top: 4rem;
}

.author-card {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 1.25rem 1.5rem;
}

.small {
  font-size: 0.8125rem;
  margin: 0;
}

.form-actions {
  display: flex;
  gap: 0.75rem;
}

.dialog {
  padding: 1.75rem;
  border-radius: var(--radius-lg);
}

.dialog-actions {
  display: flex;
  justify-content: flex-end;
  gap: 0.5rem;
  margin-top: 1.75rem;
}

.state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 5rem 1rem;
  text-align: center;
}
</style>
