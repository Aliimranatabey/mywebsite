import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import api from '@/services/api'

/**
 * Oturum durumu.
 *
 * Token saklamıyoruz — oturum HttpOnly çerezde duruyor, JavaScript onu göremez.
 * Burada tuttuğumuz tek şey "kim giriş yapmış" bilgisi, o da sayfa yenilenince
 * /auth/me ile sunucudan yeniden sorulur.
 */
export const useAuthStore = defineStore('auth', () => {
  const user = ref(null)
  const loading = ref(false)
  // Uygulama açılışında oturum kontrolü bitti mi (rota koruması bunu bekler)
  const initialized = ref(false)

  const isLoggedIn = computed(() => user.value !== null)

  /** Sayfa yenilendiğinde oturumu sunucudan geri kurar. */
  async function restore() {
    if (initialized.value) return
    try {
      // CSRF çerezini baştan al: yazma işlemleri bu token olmadan 403 alır
      await api.primeCsrf().catch(() => {})
      const { data } = await api.me()
      user.value = data
    } catch {
      // 401 bekleniyor: giriş yapılmamış demektir
      user.value = null
    } finally {
      initialized.value = true
    }
  }

  async function login(email, password) {
    loading.value = true
    try {
      await api.primeCsrf()
      const { data } = await api.login({ email, password })
      user.value = data
      return data
    } finally {
      loading.value = false
    }
  }

  async function register(email, displayName, password) {
    loading.value = true
    try {
      await api.primeCsrf()
      const { data } = await api.register({ email, displayName, password })
      user.value = data
      return data
    } finally {
      loading.value = false
    }
  }

  async function logout() {
    try {
      await api.logout()
    } finally {
      // İstek başarısız olsa da yerel durumu temizle
      user.value = null
    }
  }

  return { user, loading, initialized, isLoggedIn, restore, login, register, logout }
})
