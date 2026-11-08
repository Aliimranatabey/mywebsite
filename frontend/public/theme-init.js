// Tema sayfa boyanmadan önce belirlensin, açık/koyu arasında parlama olmasın.
// Önce kullanıcının seçimi, yoksa işletim sisteminin ayarı.
// Ayrı dosyada duruyor çünkü güvenlik politikası (CSP, nginx.conf) satır içi script'e izin vermiyor.
;(function () {
  var theme = 'light'
  try {
    var saved = localStorage.getItem('theme')
    if (saved === 'light' || saved === 'dark') theme = saved
    else if (window.matchMedia('(prefers-color-scheme: dark)').matches) theme = 'dark'
  } catch {
    if (window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches) {
      theme = 'dark'
    }
  }
  document.documentElement.dataset.theme = theme
})()
