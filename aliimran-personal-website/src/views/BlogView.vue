<template>
  <v-container class="py-12">
    <div class="text-center mb-12">
      <h1 class="text-h3 font-weight-bold mb-2">Blog Yazıları</h1>
      <p class="text-subtitle-1 text-grey-darken-1 mb-6">
        Teknoloji, yazılım ve hayat üzerine düşüncelerim
      </p>
      <v-btn color="primary" prepend-icon="mdi-plus" to="/blog/new" size="large">
        Yeni Blog Yazısı
      </v-btn>
    </div>

    <v-row v-if="loading">
      <v-col cols="12" class="text-center">
        <v-progress-circular indeterminate color="primary" size="64"></v-progress-circular>
      </v-col>
    </v-row>
    
    <v-row v-else>
      <v-col cols="12" md="4" v-for="blog in blogs" :key="blog.id">
        <v-hover v-slot="{ isHovering, props }">
          <v-card
            v-bind="props"
            :elevation="isHovering ? 8 : 2"
            class="transition-swing d-flex flex-column h-100"
            rounded="lg"
            :to="`/blog/${blog.id}`"
          >
            <v-img
              :src="extractImage(blog.content)"
              height="250"
              cover
              class="align-end"
            >
              <template v-slot:placeholder>
                <div class="d-flex align-center justify-center fill-height bg-grey-lighten-4">
                  <v-icon color="grey-lighten-1" size="48">mdi-image-off</v-icon>
                </div>
              </template>
              <v-card-title class="text-white bg-black-transparent text-wrap" style="line-height: 1.4">
                {{ blog.title }}
              </v-card-title>
            </v-img>

            <v-card-subtitle class="pt-4 pb-4">
              <div class="d-flex align-center">
                <v-icon size="small" class="mr-1">mdi-calendar</v-icon>
                {{ formatDate(blog.createdAt) }}
                <v-spacer></v-spacer>
                <v-chip size="small" color="primary" label>{{ blog.category }}</v-chip>
              </div>
            </v-card-subtitle>
          </v-card>
        </v-hover>
      </v-col>
      
      <v-col cols="12" v-if="blogs.length === 0" class="text-center">
        <v-icon size="64" color="grey-lighten-1" class="mb-4">mdi-text-box-remove</v-icon>
        <p class="text-h6 text-grey">Henüz blog yazısı bulunmuyor.</p>
      </v-col>
    </v-row>
  </v-container>
</template>

<style scoped>
.bg-black-transparent {
  background: linear-gradient(to top, rgba(0,0,0,0.9), rgba(0,0,0,0.4), transparent);
  padding-bottom: 1rem;
}
.transition-swing {
  transition: 0.3s cubic-bezier(0.25, 0.8, 0.5, 1);
}
</style>

<script setup>
import { ref, onMounted } from 'vue';
import api from '@/services/api';

const blogs = ref([]);
const loading = ref(true);

const fetchBlogs = async () => {
  try {
    const response = await api.getBlogs();
    blogs.value = response.data;
  } catch (error) {
    console.error('Bloglar yüklenirken hata oluştu:', error);
  } finally {
    loading.value = false;
  }
};

const extractImage = (content) => {
  if (!content) return 'https://images.unsplash.com/photo-1499750310159-5b5f226932b7?ixlib=rb-1.2.1&auto=format&fit=crop&w=800&q=80'; // Default image
  
  const div = document.createElement('div');
  div.innerHTML = content;
  const img = div.querySelector('img');
  return img ? img.src : 'https://images.unsplash.com/photo-1499750310159-5b5f226932b7?ixlib=rb-1.2.1&auto=format&fit=crop&w=800&q=80';
};

const formatDate = (dateString) => {
  if (!dateString) return '';
  return new Date(dateString).toLocaleDateString('tr-TR');
};

onMounted(() => {
  fetchBlogs();
});
</script>
