/** Sayfalar arasında paylaşılan küçük metin yardımcıları. */

/** Quill'den gelen HTML'den düz metin özet çıkarır. */
export function excerpt(html, maxLength = 180) {
  if (!html) return ''

  // DOMParser'ın ürettiği belge etkisizdir: içindeki <img onerror> gibi şeyler çalışmaz.
  // (innerHTML'li bir div'de resimler yüklenir ve olay nitelikleri tetiklenirdi.)
  const doc = new DOMParser().parseFromString(html, 'text/html')
  const text = (doc.body.textContent || '').replace(/\s+/g, ' ').trim()

  if (text.length <= maxLength) return text
  // Kelimenin ortasında kesmemek için son boşluktan kırp
  return text.slice(0, text.lastIndexOf(' ', maxLength)) + '…'
}

/** 25 Eylül 2026 biçiminde tarih. */
export function formatDate(value) {
  if (!value) return ''
  return new Date(value).toLocaleDateString('tr-TR', {
    day: 'numeric',
    month: 'long',
    year: 'numeric',
  })
}

/** Ortalama okuma hızına göre kaba bir okuma süresi. */
export function readingTime(html) {
  const words = excerpt(html, Number.MAX_SAFE_INTEGER).split(' ').filter(Boolean).length
  return Math.max(1, Math.round(words / 200))
}
