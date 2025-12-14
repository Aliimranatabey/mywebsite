import './assets/main.css'
// Vuetify 3'ün kendi stilleri
import 'vuetify/styles'
// İkonların görünmesi için gerekli CSS (mdi-domain yazısı yerine ikon çıkması için)
import '@mdi/font/css/materialdesignicons.css'

import { createApp } from 'vue'
import { createPinia } from 'pinia'

// Vuetify Kurulum Parçaları
import { createVuetify } from 'vuetify'
import * as components from 'vuetify/components'
import * as directives from 'vuetify/directives'

import App from './App.vue'
import router from './router'

// Vuetify Ayarlarını Yapılandırıyoruz
const vuetify = createVuetify({
  components,
  directives,
  icons: {
    defaultSet: 'mdi', // İkon seti olarak MDI kullanılsın
  },
  theme: {
    defaultTheme: 'light', // Varsayılan tema (Login ekranında dark kullanıyoruz ama genel tema light kalabilir)
  },
})

const app = createApp(App)

app.use(createPinia())
app.use(router)
app.use(router)
app.use(vuetify)

import { QuillEditor } from '@vueup/vue-quill'
import '@vueup/vue-quill/dist/vue-quill.snow.css';

app.component('QuillEditor', QuillEditor)

app.mount('#app')
