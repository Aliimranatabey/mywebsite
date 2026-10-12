import { computed } from 'vue'
import { useTheme } from 'vuetify'

const STORAGE_KEY = 'theme'

/**
 * Açık/koyu tema geçişi.
 * Hem Vuetify temasını hem <html data-theme> niteliğini (CSS token'ları) birlikte değiştirir,
 * seçimi tarayıcıda hatırlar. localStorage kapalıysa (gizli sekme vb.) sadece hatırlamaz.
 */
export function useThemeMode() {
  const theme = useTheme()

  const isDark = computed(() => theme.global.name.value === 'dark')

  function set(mode) {
    theme.change(mode)
    document.documentElement.dataset.theme = mode
    try {
      localStorage.setItem(STORAGE_KEY, mode)
    } catch {
      // depolama kapalı: tema yine değişir, sadece bir sonraki ziyarette hatırlanmaz
    }
  }

  function toggle() {
    set(isDark.value ? 'light' : 'dark')
  }

  return { isDark, toggle }
}
