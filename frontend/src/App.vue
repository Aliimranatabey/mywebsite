<template>
  <v-app>
    <NavBar />

    <v-main>
      <router-view v-slot="{ Component }">
        <v-fade-transition mode="out-in">
          <component :is="Component" />
        </v-fade-transition>
      </router-view>
    </v-main>

    <footer class="footer">
      <div class="container">
        <div class="footer-top">
          <router-link to="/" class="footer-brand">Ali İmran Atabey</router-link>

          <nav class="footer-links" aria-label="Alt menü">
            <router-link to="/blog">Yazılar</router-link>
            <router-link to="/resume">Hakkımda</router-link>
            <router-link to="/contact">İletişim</router-link>
            <router-link v-if="auth.registrationOpen" to="/register">Yazmaya başla</router-link>
          </nav>
        </div>

        <p class="footer-bottom">© {{ year }} Ali İmran Atabey</p>
      </div>
    </footer>
  </v-app>
</template>

<script setup>
import NavBar from '@/components/NavBar.vue'
import { useAuthStore } from '@/stores/auth'

const auth = useAuthStore()

const year = new Date().getFullYear()
</script>

<style scoped>
/* Apple'daki gibi küçük, gri, sessiz */
.footer {
  margin-top: clamp(4rem, 10vw, 7rem);
  background: var(--tone-b);
  border-top: 1px solid var(--border);
  font-size: 0.75rem;
  color: var(--text-muted);
}

.footer-top {
  display: flex;
  flex-wrap: wrap;
  justify-content: space-between;
  align-items: flex-start;
  gap: 1.5rem 3rem;
  padding: 2.25rem 0 1.5rem;
}

.footer-brand {
  font-family: var(--font-display);
  font-size: 1rem;
  color: var(--text);
}

.footer-links {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem 1.5rem;
}

.footer-links a {
  color: var(--text-soft);
}

.footer-bottom {
  margin: 0;
  padding: 1rem 0 2rem;
  border-top: 1px solid var(--border);
}
</style>
