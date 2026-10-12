<template>
  <v-app>
    <NavBar />
    <v-main>
      <v-container fluid class="pa-0">
        <router-view v-slot="{ Component }">
          <v-fade-transition mode="out-in">
            <component :is="Component" />
          </v-fade-transition>
        </router-view>
      </v-container>
    </v-main>
  </v-app>
</template>

<script setup>
import NavBar from '@/components/NavBar.vue'
</script>

<style>
/* Global styles if needed */
</style>
