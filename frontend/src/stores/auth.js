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

  // Kayıt kapalıyken (canlıda varsayılan) "Yazmaya başla" bağlantıları gizlenir
  const registrationOpen = ref(false)

  const isLoggedIn = computed(() => user.value !== null)

  /** Yazı yazmaya giden bağlantı; gidilecek yer yoksa null. */
  const writeLink = computed(() => {
    if (isLoggedIn.value) return '/blog/new'
    return registrationOpen.value ? '/register' : null
  })

  /** Sayfa yenilendiğinde oturumu sunucudan geri kurar. */
  async function restore() {
    if (initialized.value) return
    api
      .registrationStatus()
      .then(({ data }) => (registrationOpen.value = data?.enabled === true))
      .catch(() => {})
    try {
      const { data } = await api.me()
      user.value = data
      // CSRF çerezi sadece giriş yapmış kişiye (sana) yazılır: yazma işlemleri
      // bu token olmadan 403 alır. Ziyaretçinin tarayıcısına hiç çerez yazılmaz.
      await api.primeCsrf().catch(() => {})
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

  return {
    user,
    loading,
    initialized,
    isLoggedIn,
    registrationOpen,
    writeLink,
    restore,
    login,
    register,
    logout,
  }
})
