<template>
  <div class="form-page">
    <div class="form-card form-card--wide surface">
      <h1 class="h2">Bana yaz</h1>
      <p class="muted mt-2 mb-8">
        Soru, öneri ya da birlikte çalışma fikri, hepsine açığım. Genelde birkaç gün içinde dönüyorum.
      </p>

      <a :href="`mailto:${EMAIL}`" class="btn btn-primary btn-block">E-posta gönder</a>

      <div class="address">
        <span class="address-text">{{ EMAIL }}</span>
        <button type="button" class="btn btn-secondary btn-sm" @click="copy">
          {{ copied ? 'Kopyalandı' : 'Kopyala' }}
        </button>
      </div>

      <!-- Form yok: site kişisel veri toplamaz, mesaj ziyaretçinin kendi
           e-posta programından doğrudan gelir (KVKK yükümlülüğü doğmaz). -->
      <p class="privacy">
        Bu sitede form, çerez ya da takip aracı yok; ziyaretçilerden hiçbir kişisel veri toplanmaz.
      </p>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const EMAIL = 'aliimranatabey@gmail.com'

const copied = ref(false)

async function copy() {
  try {
    await navigator.clipboard.writeText(EMAIL)
    copied.value = true
    setTimeout(() => (copied.value = false), 2000)
  } catch {
    // Pano izni yoksa adres zaten ekranda, elle kopyalanabilir
  }
}
</script>

<style scoped>
.address {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 1rem;
  margin-top: 1rem;
  padding: 0.75rem 0.75rem 0.75rem 1rem;
  border: 1px solid var(--border);
  border-radius: 0.75rem;
}

.address-text {
  font-weight: 500;
  color: var(--text);
  overflow-wrap: anywhere;
}

.btn-sm {
  padding: 0.375rem 0.875rem;
  font-size: 0.8125rem;
  flex-shrink: 0;
}

.privacy {
  margin: 1.5rem 0 0;
  font-size: 0.75rem;
  line-height: 1.55;
  color: var(--text-muted);
}
</style>
