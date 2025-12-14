<template>
  <v-app-bar elevation="1" color="white" class="px-6" height="90">
    <div class="d-flex align-center" style="cursor: pointer" @click="$router.push('/')">
      <img :src="logo" alt="Logo" height="70" class="mr-3" />
      <v-app-bar-title class="font-weight-bold text-h5">
        Ali İmran Atabey
      </v-app-bar-title>
    </div>

    <v-spacer></v-spacer>

    <!-- Desktop Menu -->
    <nav class="d-none d-md-flex align-center" style="gap: 24px">
      <router-link to="/" class="nav-link" active-class="active-link">Ana Sayfa</router-link>
      <router-link to="/resume" class="nav-link" active-class="active-link">Özgeçmiş</router-link>
      <router-link to="/blog" class="nav-link" active-class="active-link">Blog</router-link>
      <router-link to="/contact" class="nav-link" active-class="active-link">İletişim</router-link>
    </nav>

    <!-- Mobile Menu Button -->
    <v-btn icon class="d-md-none" @click="drawer = !drawer">
      <v-icon>mdi-menu</v-icon>
    </v-btn>
  </v-app-bar>

  <!-- Mobile Navigation Drawer -->
  <v-navigation-drawer v-model="drawer" temporary location="right">
    <v-list>
      <v-list-item to="/" title="Ana Sayfa"></v-list-item>
      <v-list-item to="/resume" title="Özgeçmiş"></v-list-item>
      <v-list-item to="/blog" title="Blog"></v-list-item>
      <v-list-item to="/contact" title="İletişim"></v-list-item>
    </v-list>
  </v-navigation-drawer>
</template>

<script setup>
import { ref } from 'vue'
import logo from '@/assets/logo.svg'

const drawer = ref(false)
</script>

<style scoped>
.nav-link {
  text-decoration: none;
  color: #424242;
  font-weight: 500;
  font-size: 0.95rem;
  transition: all 0.2s ease-in-out;
  padding: 8px 16px;
  border-radius: 4px;
}

.nav-link:hover {
  color: #1976d2;
  background-color: rgba(25, 118, 210, 0.05);
}

.active-link {
  color: #1976d2;
  font-weight: 700;
  background-color: rgba(25, 118, 210, 0.1);
}
</style>
