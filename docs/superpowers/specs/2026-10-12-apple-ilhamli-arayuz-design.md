# Apple İlhamlı Arayüz — Tasarım

**Amaç:** Siteyi Apple'ın sadeliğinden ve "göze güzel gelen ihtişamından" ilham alan, ama onu taklit etmeyen bir arayüze taşımak. Kişisel site + blog dengesi korunur: vitrin ana sayfada, okuma sayfaları blog gibi kalır. Mor/neon ve yapay zekâ şablonu izleri çıkar.

**Değişmeyenler:** Vue 3 + Vuetify 3, router, Pinia, API ve backend. Kapak önceliği (yüklenen → yazıdaki ilk resim → üretilmiş kapak) aynen kalır.

## 1. Görsel sistem

| Token | Açık | Koyu |
|---|---|---|
| Zemin | `#F5F5F7` | `#000000` |
| Yüzey | `#FFFFFF` | `#1C1C1E` |
| Metin | `#1D1D1F` | `#F5F5F7` |
| İkincil metin | `#6E6E73` | `#A1A1A6` |
| Vurgu (okyanus) | `#0A5C7A` | `#5FB4D0` |

- **Yazı tipi:** gövde, menü ve butonlarda sistem fontu (`-apple-system` → Apple'da SF Pro). Büyük başlıklarda (isim, yazı başlıkları, bölüm başlıkları) **Fraunces** serif. Inter kaldırılır.
- **Cam (web yaklaşımı, Apple Liquid Glass'ın kendisi değil):** `backdrop-filter` + 1px iç kenar + ince üst ışık. Yalnızca üst menü, menü açılır paneli, açılır pencereler/menüler. `prefers-reduced-transparency` açıksa düz dolgu.
- **Taklit etmeme kuralları:** serif başlıklar, okyanus mavisi vurgu, solda kendi markamız (isim), Apple'a ait ikon/logo/metin kalıbı yok.
- Üretilmiş kapak geçişleri indigo/mor yerine okyanus–gri–gece mavisi ailesine çekilir.

## 2. Üst menü (Apple global nav ilhamı)

- Tam genişlik, 44px yükseklik, buzlu cam, küçük (13px) ortalı linkler. Solda isim, sağda tema düğmesi + giriş/kullanıcı.
- **Masaüstü:** "Yazılar" ve "Hakkımda" üstüne gelince (veya klavye odağıyla) panel aşağı doğru yumuşakça açılır, sayfa arkada bulanıklaşır. Fare çıkınca ~150ms gecikmeyle kapanır; Esc kapatır.
  - Yazılar paneli: son 4 yazı (başlık + tarih) ve "Tüm yazılar".
  - Hakkımda paneli: Deneyim, Eğitim, Teknolojiler (özgeçmiş sayfasındaki bölümlere bağlantı), İletişim.
- **Mobil:** iki çizgili düğme tam ekran menü açar; linkler sırayla belirir.
- Yazı detayında menünün alt kenarında ince okuma ilerleme çizgisi.

## 3. Sayfalar

- **Ana sayfa**
  1. "Sen" girişi: ortalı büyük serif isim, tek cümle kişisel tanıtım, iki bağlantı (Yazılar / Hakkımda).
  2. Vitrin: kapağı **yüklenmiş ya da yazı içinde resmi olan** son 3 yazı, tam genişlik bloklar. Ortalı iri başlık + tek satır özet + "Oku" bağlantısı, altında büyük görsel. Blok zeminleri sırayla değişir (açık gri / beyaz / siyah).
  3. "Son yazılar": vitrinde olmayanlardan Apple Newsroom tarzı liste (küçük görsel, kategori, başlık, tarih).
  4. Hiç yazı yoksa sade boş durum ve "Yazmaya başla".
- **Yazılar:** aynı liste düzeni + sayfalama. Kart ızgarası kalkar.
- **Yazı detayı:** büyük kapak (hafif parallax), serif başlık, 42rem okuma genişliği, sade metin. Düzenle/sil akışı aynen kalır.
- **Hakkımda, İletişim, Giriş/Kayıt, Yazma:** yeni tokenlarla yeniden giydirilir; düzenleri büyük ölçüde korunur.
- **Alt bilgi:** Apple'daki gibi küçük, sessiz, gri.

## 4. Hareket

- Menü paneli açılış/kapanışı ve sayfa bulanıklaşması.
- Vitrin blokları: görsel ekrana girerken hafifçe küçükten büyüğe ve netleşerek oturur (kaydırmaya bağlı). Projedeki tek "gösterişli" an.
- Yazı detayı kapağında hafif parallax.
- `v-reveal` ile her kartın tek tek belirmesi kaldırılır.
- `prefers-reduced-motion` açıksa kaydırmaya bağlı tüm efektler ve geçişler kapanır; içerik hiçbir zaman gizli kalmaz.

## 5. Teknik

- `src/assets/main.css`: token ve ortak sınıflar baştan yazılır.
- `src/main.js`: Vuetify açık/koyu tema renkleri yeni tokenlarla eşlenir.
- `index.html`: Inter yerine Fraunces; ilk boyama zemin renkleri güncellenir.
- Yeni `src/composables/useScrollProgress.js`: bir öğenin ekrandaki ilerlemesini (0–1) CSS değişkenine yazar, `requestAnimationFrame` ile; reduced-motion'da çalışmaz.
- `NavBar.vue` yeniden yazılır (panel + mobil menü). Son yazılar için `api.getBlogs(0, 4)` panel ilk açıldığında bir kez çağrılır.
- `PostCard.vue` → liste satırı olur; vitrin bloğu için yeni `ShowcasePost.vue`.
- `utils/cover.js`: palet güncellenir, `hasRealCover(post)` eklenir.
- Ana sayfa vitrin + liste için `api.getBlogs(0, 12)` (tek istek).

## 6. Kontrol

- `npm run build` ve `npm run lint` hatasız.
- Geliştirme sunucusunda tüm sayfalar açık/koyu temada ve 390px genişlikte açılıp bakılır; menü paneli fare ve klavye (Tab/Esc) ile denenir.
- Reduced-motion açıkken içerik görünür ve sabit.

## Kapsam dışı

- Backend / API değişikliği, kategori filtreleme, arama.
- Yeni içerik (özgeçmiş metni olduğu gibi kalır).
