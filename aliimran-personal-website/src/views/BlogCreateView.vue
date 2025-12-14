<template>
  <v-container class="py-12">
    <v-btn icon="mdi-arrow-left" variant="text" to="/blog" class="mb-4"></v-btn>
    
    <h1 class="text-h3 mb-6">Yeni Blog Yazısı</h1>

    <v-card class="pa-6">
      <v-form @submit.prevent="saveBlog">
        <v-text-field v-model="form.title" label="Başlık" variant="outlined" class="mb-4" required></v-text-field>
        <v-text-field v-model="form.category" label="Kategori" variant="outlined" class="mb-4" required></v-text-field>
        
        <div class="mb-4">
          <label class="text-subtitle-1 mb-2 d-block">İçerik</label>
          <QuillEditor theme="snow" v-model:content="form.content" contentType="html" toolbar="full" />
        </div>

        <div class="d-flex gap-4 mt-6">
          <v-btn color="primary" type="submit" :loading="saving" size="large">Oluştur</v-btn>
          <v-btn variant="outlined" to="/blog" :disabled="saving" size="large">İptal</v-btn>
        </div>
      </v-form>
    </v-card>
  </v-container>
</template>

<script setup>
import { ref, reactive } from 'vue';
import { useRouter } from 'vue-router';
import api from '@/services/api';

const router = useRouter();
const saving = ref(false);

const form = reactive({
  title: '',
  category: '',
  content: ''
});

const saveBlog = async () => {
  if (!form.title || !form.category || !form.content) {
    alert('Lütfen tüm alanları doldurun.');
    return;
  }

  saving.value = true;
  try {
    await api.createBlog(form);
    router.push('/blog');
  } catch (error) {
    console.error('Blog oluşturulurken hata:', error);
    alert('Blog oluşturulurken bir hata oluştu.');
  } finally {
    saving.value = false;
  }
};
</script>
