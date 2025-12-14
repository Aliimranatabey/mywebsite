<template>
  <v-container fluid class="fill-height hero-section d-flex align-center justify-center">
    <v-row justify="center" align="center">
      <v-col cols="12" md="8" lg="6" class="text-center">
        <h1 class="text-h2 font-weight-bold mb-6 text-white">Merhaba, Ben Ali İmran</h1>
        <p class="text-h5 mb-8 text-grey-lighten-3">
          Yazılım, Teknoloji ve Hayata Dair Notlar
        </p>
        <div class="d-flex justify-center gap-4">
          <v-btn color="primary" size="x-large" to="/blog" class="mr-4" elevation="4">
            Blog Yazıları
          </v-btn>
          <v-btn variant="outlined" color="white" size="x-large" to="/contact">
            İletişime Geç
          </v-btn>
        </div>
      </v-col>
    </v-row>
  </v-container>
</template>

<style scoped>
.hero-section {
  background: linear-gradient(rgba(0, 0, 0, 0.7), rgba(0, 0, 0, 0.7)),
              url('https://images.unsplash.com/photo-1555066931-4365d14bab8c?ixlib=rb-1.2.1&auto=format&fit=crop&w=1920&q=80');
  background-size: cover;
  background-position: center;
  min-height: 90vh;
}
</style>
