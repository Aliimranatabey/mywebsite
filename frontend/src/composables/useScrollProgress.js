import { onMounted, onUnmounted, watch } from 'vue'

/**
 * Bir öğenin ekrandan geçişini 0–1 arası bir sayı olarak `--p` CSS değişkenine yazar:
 * öğenin üstü ekranın altına değdiğinde 0, öğenin altı ekranın üstünden çıktığında 1.
 * Efektin kendisi CSS'te `var(--p)` ile kurulur; burada sadece ölçüm var.
 *
 * Hareket azaltma tercihi açıksa hiç çalışmaz; CSS tarafı `var(--p, <varsayılan>)` ile
 * sabit, hareketsiz hale düşer.
 *
 * @param {import('vue').Ref<HTMLElement|null>} target
 */
export function useScrollProgress(target) {
  let frame = 0

  const measure = () => {
    frame = 0
    const el = target.value
    if (!el) return
    const rect = el.getBoundingClientRect()
    const vh = window.innerHeight
    const p = (vh - rect.top) / (vh + rect.height)
    el.style.setProperty('--p', Math.min(1, Math.max(0, p)).toFixed(4))
  }

  // Kaydırma olayları kare başına bir ölçüme indirgenir
  const schedule = () => {
    if (!frame) frame = requestAnimationFrame(measure)
  }

  let active = false

  onMounted(() => {
    if (window.matchMedia('(prefers-reduced-motion: reduce)').matches) return
    active = true
    window.addEventListener('scroll', schedule, { passive: true })
    window.addEventListener('resize', schedule, { passive: true })
    measure()
  })

  // Öğe sonradan (veri gelince) oluşursa ilk ölçümü kaçırmasın
  watch(target, () => active && schedule(), { flush: 'post' })

  onUnmounted(() => {
    window.removeEventListener('scroll', schedule)
    window.removeEventListener('resize', schedule)
    if (frame) cancelAnimationFrame(frame)
  })
}
