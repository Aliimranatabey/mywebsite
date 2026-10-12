# Modern Arayüz + Kapak Görseli — Tasarım

**Amaç:** Editöryal (eski moda duran) temayı Medium tarzı modern, görsel ağırlıklı bir arayüzle değiştirmek. Yazarlar yazılarına kapak görseli yükleyebilsin.

## 1. Görünüm

| Konu | Karar |
|---|---|
| Zemin / yazı tipi | Beyaz zemin, her yerde **Inter** (Newsreader kaldırılır) |
| Vurgu rengi | İndigo `#4F46E5` |
| Koyu tema | Menüde güneş/ay düğmesi. İlk açılışta sistem ayarı, sonra seçim tarayıcıda hatırlanır |
| Ana sayfa | En yeni yazı büyük kapakla üstte, altında 3 sütunlu kart ızgarası (mobilde tek sütun) |
| Kart | Kapak, kategori, başlık, kısa özet, yazar + okuma süresi |
| Animasyon | Kart hover'da hafif yükselir, resim hafif yakınlaşır; kaydırınca kartlar yumuşakça belirir. `prefers-reduced-motion` açıksa animasyon yok |

Tüm sayfalar (ana sayfa, yazılar, yazı detayı, yazma, giriş/kayıt, iletişim, hakkımda) bu sisteme geçer.

## 2. Kapak görseli

**Öncelik sırası:** yüklenen kapak → yazıdaki ilk resim → başlıktan üretilen indigo tonlu geçişli kapak.

**Backend:**
- `V5` migration: `blog_posts.cover_image_url text` (nullable)
- `POST /api/uploads` (giriş gerekli, multipart) → `{ "url": "/api/uploads/<uuid>.<ext>" }`
- `GET /api/uploads/{dosya}` herkese açık
- Dosyalar Docker volume'de (`/app/uploads`), adı rastgele UUID
- Kontroller: en fazla 5 MB; sadece jpg/png/webp — uzantıya değil dosyanın ilk baytlarına bakılarak
- `coverImageUrl` sadece `/api/uploads/` ile başlıyorsa kabul edilir (dışarıdan keyfi adres girilemez)
- Kapak, yazının diğer alanları gibi create/update ile set edilir → mevcut sahiplik kuralı aynen geçerli

**Frontend:** yazma ve düzenleme ekranında sürükle-bırak alanı, önizleme, "kaldır" düğmesi.

## 3. Test

- Birim: dosya türü/boyut doğrulaması, `coverImageUrl` doğrulaması, başkasının yazısının kapağını değiştirme → 403
- Uçtan uca (curl): yükle → yazıya bağla → listede görün → başkası değiştiremesin
- Arayüz: Docker build + tüm sayfaların açık/koyu temada açılması

## Kapsam dışı

- Resimleri küçültme/yeniden boyutlandırma
- Yazı içindeki resimlerin (Quill, base64) dosyaya taşınması
- Silinen yazının kapak dosyasının otomatik temizlenmesi
