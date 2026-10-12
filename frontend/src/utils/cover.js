/**
 * Kapak görseli seçimi: yüklenen kapak → yazıdaki ilk resim → üretilmiş geçişli kapak.
 */

/** Yazının HTML içeriğindeki ilk resmin adresi; yoksa null. */
export function firstImage(html) {
  if (!html) return null
  const doc = new DOMParser().parseFromString(html, 'text/html')
  return doc.querySelector('img')?.getAttribute('src') || null
}

// Okyanus, taş grisi ve gece mavisi ailesinde, birbirinden ayırt edilebilir geçiş çiftleri
const PALETTES = [
  ['#0a5c7a', '#1f3a4d'],
  ['#2e6f8e', '#0e2a3a'],
  ['#5e7c8a', '#24343d'],
  ['#0f4c5c', '#4a7f91'],
  ['#45657a', '#16232b'],
  ['#1b5e73', '#6fa8ba'],
]

/** Başlıktan her zaman aynı sonucu veren kararlı bir sayı. */
function hash(text) {
  let h = 0
  for (let i = 0; i < text.length; i++) {
    h = (h * 31 + text.charCodeAt(i)) | 0
  }
  return Math.abs(h)
}

/** Başlığa özgü, her seferinde aynı çıkan geçişli zemin. */
export function gradientFor(title = '') {
  const n = hash(title)
  const [from, to] = PALETTES[n % PALETTES.length]
  const angle = 115 + (n % 50)
  return [
    `radial-gradient(circle at ${20 + (n % 60)}% ${15 + (n % 40)}%, rgba(255,255,255,0.22), transparent 45%)`,
    `radial-gradient(circle at 85% 90%, rgba(0,0,0,0.25), transparent 50%)`,
    `linear-gradient(${angle}deg, ${from}, ${to})`,
  ].join(', ')
}

/** Yazının gerçek bir görseli var mı (yüklenen kapak ya da içerikteki resim)? */
export function hasRealCover(post) {
  return !!(post?.coverImageUrl || firstImage(post?.content))
}

/** @returns {{ src: string|null, gradient: string }} */
export function resolveCover(post) {
  return {
    src: post?.coverImageUrl || firstImage(post?.content) || null,
    gradient: gradientFor(post?.title || ''),
  }
}
