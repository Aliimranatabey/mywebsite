// Sıra önemli: kendi tasarım sistemimiz Vuetify'dan SONRA yüklenmeli ki eşit
// öncelikteki kuralarda kazansın. (Önce yüklenince Vuetify'ın [type=submit]
// sıfırlaması form butonlarının beyaz yazısını eziyordu.)
import 'vuetify/styles'
// Başlık fontu kendi sunucumuzdan: Google Fonts ziyaretçinin IP'sini Google'a gönderiyordu (KVKK)
import '@fontsource-variable/fraunces/opsz.css'
import './assets/main.css'

import { createApp } from 'vue'
import { createPinia } from 'pinia'

import { createVuetify } from 'vuetify'
// Bileşenler tek tek, kullanıldıkları yerde yüklenir (vite.config.js → vuetify autoImport).
// Eskiden hepsi birden pakete giriyordu: ~600 KB fazladan JS ve CSS.
import { aliases, mdi } from 'vuetify/iconsets/mdi-svg'

import App from './App.vue'
import router from './router'

/**
 * Vuetify temaları. Renkler main.css'teki token'larla aynı;
 * Vuetify bileşenleri (form alanı, menü, uyarı) de aynı paleti kullansın diye.
 */
const light = {
  dark: false,
  colors: {
    background: '#F5F5F7',
    surface: '#FFFFFF',
    'surface-variant': '#EBEBEF',
    primary: '#0A5C7A',
    secondary: '#1D1D1F',
    error: '#C4314B',
    success: '#2E7D4F',
    warning: '#B26A00',
    info: '#0A5C7A',
  },
}

const dark = {
  dark: true,
  colors: {
    background: '#000000',
    surface: '#1C1C1E',
    'surface-variant': '#2C2C2E',
    primary: '#5FB4D0',
    secondary: '#F5F5F7',
    error: '#FF6B81',
    success: '#5CC98A',
    warning: '#F0B44C',
    info: '#5FB4D0',
  },
}

const vuetify = createVuetify({
  // Vuetify'ın kendi ikonları (uyarı, kapat vb.) SVG olarak; ikon fontu yüklenmiyor
  icons: { defaultSet: 'mdi', aliases, sets: { mdi } },
  theme: {
    // index.html sayfa boyanmadan önce temayı <html data-theme> olarak ayarlıyor
    defaultTheme: document.documentElement.dataset.theme === 'dark' ? 'dark' : 'light',
    themes: { light, dark },
  },
  defaults: {
    VTextField: { variant: 'outlined', density: 'comfortable' },
    VTextarea: { variant: 'outlined', density: 'comfortable' },
    VAlert: { variant: 'tonal', rounded: 'lg' },
  },
})

const app = createApp(App)

// Pinia router'dan önce gelmeli: router guard'ı auth store'u kullanıyor
app.use(createPinia())
app.use(router)
app.use(vuetify)

app.mount('#app')
