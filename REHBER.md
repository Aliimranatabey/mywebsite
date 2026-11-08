# Ali İmran Atabey — Web Sitesi Kod Rehberi

Bu doküman sitenin nasıl çalıştığını katman katman, dosya dosya anlatır. Amaç: bir
şeyi değiştirmek istediğinde hangi dosyaya bakacağını ve o dosyanın neden öyle
yazıldığını bilmen.

Siteyi internete açmak için ayrı bir doküman var: [CANLIYA_CIKIS.md](CANLIYA_CIKIS.md).

---

## İçindekiler

0. [Hızlı başlangıç](#0-hızlı-başlangıç)
1. [Büyük resim: bir istek nereden nereye gider](#1-büyük-resim-bir-istek-nereden-nereye-gider)
2. [Klasör yapısı](#2-klasör-yapısı)
3. [Docker ve çalışma ortamı](#3-docker-ve-çalışma-ortamı)
4. [Veritabanı](#4-veritabanı)
5. [Backend (Spring Boot)](#5-backend-spring-boot)
6. [Frontend (Vue + Vuetify)](#6-frontend-vue--vuetify)
7. [Lokal geliştirme](#7-lokal-geliştirme)
8. [Sık yapılan işler — tarifler](#8-sık-yapılan-işler--tarifler)
9. [Veritabanına bakmak](#9-veritabanına-bakmak)
10. [Bilinen pürüzler](#10-bilinen-pürüzler)
11. [Sorun giderme](#11-sorun-giderme)

---

## 0. Hızlı başlangıç

Gereken tek şey Docker Desktop'ın çalışıyor olması.

```bash
cd ~/vscode-workspace/mywebsite
docker compose up -d --build     # her şeyi derle ve arka planda başlat
docker compose ps                # hepsi "Up" mı?
docker compose logs -f backend   # backend loglarını izle (Ctrl+C ile çık)
docker compose down              # durdur (veriler silinmez)
```

| Ne | Adres |
|---|---|
| Site | http://localhost:8090 |
| Backend API (doğrudan) | http://localhost:8091/api |
| pgAdmin | http://localhost:5051 (admin@admin.com / admin). Sadece `COMPOSE_PROFILES=dev docker compose up -d` ile açılır |
| PostgreSQL (dışarıdan) | localhost:5433 (devuser / devpass, veritabanı: personal_site) |

> **Şu anki durum (Ekim 2026):** veritabanında 11 yazı var ama **hiç kullanıcı yok** ve
> yazıların hiçbirinin sahibi yok. Bu yüzden arayüzden hiçbir yazıyı düzenleyip
> silemezsin. Çözümü [8.1](#81-kendi-hesabını-açıp-eski-yazıları-sahiplenmek)'de.

---

## 1. Büyük resim: bir istek nereden nereye gider

```
 Tarayıcı
    │  http://localhost:8090/...
    ▼
┌──────────────────────────── frontend container ───────────────────────────┐
│ nginx                                                                      │
│  ├─ /            → derlenmiş Vue uygulaması (index.html + JS + CSS)        │
│  └─ /api/...     → proxy ──────────────┐                                   │
└────────────────────────────────────────┼───────────────────────────────────┘
                                         ▼
┌──────────────────────────── backend container ────────────────────────────┐
│ Spring Boot (Java 21), port 8080                                           │
│  RateLimitFilter → Spring Security (oturum, CSRF, yetki) → Controller      │
│        → Service (iş kuralları) → Repository (JPA) ──────┐                 │
│  ImageStorageService → /app/uploads (volume)             │                 │
└──────────────────────────────────────────────────────────┼─────────────────┘
                                                           ▼
┌──────────────────────────── postgres container ───────────────────────────┐
│ PostgreSQL 16, veritabanı "personal_site" → volume (kalıcı)                │
└────────────────────────────────────────────────────────────────────────────┘
```

Önemli nokta: tarayıcı açısından site ve API **aynı adresten** gelir
(`localhost:8090` ve `localhost:8090/api`). Bu yüzden CORS devreye girmez ve oturum
çerezi sorunsuz gidip gelir. Bu tasarım, canlıya çıkarken de işini kolaylaştırır.

### Örnek: ana sayfa açılırken ne olur?

1. Tarayıcı `/` ister → nginx `index.html`'i verir.
2. `index.html` içindeki küçük script, sayfa boyanmadan önce açık/koyu temayı seçer.
3. Vue başlar, router `/` için `HomeView`'ı yükler.
4. Router guard'ı `auth.restore()` çağırır:
   - önce `GET /api/auth/csrf`, CSRF çerezini almak için,
   - sonra `GET /api/auth/me`, giriş yapılmış mı diye. Giriş yoksa 401 döner; bu beklenen durum.
5. `HomeView`, `GET /api/blogs?page=0&size=12` ile son yazıları çeker.
6. Gelen yazılar vitrine (2 büyük blok) ve kutulara (2×2) dağıtılır.

---

## 2. Klasör yapısı

```
mywebsite/
├── docker-compose.yml        4 servisi (postgres, pgadmin, backend, frontend) birlikte kaldırır
├── .env                      gizli bilgiler (mail şifresi) — git'e girmez
├── .env.example              .env'in şablonu
├── REHBER.md                 bu doküman
├── CANLIYA_CIKIS.md          yayına çıkma rehberi
├── docs/
│   ├── brand/                logo SVG'leri (monogram, wordmark, lockup; açık/koyu/tek renk)
│   └── superpowers/specs/    tasarım kararlarının yazılı hali
├── .claude/skills/           Claude Code skill'leri (tasarım ve beyin fırtınası)
├── backend/
│   ├── Dockerfile            2 aşamalı: Maven ile derle → sadece JRE ile çalıştır
│   ├── pom.xml               bağımlılıklar
│   └── src/
│       ├── main/java/com/aliimran/personal_website_api/
│       │   ├── PersonalwebsiteApplication.java   giriş noktası
│       │   ├── config/        güvenlik, CORS
│       │   ├── controller/    HTTP uçları
│       │   ├── service/       iş kuralları
│       │   ├── repository/    veritabanı erişimi
│       │   ├── entity/        tablo ↔ Java sınıfı eşlemeleri
│       │   ├── dto/           dışarıya açılan veri biçimleri
│       │   ├── security/      oturumdaki kullanıcı, hız sınırı
│       │   ├── exception/     hata türleri ve tek tip hata cevabı
│       │   └── util/          HTML temizleme, slug üretme
│       ├── main/resources/
│       │   ├── application.properties
│       │   └── db/migration/  Flyway SQL dosyaları (V1…V5)
│       └── test/              birim testleri (H2 bellek içi veritabanıyla)
└── frontend/
    ├── Dockerfile            2 aşamalı: Node ile derle → nginx ile sun
    ├── nginx.conf            statik dosyalar + /api proxy
    ├── index.html            fontlar, favicon, tema scripti
    ├── vite.config.js        geliştirme sunucusu ve /api proxy'si
    ├── public/               favicon.svg, favicon.ico, apple-touch-icon.png
    └── src/
        ├── main.js           uygulamayı kurar (Vuetify teması, Pinia, router)
        ├── App.vue           iskelet: menü + sayfa + alt bilgi
        ├── assets/           main.css (tasarım sistemi), portrait.jpg
        ├── router/           sayfa adresleri ve giriş kontrolü
        ├── services/api.js   backend'le konuşan tek yer
        ├── stores/auth.js    oturum durumu (Pinia)
        ├── composables/      useThemeMode, useScrollProgress
        ├── utils/            cover.js (kapak seçimi), text.js (özet, tarih)
        ├── components/       NavBar, ShowcasePost, TilePost, PostCard, PostCover, …
        └── views/            sayfalar (Home, Blog, BlogPost, BlogCreate, Resume, Contact, Login, Register)
```

---

## 3. Docker ve çalışma ortamı

### 3.1 docker-compose.yml

| Servis | İmaj | Dışarıya açık port | Ne yapar |
|---|---|---|---|
| `postgres` | postgres:16 | 5433 → 5432 | Veritabanı. Veriler `personal_website_data` volume'ünde. |
| `pgadmin` | dpage/pgadmin4:8 | 5051 → 80 | Veritabanına görsel arayüz. Sadece `dev` profilinde açılır. |
| `caddy` | caddy:2 | 80, 443 | HTTPS kapısı. Sadece sunucuda (`prod` profili, Caddyfile). |
| `backend` | `./backend`'den derlenir | 8091 → 8080 | Spring Boot API. Görseller `personal_website_uploads` volume'ünde. |
| `frontend` | `./frontend`'den derlenir | 8090 → 80 | nginx: Vue uygulamasını sunar, `/api`'yi backend'e iletir. |

Container'lar birbirine **servis adıyla** ulaşır: backend veritabanına
`jdbc:postgresql://postgres:5432/...`, nginx backend'e `http://backend:8080` diye gider.
`localhost` container'ın kendisi demektir, bu yüzden container içinde kullanılmaz.

Backend'in ayarları `environment:` altında verilir. Spring Boot büyük harfli ortam
değişkenlerini kendi ayarlarına otomatik eşler:
`SPRING_DATASOURCE_URL` → `spring.datasource.url`,
`UPLOADS_DIR` → `${UPLOADS_DIR}` yer tutucusu. Böylece `application.properties`
lokal varsayılanları tutar, Docker bunları ezer.

`MAIL_USERNAME`, `MAIL_PASSWORD` ve `CONTACT_RECIPIENT` kök dizindeki `.env`
dosyasından gelir. Docker Compose bu dosyayı otomatik okur.

### 3.2 Volume'ler: veriler neden kaybolmuyor

Container silinip yeniden kurulsa da volume'ler kalır. Docker bunlara proje klasörünün
adını önek olarak ekler:

| compose'daki ad | Docker'daki gerçek ad | İçinde ne var |
|---|---|---|
| `personal_website_data` | `mywebsite_personal_website_data` | Bütün veritabanı |
| `personal_website_uploads` | `mywebsite_personal_website_uploads` | Yüklenen kapak görselleri |
| `personal_website_pgadmin_data` | `mywebsite_personal_website_pgadmin_data` | pgAdmin ayarları |

`docker compose down` güvenlidir. `docker compose down -v` **volume'leri de siler**,
yani bütün yazılar gider.

### 3.3 Dockerfile'lar

İkisi de **çok aşamalı** (multi-stage) build kullanır. Derleme araçları (Maven, Node)
sadece ilk aşamada var; son imajda yalnızca çalışması gereken şey kalır.

- **backend/Dockerfile**
  1. `eclipse-temurin:21-jdk` imajında önce sadece `pom.xml` kopyalanır ve
     `dependency:go-offline` çalışır. Bağımlılıklar ayrı bir katmanda önbelleğe
     alınır; kod değişince tekrar inmezler.
  2. Sonra kod kopyalanır ve `mvnw clean package -DskipTests` ile jar üretilir.
  3. Çalıştırma aşaması `21-jre` (daha küçük) imajına sadece `app.jar`'ı alır.
- **frontend/Dockerfile**
  1. `node:22-alpine` içinde `npm install` ve `npm run build` çalışır, `dist/` üretilir.
  2. `nginx:stable-alpine`'a `dist/` ve `nginx.conf` kopyalanır.

### 3.4 nginx.conf

```nginx
client_max_body_size 50m;            # varsayılan 1 MB; görsel yükleme ve resimli yazılar için
location / {
    try_files $uri $uri/ /index.html;  # /blog/12 gibi adresler de index.html'e düşsün (SPA)
}
location /api {
    proxy_pass http://backend:8080;
    proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;  # gerçek IP hız sınırı için
}
```

`try_files` satırı önemli: Vue router adresleri tarayıcıda üretir. Biri doğrudan
`/blog/12`'yi açarsa sunucuda böyle bir dosya yoktur; nginx `index.html`'i verir,
gerisini Vue halleder.

---

## 4. Veritabanı

### 4.1 Tablolar

| Tablo | Kolonlar | Not |
|---|---|---|
| `blog_posts` | id, slug (benzersiz), title, content (HTML), category, cover_image_url, author_id → users, created_at, updated_at | Yazılar |
| `users` | id, email (benzersiz), display_name, password_hash (BCrypt), created_at | Hesaplar |
| `contact_messages` | id, name, email, message, created_at | İletişim formundan gelenler |
| `tags`, `post_tags` | — | V1'de kurulmuş ama kodda **kullanılmıyor** |
| `flyway_schema_history` | — | Flyway'in hangi migration'ı çalıştırdığının kaydı |

### 4.2 Flyway migration'ları

Şema elle değil, `backend/src/main/resources/db/migration/` içindeki SQL dosyalarıyla
kurulur. Backend her açılışta Flyway'i çalıştırır; Flyway henüz uygulanmamış dosyaları
sırayla uygular ve `flyway_schema_history`'ye yazar.

| Dosya | Ne yapar |
|---|---|
| `V1__init.sql` | blog_posts, tags, post_tags, contact_messages tablolarını kurar |
| `V2__seed.sql` | İki örnek yazı ekler ("Vue 3 ile Başlangıç", "Spring Boot Giriş") |
| `V3__add_category_to_blog_posts.sql` | `category` kolonu |
| `V4__create_users_and_post_ownership.sql` | `users` tablosu ve `blog_posts.author_id` |
| `V5__add_cover_image_to_blog_posts.sql` | `cover_image_url` kolonu |

İki kural:
- **Var olan migration dosyasını asla değiştirme.** Flyway dosyaların checksum'ını
  saklar; değişikliği görünce backend açılmaz. Her değişiklik yeni bir `V6__...sql` olur.
- `application.properties`'te `spring.jpa.hibernate.ddl-auto=validate` var:
  Hibernate şemayı değiştirmez, sadece entity'lerle uyumlu mu diye kontrol eder.
  Entity'ye alan ekleyip migration yazmayı unutursan backend açılışta hata verir.
  Bu bilinçli bir emniyet kemeri.

---

## 5. Backend (Spring Boot)

Java 21, Spring Boot 3, Spring Security 6, Spring Data JPA, Flyway, jsoup.

### 5.1 Katmanlar

```
Controller   HTTP'yi konuşur: adres, parametre, durum kodu. İş kuralı içermez.
   ↓
Service      İş kuralları: sahiplik, HTML temizleme, slug, dosya doğrulama.
   ↓
Repository   Veritabanı sorguları (Spring Data JPA arayüzleri; SQL yazmıyoruz).
   ↓
Entity       Tablo satırının Java hali.
```

Entity'ler **asla doğrudan JSON olarak dönmez**; araya DTO (`dto/` klasörü) girer.
Sebebi: `BlogPost` → `author` → `User` → `passwordHash` zinciri. Entity dönseydik
şifre hash'leri API'den sızardı.

### 5.2 API uçları

| Metot | Adres | Giriş gerekir mi | Ne yapar |
|---|---|---|---|
| GET | `/api/health` | Hayır | `"ok"` döner (sağlık kontrolü) |
| GET | `/api/blogs?page=0&size=12` | Hayır | Sayfalı yazı listesi, yeniden eskiye. `size` en fazla 50. |
| GET | `/api/blogs/{id}` | Hayır | Tek yazı |
| POST | `/api/blogs` | Evet | Yazı oluşturur; yazar oturumdaki kullanıcıdır |
| PUT | `/api/blogs/{id}` | Evet + sahibi olmalı | Günceller |
| DELETE | `/api/blogs/{id}` | Evet + sahibi olmalı | Siler |
| POST | `/api/uploads` | Evet | Görsel yükler (multipart, alan adı `file`) → `{"url": "/api/uploads/<uuid>.jpg"}` |
| GET | `/api/uploads/{ad}` | Hayır | Görseli verir; 1 yıl önbelleklenir |
| POST | `/api/contact` | Hayır | İletişim mesajı: kaydeder, mail atmayı dener |
| GET | `/api/auth/csrf` | Hayır | CSRF çerezini yazdırır |
| POST | `/api/auth/register` | Hayır | Kayıt + otomatik giriş |
| POST | `/api/auth/login` | Hayır | Giriş |
| POST | `/api/auth/logout` | Evet | Oturumu sunucuda bitirir |
| GET | `/api/auth/me` | Evet | Giriş yapan kullanıcı (sayfa yenilenince oturumu geri kurmak için) |

Yazı cevabı (`BlogPostResponse`) şu alanları taşır: `id, title, content, category,
slug, coverImageUrl, createdAt, updatedAt, authorId, authorDisplayName, canEdit`.
`canEdit`, isteği yapan kişi yazının sahibi mi demek. Frontend "Düzenle/Sil"
butonlarını buna bakarak gösterir; yetki mantığı iki yerde tekrar yazılmaz.

### 5.3 Kimlik doğrulama: oturum çerezi ile

Site JWT değil, **sunucu tarafı oturum** kullanır.

1. `POST /api/auth/login` → `AuthController.authenticate()`:
   - `AuthenticationManager` e-postayı `AppUserDetailsService` ile bulur ve şifreyi
     `BCryptPasswordEncoder` ile karşılaştırır.
   - Başarılıysa `SecurityContext` oluşturulur ve `HttpSessionSecurityContextRepository`
     ile HTTP session'a yazılır. Form login kullanmadığımız için bunu elle yapıyoruz.
2. Tomcat tarayıcıya `JSESSIONID` çerezi verir. Çerez **HttpOnly**'dir, JavaScript
   göremez. Bu yüzden XSS olsa bile oturum çalınamaz.
3. Sonraki isteklerde çerez otomatik gider; Spring Security kullanıcıyı session'dan
   bulur. Kod içinde oturumdaki kullanıcıya `CurrentUser.get()` / `CurrentUser.id()`
   ile ulaşılır.
4. Hatalı giriş her zaman aynı mesajı verir ("E-posta veya şifre hatalı.").
   Hangisinin yanlış olduğunu söylememek, başkasının hangi e-postaların kayıtlı
   olduğunu öğrenmesini engeller.

`AppUserDetails`, kullanıcı id'sini de taşır; sahiplik kontrolü için veritabanına
tekrar gitmek gerekmez.

### 5.4 CSRF koruması

Oturum çerezi her istekte otomatik gittiği için, kötü niyetli başka bir site
tarayıcına senin adına `POST /api/blogs` attırabilir. CSRF token'ı bunu engeller:

1. Backend `XSRF-TOKEN` adlı, HttpOnly **olmayan** bir çerez yazar
   (`CookieCsrfTokenRepository.withHttpOnlyFalse()`).
2. Axios bu çerezi okuyup her istekte `X-XSRF-TOKEN` header'ı olarak geri gönderir
   (`services/api.js` → `xsrfCookieName` / `xsrfHeaderName`).
3. Başka bir site bu çerezi okuyamaz, dolayısıyla header'ı koyamaz → isteği 403 alır.

Spring token'ı "tembel" üretir: kimse okumazsa çerez hiç yazılmaz. Bu yüzden frontend
açılışta ve giriş/kayıt öncesinde `GET /api/auth/csrf` çağırır (`api.primeCsrf()`).

### 5.5 Yetki kuralları

İki katmanda tanımlı:

- **`config/SecurityConfig.java`**, adres bazında:
  okuma (`GET /api/blogs/**`, `/api/uploads/**`), kayıt/giriş ve iletişim herkese açık.
  Yazma uçları ve `/api/auth/**` giriş ister. Giriş yoksa yönlendirme değil düz JSON
  401 döner, çünkü SPA'yız. Form login, basic auth ve varsayılan logout kapalı.
- **`service/BlogService.requireOwnership()`**, nesne bazında: yazıyı sadece sahibi
  güncelleyip silebilir, değilse `ForbiddenException` → 403. Bu kural controller'da
  değil serviste durur; servisi başka yerden çağıran kod da atlayamaz. Sahibi
  olmayan yazılar (`author_id` boş) hiç kimse tarafından düzenlenemez.

Ayrıca `BlogPostRequest` bilinçli olarak `authorId` alanı içermez. Yazar her zaman
oturumdan alınır; aksi halde biri başkası adına yazı yayınlayabilirdi.

### 5.6 Hız sınırı: `security/RateLimitFilter.java`

IP başına, 15 dakikalık sabit pencere:

| Uç | Limit |
|---|---|
| POST /api/auth/register | 5 |
| POST /api/auth/login | 10 |
| POST /api/contact | 5 |
| POST /api/uploads | 30 |

Aşılınca 429 döner. Sayaçlar bellekte (`ConcurrentHashMap`) tutulur ve saatte bir
temizlenir (`@Scheduled`; bunun için `PersonalwebsiteApplication` üzerinde
`@EnableScheduling` var). Tek sunucu için yeterli. IP, nginx'in eklediği
`X-Forwarded-For` header'ının ilk değerinden okunur.

### 5.7 Yazılar: `BlogService`

- **Oluşturma:** başlık kırpılır, içerik `HtmlSanitizer.clean()`'den geçer, boş kapak
  `null` yapılır, başlıktan slug üretilir, yazar oturumdaki kullanıcıdır.
- **Güncelleme:** önce sahiplik kontrol edilir. Başlık değiştiyse slug yenilenir.
- **Zaman damgaları:** entity'deki `@PrePersist` / `@PreUpdate` metotları
  `createdAt` / `updatedAt`'i kendisi yazar.
- **Listeleme:** `BlogPostRepository.findAll` üzerinde `@EntityGraph("author")` var.
  Yazarlar aynı sorguda çekilir; 12 yazı için 13 sorgu atılmaz (N+1 problemi).

**HTML temizleme (`util/HtmlSanitizer.java`).** Yazılar Quill editöründen HTML olarak
gelir ve frontend'de `v-html` ile basılır. Temizlenmeseydi bir kullanıcının yazıya
koyduğu `<script>`, yazıyı okuyan herkesin tarayıcısında çalışırdı (XSS). jsoup'un
`Safelist.relaxed()` listesi temel alınmış; `script`, `iframe`, `onclick` gibi her şey
atılır. Resimlerde `data:` adreslerine izin var, çünkü Quill yapıştırılan resimleri
base64 olarak gömer.

**Slug (`util/SlugGenerator.java`).** "Algoritma Analizi: Temel Kavramlar" →
`algoritma-analizi-temel-kavramlar`. Türkçe harfler Latin karşılığına çevrilir
(ı→i, ş→s…). Aynı slug varsa sonuna `-2`, `-3` eklenir. Slug şu an sadece
veritabanında tutuluyor; adreslerde id kullanılıyor (`/blog/12`).

### 5.8 Görsel yükleme: `ImageStorageService`

- En fazla 5 MB.
- Tür **uzantıdan değil dosyanın ilk baytlarından** (imza) anlaşılır: JPEG `FF D8 FF`,
  PNG `89 50 4E 47…`, WEBP `RIFF….WEBP`. Adı `.jpg` yapılmış bir HTML dosyası geçemez.
- Dosya adı rastgele UUID'dir; kullanıcının verdiği ad hiç kullanılmaz.
- Önce geçici dosyaya yazılır, sonra tek hamlede (`ATOMIC_MOVE`) yerine taşınır;
  yarım yazılmış dosya asla sunulmaz.
- Okurken ad `^[a-f0-9-]{36}\.(jpg|png|webp)$` kalıbına uymalı; `../` gibi yol oyunları
  burada elenir.
- `BlogPostRequest.coverImageUrl` da aynı kalıpla doğrulanır: kapak olarak sadece bu
  sitenin yükleme adresleri kabul edilir, dışarıdan keyfi bir adres (izleme pikseli vb.)
  girilemez.

### 5.9 İletişim formu: `ContactService`

1. Mesaj **her durumda önce veritabanına** yazılır.
2. Sonra Gmail SMTP ile mail atılmaya çalışılır:
   - gönderen `MAIL_USERNAME`,
   - alıcı `CONTACT_RECIPIENT`,
   - "Yanıtla" denince cevap ziyaretçiye gitsin diye `Reply-To` ziyaretçinin adresi.
3. Mail gitmezse (ayar yok, şifre yanlış) hata ziyaretçiye yansımaz. Cevaptaki
   `mailSent: false` alanı sayesinde frontend "Mesajınız kaydedildi" der.

> Şu an `.env` içinde gerçek Gmail bilgileri yok, şablon değerler duruyor. Yani form
> mesajları sadece veritabanına düşüyor, mail gelmiyor. Gmail "Uygulama Şifresi"
> nasıl alınır, `.env.example` içinde yazıyor.

### 5.10 Hata cevapları: `GlobalExceptionHandler`

Bütün hatalar aynı biçimde döner: `{"message": "..."}`. Form doğrulama hatalarında
ek olarak alan bazında `{"errors": {"title": "Başlık zorunludur"}}` gelir.

| Durum | Kod |
|---|---|
| `NotFoundException`, olmayan adres | 404 |
| `ForbiddenException` | 403 |
| `BadRequestException`, doğrulama hatası, bozuk JSON, yanlış parametre | 400 |
| Hatalı giriş | 401 |
| Desteklenmeyen metot | 405 |
| Beklenmeyen hata | 500 (detay loga, kullanıcıya genel mesaj) |

### 5.11 Ayarlar: `application.properties`

| Ayar | Değer | Not |
|---|---|---|
| `spring.datasource.*` | localhost:5432, devuser/devpass | Lokal varsayılan; Docker bunu ezer |
| `spring.jpa.hibernate.ddl-auto` | validate | Şemayı Flyway yönetir |
| `spring.jpa.show-sql` | true | Her SQL loga basılır; canlıda kapatılmalı |
| `spring.jpa.open-in-view` | false | Lazy yükleme controller'a sızmasın diye |
| `spring.mail.*` | smtp.gmail.com:587 | Kullanıcı/şifre ortam değişkeninden |
| `spring.servlet.multipart.max-file-size` | 50MB | Asıl 5 MB sınırı serviste |
| `app.contact.recipient` | `${CONTACT_RECIPIENT}` | |
| `app.uploads.dir` | `${UPLOADS_DIR:./uploads}` | Docker'da `/app/uploads` |
| `app.cors.allowed-origins` | varsayılan `http://localhost:5173` | `CorsConfig`'te okunur |

### 5.12 Testler

```bash
cd backend
./mvnw test
```

Testler PostgreSQL'e değil bellek içi **H2**'ye bağlanır (`src/test/resources/application.properties`).
Flyway testlerde kapalı, şemayı Hibernate kurar.

| Test | Neyi kontrol eder |
|---|---|
| `BlogServiceOwnershipTest` | Başkasının yazısını güncelleme/silme → 403 |
| `BlogPostRequestValidationTest` | Başlık/kategori sınırları, kapak adresi kalıbı |
| `ImageStorageServiceTest` | Dosya imzası, boyut, yol oyunları |
| `HtmlSanitizerTest` | script/onclick temizleniyor mu |
| `SlugGeneratorTest` | Türkçe harfler, çakışmada -2, -3 |
| `PersonalwebsiteApplicationTests` | Uygulama ayağa kalkıyor mu |

---

## 6. Frontend (Vue + Vuetify)

Vue 3 (Composition API, `<script setup>`), Vite 7, Vuetify 3, Pinia, Vue Router, axios,
Quill editörü (`@vueup/vue-quill`).

### 6.1 Açılış: `index.html` ve `main.js`

**`index.html`**
- Google Fonts'tan **Fraunces** (başlık fontu) yüklenir. Gövde metni sistem fontudur
  (Apple cihazlarda SF Pro), yüklenmez.
- Favicon'lar: `favicon.svg` (sistem temasına göre renk değiştirir), `favicon.ico`,
  `apple-touch-icon.png`.
- **Tema scripti:** Vue yüklenmeden önce çalışır. Önce `localStorage`'daki seçime,
  yoksa işletim sisteminin ayarına bakıp `<html data-theme="dark|light">` yazar.
  Böylece koyu temada sayfa açılırken beyaz bir parlama olmaz.

**`main.js`**
- CSS sırası bilinçli: önce `vuetify/styles`, sonra bizim `main.css`. Eşit öncelikteki
  kurallarda bizimki kazansın diye.
- Vuetify'a iki tema verilir (`light`, `dark`); renkleri `main.css`'teki token'larla
  aynıdır. Form alanları, uyarılar ve menüler de aynı paleti kullanır.
- Pinia, router'dan önce kurulur; router guard'ı auth store'u kullanıyor.
- Quill editörü global bileşen olarak kaydedilir.

### 6.2 Sayfalar ve yönlendirme: `router/index.js`

| Adres | Sayfa | Kural |
|---|---|---|
| `/` | `HomeView` | |
| `/blog` | `BlogView` | |
| `/blog/new` | `BlogCreateView` | `requiresAuth`: giriş yoksa `/login?redirect=...` |
| `/blog/:id` | `BlogPostView` | |
| `/resume` | `ResumeView` | |
| `/contact` | `ContactView` | |
| `/login`, `/register` | `LoginView`, `RegisterView` | `guestOnly`: girişliysen `/blog`'a gönderir |
| diğer her şey | → `/` | |

- Sayfalar `() => import(...)` ile **tembel** yüklenir; her sayfa ayrı bir JS dosyası olur.
- `beforeEach`, her geçişte önce `auth.restore()` bekler. Böylece sayfa yenilendiğinde
  giriş durumu sunucudan öğrenilmeden korumalı sayfaya karar verilmez.
- `afterEach`, sekme başlığını "Sayfa • Ali İmran" yapar.
- `scrollBehavior`:
  - yeni sayfada en üste kaydırır,
  - geri gelindiğinde eski konuma döner,
  - `#deneyim` gibi bir anchor varsa o bölüme yumuşakça kayar. Sayfa geçiş
    animasyonu bitsin diye 320 ms bekler.

> Router guard sadece arayüz kolaylığıdır. Gerçek koruma backend'de; guard atlansa
> bile API 401/403 döner.

### 6.3 Backend'le konuşma: `services/api.js`

Uygulamada backend'e giden **her** istek buradan geçer.

- `baseURL: '/api'`: aynı origin, CORS yok.
- `withCredentials: true`: oturum çerezi gitsin.
- `xsrfCookieName` / `xsrfHeaderName`: CSRF token'ı otomatik header'a konur.
- Hata interceptor'ı: backend'in `{"message"}` cevabını `Error.message`'a, alan
  hatalarını `error.fieldErrors`'a koyar. Sayfalarda sadece `e.message` gösterilir.
- Görsel yüklemede `Content-Type` bilinçli olarak `undefined` bırakılır; tarayıcı
  multipart sınırını (boundary) kendisi koysun diye.

### 6.4 Oturum: `stores/auth.js` (Pinia)

- Token saklanmaz; oturum HttpOnly çerezde. Store sadece "kim giriş yapmış"ı tutar.
- `restore()`: uygulama açılışında bir kez çalışır (`initialized` bayrağı). CSRF'i
  hazırlar, `/auth/me`'yi sorar; 401 ise giriş yok demektir.
- `login()` / `register()`: önce CSRF'i hazırlar, sonra isteği atar.
- `logout()`: istek başarısız olsa da yerel durumu temizler.

### 6.5 Tasarım sistemi: `assets/main.css`

Bütün renkler, fontlar ve ölçüler en üstte **CSS değişkeni (token)** olarak tanımlı.
Bileşenler sadece token kullanır, renk kodu yazmaz. Rengi tek yerden değiştirirsin.

| Token | Açık | Koyu | Ne için |
|---|---|---|---|
| `--bg` | #F5F5F7 | #000000 | Sayfa zemini |
| `--bg-elevated` | #FFFFFF | #1C1C1E | Kart, form yüzeyi |
| `--tone-a` / `--tone-b` | #FFF / #F5F5F7 | #000 / #161617 | Ana sayfa blok zeminleri |
| `--text` | #1D1D1F | #F5F5F7 | Ana metin |
| `--text-muted` | #6E6E73 | #A1A1A6 | İkincil metin |
| `--accent` | #0A5C7A | #5FB4D0 | Okyanus mavisi: bağlantı, vurgu |
| `--accent-solid` | #0A5C7A | #1F7A99 | Buton zemini (beyaz yazıyla yeterli kontrast) |
| `--font-display` | Fraunces | | Başlıklar |
| `--font` | sistem fontu (SF Pro) | | Gövde |
| `--nav-h` | 44px | | Menü yüksekliği |

Mekanizmalar:
- **Koyu tema:** `:root[data-theme='dark']` bloğu token'ları ezer. Tema düğmesi
  (`composables/useThemeMode.js`) hem Vuetify temasını hem `data-theme`'i değiştirir
  ve seçimi `localStorage`'a yazar.
- **`.tone-inverse`:** bir bloğun içinde token'ları ters çevirir. Açık temada siyah
  blok, koyu temada açık blok olur. Ana sayfadaki siyah vitrin bloğu böyle çalışır.
- **`.glass`:** buzlu cam (`backdrop-filter`). Apple Liquid Glass'ın web'deki bir
  yaklaşımı, kendisi değil. Tarayıcı desteklemiyorsa ya da kullanıcı "şeffaflığı
  azalt" demişse düz dolguya döner.
- **Erişilebilirlik:** `prefers-reduced-motion` açıksa bütün animasyonlar kapanır.
  Klavye odağı `:focus-visible` ile görünür.
- Ortak sınıflar: `.btn`, `.btn-primary`, `.btn-secondary`, `.h1`, `.h2`, `.lead`,
  `.surface`, `.prose` (yazı gövdesi), `.editor-box` (Quill), `.form-page`, `.form-card`.

### 6.6 Bileşenler (`components/`)

| Bileşen | Ne yapar |
|---|---|
| `NavBar.vue` | Üst menü. Masaüstünde "Yazılar"/"Hakkımda" üstüne gelince panel aşağı açılır, arkası bulanıklaşır. Mobilde tam ekran menü. Yazı detayında okuma ilerleme çizgisi. |
| `BrandMark.vue` | AİA monogramı (SVG). Gövde metin rengini, nokta vurgu rengini alır. |
| `ShowcasePost.vue` | Ana sayfadaki tam genişlik vitrin bloğu. Görsel ekrana girerken büyüyüp netleşir. |
| `TilePost.vue` | Ana sayfadaki 2×2 kutu: görsel kutunun tamamı, üstünde sadece başlık. |
| `PostCard.vue` | Yazılar sayfasındaki liste satırı (küçük görsel + başlık + özet). |
| `PostCover.vue` | Yazının kapağını basar. Görsel açılmazsa başlıktan üretilmiş renk geçişine düşer. |
| `AuthorAvatar.vue` | İsmin baş harfleri, isme özgü sabit renkte daire. |
| `CoverUploader.vue` | Yazma/düzenleme ekranında sürükle-bırak kapak yükleme. Tür ve boyut ön kontrolü burada, asıl kontrol sunucuda. |

**NavBar ayrıntıları**
- Panel hemen açılmaz, 90 ms bekler; imleç menünün üstünden geçip giderken panel
  boşuna açılıp kapanmasın diye. Panel zaten açıksa diğerine anında geçer.
- Kapanma da 150 ms gecikmelidir; imleç çubuktan panele inerken arada kapanmasın.
- Bulanık arka plan (`.scrim`) `<header>`'ın içinde. İmleç oraya inince `closeSoon` çalışır.
- Klavye: tetikleyiciye odaklanınca panel açılır, aşağı ok paneldeki ilk bağlantıya
  götürür, Esc kapatır ve odağı tetikleyiciye geri verir. Odak menüden çıkınca panel kapanır.
- "Yazılar" panelindeki son 4 yazı, panel **ilk açıldığında** bir kez yüklenir.
- Mobil menü açıkken sayfanın kaymaması için `<html>`'e `overflow: hidden` verilir.
- Sayfa değişince bütün menüler kapanır.

**Composable'lar**
- `useScrollProgress(ref)`: bir öğenin ekrandan geçişini 0–1 arası bir sayıyla `--p`
  CSS değişkenine yazar. Ölçüm her karede en fazla bir kez yapılır
  (`requestAnimationFrame`). Efektin kendisi CSS'tedir, örneğin
  `scale(clamp(0.88, 0.88 + var(--p) * 0.34, 1))`. Hareket azaltma açıksa hiç çalışmaz.
- `useThemeMode()`: `isDark` ve `toggle()`.

**Yardımcılar (`utils/`)**
- `cover.js`:
  - `resolveCover(post)`: kapak seçim sırası yüklenen kapak → yazının içindeki ilk
    resim → başlıktan üretilmiş renk geçişi.
  - `hasRealCover(post)`: yazının gerçek bir görseli var mı.
  - `gradientFor(text)`: aynı metin için her seferinde aynı renk geçişini verir.
    Palet okyanus, taş grisi ve gece mavisi tonlarından oluşur.
- `text.js`:
  - `excerpt(html, n)`: HTML'den düz metin özeti çıkarır, kelime ortasında kesmez.
  - `formatDate()`: "15 Eylül 2025" biçimi.
  - `readingTime()`: dakikada 200 kelimeden okuma süresi.

### 6.7 Sayfalar (`views/`)

**`HomeView.vue`**
1. Karşılama: `assets/portrait.jpg`, isim, "Software Engineer", tek satır unvan, iki
   buton. Açılışta portre yerine oturur, ardından metin belirir.
2. `GET /api/blogs?size=12` ile son 12 yazı gelir.
3. **Vitrin:** gerçek görseli olan (`hasRealCover`) ilk 2 yazı. Zeminleri açık gri,
   sonra siyah (`SHOWCASE_TONES`).
4. **Kutular:** vitrinde olmayanlardan 4 tane (`TILE_COUNT`). Görseli olanlar önce gelir.
5. "Tüm yazılar" butonu. Yazı hiç yoksa "Henüz yazı yok" ekranı.

**`BlogView.vue`**: bütün yazılar, sayfa başına 12, liste görünümü, önceki/sonraki.

**`BlogPostView.vue`**
- Okuma modu: kategori, tarih, başlık, yazar, okuma süresi, kapak (hafif parallax),
  içerik (`v-html`; backend'de temizlenmiş HTML).
- Kapak kuralı: yüklenmiş kapak varsa o gösterilir. Yoksa ve içerikte resim varsa
  kapak gösterilmez, aynı resim iki kez görünmesin diye. İkisi de yoksa üretilmiş
  kapak gösterilir.
- `canEdit` true ise "Düzenle" ve "Sil" görünür. Düzenleme aynı sayfada form olarak
  açılır. Silme bir onay penceresi ister.

**`BlogCreateView.vue`**: kapak, başlık, kategori, Quill editörü → `POST /api/blogs` →
yeni yazının sayfasına gider. Sunucudan gelen alan hataları ilgili alanın altında görünür.

**`ResumeView.vue`**: özgeçmiş. Deneyimler `experience`, teknolojiler `skills`
dizisinde **veri olarak** durur; içeriği değiştirmek için şablona değil bu dizilere
dokunursun. Bölümlerin `id`'leri (`deneyim`, `egitim`, `teknolojiler`) menü panelindeki
bağlantılar için.

**`ContactView.vue`**: ad, e-posta, mesaj → `POST /api/contact`. Backend'in döndüğü
mesaj olduğu gibi gösterilir.

**`LoginView.vue` / `RegisterView.vue`**: giriş sonrası `?redirect=` varsa oraya,
yoksa `/blog`'a gider.

### 6.8 Logo ve favicon

- Monogram: A harfinin ortasından inen dikey çizgi İ'nin gövdesi, okyanus mavisi
  nokta İ'nin noktası. Kaynak SVG'ler `docs/brand/` altında.
- Sitede kullanılanlar:
  - `components/BrandMark.vue` (menüde),
  - `public/favicon.svg` (sistem temasına göre renk değiştirir),
  - `public/favicon.ico` (32px, eski tarayıcılar için),
  - `public/apple-touch-icon.png` (180px, iPhone ana ekranı).

---

## 7. Lokal geliştirme

Her değişiklikte Docker'ı yeniden derlemek yavaş. Kod yazarken:

### Sadece frontend'i değiştiriyorsan

```bash
cd frontend
npm install          # ilk seferde
npm run dev          # http://localhost:5173, kaydettiğin an tarayıcı yenilenir
```

`vite.config.js`, `/api` isteklerini `http://localhost:8080`'e yönlendirir. Docker'daki
backend ise **8091**'de. İki seçeneğin var:
- Backend'i aşağıdaki gibi lokal çalıştır (8080'de açılır), ya da
- `vite.config.js`'teki `target`'ı geçici olarak `http://localhost:8091` yap.

### Backend'i lokal çalıştırmak

Java 21 gerekir. Veritabanı Docker'da kalabilir:

```bash
docker compose up -d postgres
cd backend
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5433/personal_site ./mvnw spring-boot:run
```

`application.properties` 5432 diyor ama Docker'daki PostgreSQL dışarıya 5433'ten
açık; ortam değişkeni bunu düzeltir.

### Kontroller

```bash
cd frontend && npm run lint && npm run build
cd backend && ./mvnw test
```

### Değişikliği Docker'daki siteye yansıtmak

```bash
docker compose up -d --build frontend   # sadece frontend
docker compose up -d --build backend    # sadece backend
```

---

## 8. Sık yapılan işler — tarifler

### 8.1 Kendi hesabını açıp eski yazıları sahiplenmek

1. Kayıt varsayılan olarak kapalı. Kök dizindeki `.env`'e `REGISTRATION_ENABLED=true`
   yaz, `docker compose up -d backend` çalıştır, sitede **Yazmaya başla** → kayıt ol.
   Sonra satırı sil (ya da `false` yap) ve aynı komutu tekrar çalıştır.
2. Mevcut 11 yazıyı kendine bağla:
   ```bash
   docker compose exec -T postgres psql -U devuser -d personal_site -c \
     "update blog_posts set author_id = (select id from users where email='SENIN_EPOSTAN') where author_id is null;"
   ```
3. Sayfayı yenile; yazılarında "Düzenle" ve "Sil" görünür.

### 8.2 Ana sayfadaki metni ya da fotoğrafı değiştirmek

- Metin: `frontend/src/views/HomeView.vue`, `intro-title` ve `intro-lead` satırları.
- Fotoğraf: `frontend/src/assets/portrait.jpg`'i aynı adla değiştir. Kare ve 560px
  yeterli.
- Vitrin ve kutu sayısı: aynı dosyada `SHOWCASE_TONES` ve `TILE_COUNT`.

### 8.3 Özgeçmişi güncellemek

`frontend/src/views/ResumeView.vue` → `experience` ve `skills` dizileri. Tanıtım
paragrafı şablonun üstünde.

### 8.4 Renkleri değiştirmek

Üç yer:
1. `frontend/src/assets/main.css` → `:root` ve `:root[data-theme='dark']` token'ları
   (ve `.tone-inverse` blokları).
2. `frontend/src/main.js` → Vuetify `light` / `dark` temaları (form alanları için).
3. `frontend/index.html` → ilk boyama zemin renkleri; `public/favicon.svg` renkleri.

### 8.5 Yeni bir sayfa eklemek

1. `frontend/src/views/YeniView.vue` oluştur.
2. `router/index.js`'e rota ekle.
3. Menüde görünsün istiyorsan `components/NavBar.vue` → `items` dizisine ekle.
   İstersen `App.vue`'daki alt bilgiye de.

### 8.6 Veritabanına yeni alan eklemek (örnek: yazıya "öne çıkar" bayrağı)

1. `backend/src/main/resources/db/migration/V6__add_featured_to_blog_posts.sql`:
   ```sql
   alter table blog_posts add column if not exists featured boolean not null default false;
   ```
2. `entity/BlogPost.java`'ya alan, getter ve setter ekle.
3. Dışarıya açılacaksa `dto/BlogPostResponse.java`'ya, gelecekse
   `dto/BlogPostRequest.java`'ya ekle.
4. Servis mantığını yaz, testini ekle.
5. `docker compose up -d --build backend`. Flyway V6'yı otomatik uygular.

### 8.7 Yeni bir API ucu eklemek

1. Controller'a metodu yaz; iş kuralını servise koy.
2. `config/SecurityConfig.java`'da bu adresin herkese mi, girişlilere mi açık olduğunu
   belirt. Belirtmezsen `anyRequest().permitAll()` yüzünden **herkese açık** olur.
3. Frontend'de `services/api.js`'e fonksiyonunu ekle.

---

## 9. Veritabanına bakmak

### psql ile (en hızlısı)

```bash
docker compose exec postgres psql -U devuser -d personal_site
```

```sql
\dt                                                      -- tablolar
select id, title, author_id, created_at from blog_posts order by created_at desc;
select * from contact_messages order by created_at desc;  -- iletişim mesajları
\q                                                       -- çık
```

### pgAdmin ile

1. `COMPOSE_PROFILES=dev docker compose up -d` (pgAdmin varsayılan olarak kapalı), sonra http://localhost:5051 → admin@admin.com / admin
2. Servers → Register → Server
   - Name: `personal_website`
   - Connection: Host `postgres`, Port `5432`, Database `personal_site`, Username
     `devuser`, Password `devpass`

Host'a `localhost` değil `postgres` yazılır: pgAdmin de bir container ve veritabanına
Docker ağı üzerinden servis adıyla ulaşır.

### Yedek almak / geri yüklemek

```bash
docker compose exec -T postgres pg_dump -U devuser personal_site > yedek_$(date +%F).sql
cat yedek_2026-10-12.sql | docker compose exec -T postgres psql -U devuser -d personal_site
```

Geri yükleme boş bir veritabanına yapılmalı; dolu veritabanında "tablo zaten var"
hataları alırsın.

---

## 10. Bilinen pürüzler

Sitenin çalışmasını engellemiyorlar ama bilmen iyi olur.

| Konu | Nerede | Not |
|---|---|---|
| Kayıt sayfası görünüyor | `RegisterView`, menü | Kayıt backend'de kapalı (`REGISTRATION_ENABLED=false`); sayfa açılır ama "kayıt kapalı" mesajı döner. İstersen bağlantılar menüden kaldırılabilir. |
| Kullanılmayan etiket tabloları | `tags`, `post_tags`, `entity/Tag.java` | V1'den kalma, hiçbir yerde kullanılmıyor. |
| Resimler yazının içinde gömülü | Quill → base64 | Resimli yazılar veritabanında büyük yer kaplar ve liste cevabını ağırlaştırır. İleride resimleri de `/api/uploads`'a taşımak iyi olur. |
| Node 25 + devtools eklentisi | `vite-plugin-vue-devtools` | 8.2.1'e yükseltildi; sorun yok. Tekrar çıkarsa: `NODE_OPTIONS=--no-webstorage npm run dev`. |

---

## 11. Sorun giderme

| Belirti | Sebep / Çözüm |
|---|---|
| `port is already allocated` | Aynı portu başka bir şey kullanıyor. `docker ps` ile bak, diğerini durdur. |
| Backend açılmıyor, logda Flyway/validate hatası | Bir migration değiştirilmiş ya da entity ile şema uyuşmuyor. Var olan migration'ı geri al, değişikliği yeni bir `V…` dosyasıyla yap. |
| Backend veritabanına bağlanamıyor | PostgreSQL henüz hazır değil: `docker compose restart backend`. |
| Yazı kaydederken 403 | CSRF çerezi yok ya da oturum düşmüş. Sayfayı yenile, tekrar giriş yap. |
| Yazıda "Düzenle" görünmüyor | Yazı senin değil ya da sahipsiz. Bkz. [8.1](#81-kendi-hesabını-açıp-eski-yazıları-sahiplenmek). |
| Görsel yüklerken 413 | nginx `client_max_body_size` değişmiş olabilir (50m olmalı). |
| 429 "Çok fazla deneme" | Hız sınırı. 15 dakika bekle ya da `docker compose restart backend`. |
| İletişim maili gelmiyor | `.env`'de gerçek Gmail uygulama şifresi yok. Mesajlar yine `contact_messages` tablosunda. |
| Değişikliğim sitede görünmüyor | `docker compose up -d --build frontend` (veya `backend`) çalıştırmadın. Ya da tarayıcı önbelleği: Cmd+Shift+R. |
