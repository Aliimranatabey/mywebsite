<template>
  <v-container class="py-12">
    <v-btn icon="mdi-arrow-left" variant="text" to="/blog" class="mb-4"></v-btn>
    
    <div v-if="loading" class="text-center">
      <v-progress-circular indeterminate color="primary"></v-progress-circular>
    </div>

    <div v-else-if="blog">
      <!-- View Mode -->
      <div v-if="!editMode">
        <div class="d-flex justify-space-between align-center mb-2">
          <h1 class="text-h3">{{ blog.title }}</h1>
          <v-btn color="primary" variant="text" prepend-icon="mdi-pencil" @click="enableEditMode">
            Düzenle
          </v-btn>
        </div>
        
        <div class="d-flex align-center mb-4 text-grey">
          <v-icon size="small" class="mr-1">mdi-calendar</v-icon>
          <span class="mr-4">{{ formatDate(blog.createdAt) }}</span>
          <v-chip size="small" color="primary">{{ blog.category }}</v-chip>
        </div>
        
        <v-divider class="mb-6"></v-divider>
        
        <div class="text-body-1 blog-content" v-html="blog.content"></div>
      </div>

      <!-- Edit Mode -->
      <div v-else>
        <h2 class="text-h4 mb-4">Blog Düzenle</h2>
        <v-form @submit.prevent="saveBlog">
          <v-text-field v-model="editForm.title" label="Başlık" variant="outlined" class="mb-4"></v-text-field>
          <v-text-field v-model="editForm.category" label="Kategori" variant="outlined" class="mb-4"></v-text-field>
          
          <div class="mb-4">
            <label class="text-subtitle-1 mb-2 d-block">İçerik</label>
            <QuillEditor theme="snow" v-model:content="editForm.content" contentType="html" toolbar="full" />
          </div>

          <div class="d-flex gap-4 mt-6">
            <v-btn color="primary" type="submit" :loading="saving">Kaydet</v-btn>
            <v-btn variant="outlined" @click="cancelEdit" :disabled="saving">İptal</v-btn>
          </div>
        </v-form>
      </div>
    </div>

    <div v-else class="text-center">
      <p>Blog yazısı bulunamadı.</p>
    </div>
  </v-container>
</template>

<script setup>
import { ref, onMounted, reactive } from 'vue';
import { useRoute } from 'vue-router';
import api from '@/services/api';

const route = useRoute();
const blog = ref(null);
const loading = ref(true);
const editMode = ref(false);
const saving = ref(false);

const editForm = reactive({
  title: '',
  category: '',
  content: ''
});

const fetchBlog = async () => {
  try {
    const response = await api.getBlog(route.params.id);
    blog.value = response.data;
  } catch (error) {
    console.error('Blog yüklenirken hata oluştu:', error);
  } finally {
    loading.value = false;
  }
};

const enableEditMode = () => {
  editForm.title = blog.value.title;
  editForm.category = blog.value.category;
  editForm.content = blog.value.content;
  editMode.value = true;
};

const cancelEdit = () => {
  editMode.value = false;
};

const saveBlog = async () => {
  saving.value = true;
  try {
    const updatedBlog = {
      ...blog.value,
      title: editForm.title,
      category: editForm.category,
      content: editForm.content
    };
    
    const response = await api.updateBlog(blog.value.id, updatedBlog);
    blog.value = response.data;
    editMode.value = false;
  } catch (error) {
    console.error('Blog güncellenirken hata:', error);
    alert('Güncelleme başarısız oldu.');
  } finally {
    saving.value = false;
  }
};

const formatDate = (dateString) => {
  if (!dateString) return '';
  return new Date(dateString).toLocaleDateString('tr-TR');
};

onMounted(() => {
  fetchBlog();
});
</script>

<style scoped>
.blog-content {
  white-space: pre-wrap;
  line-height: 1.8;
}
.blog-content :deep(img) {
  max-width: 100%;
  height: auto;
  border-radius: 8px;
  margin: 1rem 0;
}
</style>
