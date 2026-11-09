# Siteyi Canlıya Çıkarma Rehberi

> **Kısa yol:** Hetzner + Cloudflare ile adım adım kurulum için [deploy/YAYINLA.md](deploy/YAYINLA.md).
> Bu doküman seçeneklerin ve ayrıntıların uzun açıklaması.
>
> **Kasım 2026 güncellemesi:** İletişim formu ve Gmail ayarları kaldırıldı (KVKK).
> Aşağıdaki `MAIL_*`, `CONTACT_RECIPIENT` ve "iletişim formundan mesaj at" adımlarını atla.

Bu doküman, şu an sadece kendi bilgisayarında çalışan siteyi internete açmak için
seçeneklerini ve her birinin adımlarını anlatır.

Okuma sırası:
1. **Bölüm 0** — Yayına çıkmadan önce mutlaka yapılması gerekenler (güvenlik). Atlanmaz.
2. **Bölüm 1** — Seçenekleri karşılaştır, birini seç.
3. **Bölüm 2/3/4** — Seçtiğin yolun adımlarını uygula.
4. **Bölüm 5** — Alan adı al ve bağla.
5. **Bölüm 6** — Notlarını/verilerini yedekle. Bu bölüm en az yayına çıkmak kadar önemli.

---

## Bölüm 0 — Yayına Çıkmadan Önce: Güvenlik

Şu an proje "kendi bilgisayarımda denerim" ayarlarıyla duruyor. İnternete açık bir
sunucuda bu ayarlar sorun çıkarır. Hangi yolu seçersen seç, aşağıdaki maddeleri
halletmen gerekiyor.

### 0.1 ✅ Blog yazma uçları artık korumalı

Bu madde **çözüldü**. Site artık üyelik sistemiyle çalışıyor:

- Yazı yazmak için kayıt olup giriş yapmak gerekiyor
- Her yazının bir sahibi var (`blog_posts.author_id`)
- Bir yazıyı **sadece onu yazan kişi** güncelleyip silebilir; başkası denerse 403 alır
- Oturum HttpOnly çerezde tutuluyor, CSRF koruması açık
- Kullanıcıların yazdığı HTML jsoup ile temizleniyor (XSS koruması)
- Kayıt, giriş ve iletişim formunda IP bazlı hız sınırı var

Yayına çıkmadan önce yapılacak şey: kendi hesabını açıp eski yazıları sahiplenmek.
**Şu anki durum (Ekim 2026):** veritabanında hiç kullanıcı yok ve 11 yazının 11'i de
sahipsiz, yani hiçbiri arayüzden düzenlenemiyor. Kayıt olduktan sonra şu komutu
çalıştır (lokalde ya da sunucuda, yazıların hangisindeyse):

```bash
docker compose exec -T postgres psql -U devuser -d personal_site -c \
  "update blog_posts set author_id = (select id from users where email='SENIN_EPOSTAN') where author_id is null;"
```

Sahipsiz yazılar (seed verisinden gelenler) aksi halde arayüzden düzenlenemez.

> ⚠️ Halen eksik olan iki şey: **e-posta doğrulama** ve **şifre sıfırlama**.
> Yani sahte e-postayla hesap açılabilir ve şifresini unutan kullanıcı giremez.
> Kişisel blog için kabul edilebilir, ama gerçek bir üyelik sistemi istiyorsan bunlar eklenmeli.

### 0.2 Veritabanı şifresi

`docker-compose.yml` şifreyi `.env`'deki `POSTGRES_PASSWORD`'den okur; boşsa lokal
geliştirme şifresi `devpass` kullanılır. Sunucuda mutlaka doldur (Bölüm 2.5):

```bash
openssl rand -base64 24
```

> ⚠️ PostgreSQL imajı `POSTGRES_PASSWORD`'ü **sadece veritabanı ilk kez kurulurken**
> (volume boşken) kullanır. Volume zaten doluysa şifreyi veritabanının içinden
> değiştirmen gerekir:
> ```bash
> docker compose exec postgres psql -U devuser -d personal_site -c "alter user devuser with password 'YENI_SIFRE';"
> ```
> Yeni sunucuda boş volume'le başladığın için bu sorun çıkmaz.

### 0.3 ✅ Veritabanı ve pgAdmin portları dışarı kapalı

PostgreSQL (`5433`), backend (`8091`) ve frontend (`8090`) portları `127.0.0.1:`
önekiyle açılıyor, yani sadece makinenin kendisinden erişilir. Sunucuda internete
bakan tek servis Caddy (80/443).

pgAdmin sadece `dev` profilinde çalışır, sunucuda hiç açılmaz. Sunucudaki
veritabanına bakman gerekirse SSH tüneli kullan (Bölüm 6.3).

> ⚠️ Docker, ufw kurallarını atlayabilir (iptables'a doğrudan yazar). `127.0.0.1:`
> öneki bu yüzden önemli; port eklerken onu silme.

### 0.4 ✅ Gizli bilgiler .env'de

Kök dizindeki `.env` `.gitignore`'da; şablonu `.env.example` git'te. Sunucuda
`cp .env.example .env` ile başlayıp doldurursun (Bölüm 2.5).

> Eski `backend/.env` dosyası ilk commit'le GitHub'a gitmişti; içindeki
> veritabanı/pgAdmin şifrelerini başka bir yerde kullanıyorsan değiştir.

### 0.5 ✅ Otomatik yeniden başlama ve log sınırı

Her serviste `restart: unless-stopped` ve log sınırı (10 MB × 3 dosya) var.
Sunucu yeniden başlayınca site kendiliğinden kalkar.

> Lokalde de geçerli: Docker Desktop açılınca container'lar kendiliğinden başlar.
> İstemiyorsan `docker compose stop` ile durdur; durdurulanlar bir daha kendiliğinden kalkmaz.

### 0.6 ✅ Kayıt varsayılan olarak kapalı

`docker-compose.yml` backend'e `REGISTRATION_ENABLED=false` verir; kimse hesap açamaz
ve sitede "Yazmaya başla" / "Kayıt ol" bağlantıları görünmez. Kendi hesabını açmak
için `.env`'e geçici olarak `REGISTRATION_ENABLED=true` yaz,
`docker compose up -d backend`, kayıt ol, sonra satırı silip komutu tekrarla
(REHBER.md 8.1).

### 0.7 ✅ Canlı ortam ayarları

Uploads volume'ü (`UPLOADS_DIR`) compose'da. Oturum çerezinin `Secure` bayrağı
`.env`'deki `COOKIE_SECURE=true` ile açılır (Bölüm 2.5).

---

## Bölüm 1 — Seçenekler

### Karşılaştırma

| | **A) VPS kirala** | **B) Hazır platform** | **C) Karma** |
|---|---|---|---|
| Ne yaparsın | Bir Linux sunucu kiralar, şu an localde çalıştırdığın `docker compose up` komutunu orada çalıştırırsın | Railway/Render'a git reposunu bağlarsın, kendisi build eder | Frontend Vercel'de, backend+DB Railway'de |
| Aylık maliyet | ~4-6 EUR (Hetzner CX23) | 5-20 USD (ücretsiz katman DB için yetersiz) | ~5-10 USD |
| Kurulum süresi | 1-2 saat | 30-60 dakika | 1-2 saat |
| Kod değişikliği | **Yok** — mevcut yapı aynen çalışır | **Var** — nginx proxy yapısı bozulur, CORS ve API adresi ayarı gerekir | **Var** — B ile aynı |
| Veritabanı kontrolü | Tamamen sende, volume'de | Platformun yönetilen DB'si | Platformun yönetilen DB'si |
| Yedekleme | Sen kurarsın (Bölüm 6) | Çoğunlukla otomatik | Çoğunlukla otomatik |
| Sunucu bakımı | Sende (güncelleme, güvenlik) | Platformda | Platformda |
| Öğrenme değeri | Yüksek — Linux, Docker, nginx, SSL | Düşük | Orta |

### Hangisi sana uygun?

**Seçenek A (VPS) — çoğu durumda en mantıklısı.** Çünkü projen zaten tamamen
Docker'lanmış ve `docker-compose.yml` dosyan tüm servisleri (HTTPS dahil) birlikte ayağa kaldırıyor.
Sunucuda kelimenin tam anlamıyla **aynı komutu** çalıştırırsın. Frontend'in nginx'i
`/api` isteklerini backend'e proxy'liyor, yani her şey tek adresten çalışıyor —
CORS sorunu bile çıkmaz. Hiçbir kod değişikliği gerekmez.

Veritabanı senin kontrolünde bir volume'de durur; notların, blog yazıların orada
kalır, istediğin an yedeğini alırsın.

**Seçenek B (Railway/Render) — sunucuyla uğraşmak istemiyorsan.** Ama dikkat:
mevcut mimarin tek container'dan nginx ile proxy yapıyor. Railway her servisi ayrı
bir adrese koyar. Frontend ile backend farklı adreslerde olunca oturum çerezi ve CSRF
çerezi **siteler arası** (cross-site) hale gelir ve giriş/yazma bozulur (ayrıntı:
Bölüm 3.1). Yani "kolay" görünen yol aslında ciddi kod değişikliği istiyor.

**Seçenek C (Vercel + Railway) —** B ile aynı değişiklikleri gerektirir, ek olarak iki
farklı platform yönetirsin. Frontend çok hızlı açılır. Bu proje ölçeğinde getirisi sınırlı.

---

## Bölüm 2 — Seçenek A: VPS ile Yayına Çıkma

### 2.1 Sunucu kirala

Sağlayıcı seçenekleri:

| Sağlayıcı | Ürün | Fiyat | Not |
|---|---|---|---|
| **Hetzner** | CX23 (2 vCPU, 4GB RAM) | ~4-6 EUR/ay + KDV | Fiyat/performans en iyisi, Almanya/Finlandiya. 2026'da iki kez zam yaptı |
| DigitalOcean | Basic Droplet (1GB) | 6 USD/ay | Dokümantasyonu çok iyi, yeni başlayan dostu |
| Vultr | Regular (1GB) | 5-6 USD/ay | İstanbul lokasyonu var, Türkiye'den hızlı |
| Contabo | VPS S | ~5 EUR/ay | Ucuz ama destek yavaş |

> Fiyatlar ve ürün adları değişebiliyor; almadan önce sağlayıcının sitesinden güncelini kontrol et.

Bu proje için **en az 2GB RAM** öneririm. Java (Spring Boot) + PostgreSQL + Nginx
birlikte 1GB'a sığar ama build sırasında Maven zorlanır. Hetzner CX23 (4GB) rahat eder.

Kurarken:
- İşletim sistemi: **Ubuntu 24.04 LTS**
- SSH anahtarı ekle (şifreyle girmekten çok daha güvenli). Anahtarın yoksa:
  `ssh-keygen -t ed25519` ile oluşturur, `~/.ssh/id_ed25519.pub` içeriğini panele yapıştırırsın.

Sunucu hazır olunca sana bir **IP adresi** verir, örneğin `91.99.12.34`. Bu adresi not et.

### 2.2 Sunucuya bağlan ve hazırla

```bash
ssh root@91.99.12.34
```

Sistemi güncelle ve Docker'ı kur:

```bash
apt update && apt upgrade -y
curl -fsSL https://get.docker.com | sh
```

Kurulumu doğrula:

```bash
docker --version
docker compose version
```

> İyi pratik: SSH anahtarınla girebildiğini doğruladıktan sonra şifreyle girişi kapat
> (`/etc/ssh/sshd_config` → `PasswordAuthentication no`, sonra `systemctl restart ssh`).
> Botlar sunucunu ilk saatten itibaren şifre denemeye başlar.

### 2.3 Güvenlik duvarını kur

Sadece gerekli portları aç. Bunu Docker kurduktan **sonra** yap:

```bash
ufw allow OpenSSH
ufw allow 80/tcp
ufw allow 443/tcp
ufw enable
```

> ⚠️ `ufw enable` demeden önce `ufw allow OpenSSH` yazdığından emin ol, yoksa
> kendi sunucuna giremezsin.

> ⚠️ Docker, ufw kurallarını bazen atlar (iptables'a doğrudan yazar). Bu yüzden
> Bölüm 0.3'teki `127.0.0.1:` öneki ayrıca önemli — sadece ufw'ye güvenme.

### 2.4 Projeyi sunucuya aktar

Projen GitHub'da (`github.com/Aliimranatabey/mywebsite`):

```bash
apt install -y git
git clone https://github.com/Aliimranatabey/mywebsite.git mywebsite
cd mywebsite
```

Önce yerel değişikliklerini commit'leyip push'lamış olman gerekir; sunucu GitHub'daki
son hali çeker. Repo **private** ise `git clone` kullanıcı adı ve şifre ister; şifre
yerine GitHub'da oluşturduğun bir *Personal Access Token* (sadece okuma yetkili) gir.

> Klasör adını `mywebsite` tut. Docker volume adları klasör adından türer
> (`mywebsite_personal_website_data`); Bölüm 2.7 ve 6'daki komutlar bu adı kullanıyor.

GitHub'da değilse, kendi bilgisayarından kopyala (bu komutu **kendi Mac'inde** çalıştır):

```bash
rsync -av --exclude node_modules --exclude target --exclude .git \
  ~/vscode-workspace/mywebsite/ root@91.99.12.34:/root/mywebsite/
```

### 2.5 Gizli bilgileri ayarla

Sunucuda, proje kökünde `.env` dosyasını oluştur:

```bash
cd /root/mywebsite
cp .env.example .env
openssl rand -base64 24      # çıkan değeri kopyala, POSTGRES_PASSWORD'e yapıştıracaksın
nano .env
```

İçini doldur:

```
MAIL_USERNAME=aliimranatabey@gmail.com
MAIL_PASSWORD=16haneliuygulamasifresi
CONTACT_RECIPIENT=aliimranatabey@gmail.com
POSTGRES_PASSWORD=<openssl çıktısı>
REGISTRATION_ENABLED=false

COMPOSE_PROFILES=prod
DOMAIN=aliimranatabey.com
COOKIE_SECURE=true
```

Kaydet: `Ctrl+O`, `Enter`, `Ctrl+X`. Sonra dosyayı sadece root okusun:
`chmod 600 .env`

**Gmail uygulama şifresi** (`MAIL_PASSWORD`) normal Gmail şifren değildir:
1. [myaccount.google.com](https://myaccount.google.com) → **Güvenlik** → **2 Adımlı
   Doğrulama**'yı aç (kapalıysa uygulama şifresi seçeneği görünmez)
2. Aynı sayfada arama kutusuna "Uygulama şifreleri" yaz → ad olarak `mywebsite` gir → **Oluştur**
3. Çıkan 16 harfi boşluksuz `MAIL_PASSWORD=`'e yapıştır

Bu şifreyi lokalde de denemek istersen kendi Mac'indeki `.env`'e aynı iki satırı yaz
ve `docker compose up -d backend` çalıştır. Lokal `.env`'e `POSTGRES_PASSWORD`,
`COMPOSE_PROFILES`, `DOMAIN` yazma; lokal veritabanı `devpass` ile kurulu.

### 2.6 Üretim ayarları

Ayrıca bir şey yapman gerekmiyor. `docker-compose.yml` aynı dosyayla hem lokalde hem
sunucuda çalışır; farkı `.env` belirler:

| | Lokal | Sunucu |
|---|---|---|
| `COMPOSE_PROFILES` | boş | `prod` → Caddy (HTTPS) açılır |
| Siteye giriş | `http://localhost:8090` | `https://DOMAIN` |
| pgAdmin | `COMPOSE_PROFILES=dev docker compose up -d` ile | çalışmaz |
| Veritabanı şifresi | `devpass` | `.env`'deki `POSTGRES_PASSWORD` |

Hazır olanlar: `restart: unless-stopped`, log sınırı (10 MB × 3), postgres/backend/
frontend portları sadece `127.0.0.1`'de, kapak görselleri için uploads volume'ü,
backend'in veritabanı hazır olmadan açılmaması (healthcheck).

### 2.7 Mevcut yazılarını sunucuya taşı

Sunucudaki veritabanı **boş** başlar. Bilgisayarındaki 11 yazının oraya gitmesi için
yerel veritabanını döküp sunucuda geri yüklemen gerekir. Bu adımı backend'i ilk kez
başlatmadan **önce** yap; backend önce açılırsa boş tabloları kendisi kurar ve geri
yükleme "tablo zaten var" hatası verir.

**1. Kendi Mac'inde** (proje klasöründe, lokal Docker çalışırken) yedeği al ve sunucuya gönder:

```bash
docker compose exec -T postgres pg_dump -U devuser personal_site > tasima.sql
scp tasima.sql root@91.99.12.34:/root/mywebsite/
```

Kapak görseli yüklediysen onları da paketle (şu an hiç yok, ileride gerekirse):

```bash
docker run --rm -v mywebsite_personal_website_uploads:/data -v "$PWD":/yedek alpine \
  tar czf /yedek/uploads.tgz -C /data .
scp uploads.tgz root@91.99.12.34:/root/mywebsite/
```

**2. Sunucuda:** container'ları ve volume'leri oluştur ama başlatma, sonra sadece
veritabanını aç ve yedeği yükle:

```bash
cd /root/mywebsite
docker compose up --no-start --build     # imajları derler, volume'leri kurar, başlatmaz
docker compose start postgres
sleep 5
cat tasima.sql | docker compose exec -T postgres psql -U devuser -d personal_site
```

Görselleri taşıdıysan:

```bash
docker run --rm -v mywebsite_personal_website_uploads:/data -v "$PWD":/yedek alpine \
  tar xzf /yedek/uploads.tgz -C /data
```

Yedek, Flyway'in kayıt tablosunu da içerdiği için backend açılınca migration'ları
tekrar çalıştırmaz, şemayı olduğu gibi kabul eder.

### 2.8 Ayağa kaldır

Önce alan adının DNS kayıtlarını sunucuya yönlendir (Bölüm 5.2) ve `dig +short
alanadin.com` sunucunun IP'sini verene kadar bekle. Caddy sertifikayı alabilmek için
buna ihtiyaç duyar. Sonra:

```bash
docker compose up -d --build
```

İlk build 5-10 dakika sürebilir (Maven bağımlılıkları + npm paketleri iner).

Kontrol et:

```bash
docker compose ps          # postgres, backend, frontend, caddy "Up" olmalı
docker compose logs -f     # hata var mı bak, çıkmak için Ctrl+C
```

Tarayıcıdan `https://alanadin.com` adresini aç. Siten yayında.

> IP adresiyle (`http://91.99.12.34`) açılmaz; bu bilerek böyle. Caddy sadece alan
> adına gelen istekleri karşılar, frontend'in portu da sadece sunucunun içine açık.

### 2.9 Sonrası

- İletişim formundan kendine bir mesaj gönder, mail geliyor mu bak
- Yedek yok → **Bölüm 6** şart
- Google'a tanıt: [Search Console](https://search.google.com/search-console)'a
  alan adını ekle, site haritası olarak `https://alanadin.com/sitemap.xml` ver

---

## Bölüm 3 — Seçenek B: Railway ile Yayına Çıkma

Railway, GitHub reponu bağladığında Dockerfile'ları görüp servisleri kendisi ayağa
kaldırır. Sunucu yönetmezsin, ama mimaride değişiklik gerekir.

### 3.1 Önce kodda yapman gereken değişiklikler

> ⚠️ **Bu bölümün eski hali eksikti.** Frontend ile backend ayrı adreslerde olursa
> sadece adres ve CORS ayarı yetmez:
> - **Oturum çerezi gitmez.** `frontend.up.railway.app` ile `backend.up.railway.app`
>   tarayıcı için farklı sitelerdir. `JSESSIONID` çerezi varsayılan olarak siteler arası
>   isteklerde gönderilmez; giriş yapsan da bir sonraki istekte oturum yok görünür.
>   Çözmek için çerezi `SameSite=None; Secure` yapmak gerekir.
> - **CSRF bozulur.** Backend `XSRF-TOKEN` çerezini kendi adresine yazar. Frontend
>   başka bir adreste çalıştığı için bu çerezi okuyamaz ve header'a koyamaz; bütün
>   yazma istekleri 403 alır. Token'ın cevap gövdesinden okunacak şekilde
>   değiştirilmesi gerekir.
>
> **Daha kolay yol:** Railway'de de mevcut yapıyı koru. Frontend servisinin nginx'i
> `/api`'yi backend'in Railway içi adresine (`http://backend.railway.internal:8080`)
> proxy'lesin; dışarıya sadece frontend'e domain ver. Böylece her şey yine tek adresten
> çalışır ve aşağıdaki değişikliklerin hiçbiri gerekmez. Bunun için sadece
> `frontend/nginx.conf`'taki `proxy_pass` adresinin değişmesi yeterli.

Ayrı adres yolunu yine de seçersen gereken değişiklikler:

**1. Frontend, backend adresini ortam değişkeninden okusun.**
`frontend/src/services/api.js` içinde:

```js
// Eski:
const API_URL = '/api';

// Yeni:
const API_URL = import.meta.env.VITE_API_URL || '/api';
```

Vite'ta ortam değişkenleri **build sırasında** gömülür, çalışma anında değil.
Yani `VITE_API_URL` değerini Railway'de build ayarlarına vermen gerekir.

**2. Backend, frontend'in adresine CORS izni versin.**
`CorsConfig.java` izinli adresleri zaten ortam değişkeninden okuyor; kod değişikliği
gerekmez. Backend servisine şu değişkeni ver (virgülle birden fazla adres yazılabilir):

```
APP_CORS_ALLOWED_ORIGINS=https://senin-frontend-adresin.up.railway.app
```

**3. Çerezler ve CSRF** (yukarıdaki uyarı): oturum çerezini
`SERVER_SERVLET_SESSION_COOKIE_SAME_SITE=none` ve
`SERVER_SERVLET_SESSION_COOKIE_SECURE=true` ile ayarla. CSRF token'ını frontend'in
`GET /api/auth/csrf` cevabından okuyup header'a koymasını sağla
(`services/api.js` ve `stores/auth.js`).

### 3.2 Railway adımları

1. [railway.app](https://railway.app) → GitHub hesabınla giriş yap
2. **New Project** → **Deploy from GitHub repo** → reponu seç
3. **Veritabanı ekle:** proje içinde **New** → **Database** → **PostgreSQL**.
   Railway sana `DATABASE_URL`, `PGUSER`, `PGPASSWORD` gibi değişkenler verir.
4. **Backend servisi ekle:** **New** → **GitHub Repo** → Root Directory'yi `backend`
   olarak ayarla. Değişkenleri gir:
   ```
   SPRING_DATASOURCE_URL=jdbc:postgresql://${{Postgres.PGHOST}}:${{Postgres.PGPORT}}/${{Postgres.PGDATABASE}}
   SPRING_DATASOURCE_USERNAME=${{Postgres.PGUSER}}
   SPRING_DATASOURCE_PASSWORD=${{Postgres.PGPASSWORD}}
   SPRING_FLYWAY_ENABLED=true
   MAIL_USERNAME=aliimranatabey@gmail.com
   MAIL_PASSWORD=16haneliuygulamasifresi
   CONTACT_RECIPIENT=aliimranatabey@gmail.com
   ```
   **Settings → Networking → Generate Domain** ile backend'e bir adres ver.
5. **Frontend servisi ekle:** Root Directory `frontend`. Değişken olarak
   `VITE_API_URL=https://backend-adresin.up.railway.app/api` gir.
   Buna da domain üret.
6. Backend servisine `APP_CORS_ALLOWED_ORIGINS` değişkenini frontend adresiyle ekle
   (yeniden deploy olur).

### 3.3 Dikkat edilecekler

- **Ücretsiz katman yetmez.** Railway'in ücretsiz kredisi (~5 USD) sürekli çalışan
  bir PostgreSQL + Java servisini bir ay taşımaz. Hobby planı 5 USD/ay + kullanım.
- **Render'ın ücretsiz katmanında servisler uyur.** 15 dakika trafik olmazsa kapanır,
  sonraki ziyaretçi 30-50 saniye bekler. Ücretsiz PostgreSQL'i de 90 gün sonra siler.
- **Flyway migration'ları** ilk açılışta otomatik çalışır, tabloların kurulur.

---

## Bölüm 4 — Seçenek C: Vercel (frontend) + Railway (backend + DB)

Bölüm 3.1'deki kod değişikliklerinin **aynısı**, çerez ve CSRF kısmı dahil, burada da
gerekir. Vercel ile Railway kesinlikle farklı siteler olduğu için "tek adres" kısayolu
burada yok.

1. Backend ve PostgreSQL'i Bölüm 3.2'deki gibi Railway'de kur
2. [vercel.com](https://vercel.com) → **Import Git Repository** → reponu seç
3. **Root Directory:** `frontend`
4. Framework: Vite (otomatik algılar). Build: `npm run build`, Output: `dist`
5. **Environment Variables:** `VITE_API_URL = https://backend-adresin.up.railway.app/api`
6. Deploy

Avantajı: frontend Vercel'in CDN'inden dağıtılır, dünyanın her yerinden çok hızlı
açılır ve ücretsizdir. Dezavantajı: iki ayrı platform yönetirsin, CORS ayarı şart.

---

## Bölüm 5 — Alan Adı (Domain) Alma ve Bağlama

### 5.1 Nereden alınır?

| Sağlayıcı | .com fiyatı (yıllık) | Not |
|---|---|---|
| **Cloudflare Registrar** | ~10-11 USD | Maliyetine satar, kâr koymaz. Yenileme fiyatı da aynı kalır. DNS yönetimi ücretsiz ve çok iyi. **Önerdiğim.** |
| **Porkbun** | ~11 USD | İlk yıl indirimli, arayüzü sade, WHOIS gizliliği ücretsiz |
| Namecheap | ~14 USD | Çok yaygın, desteği iyi |
| GoDaddy | ~20 USD+ | İlk yıl ucuz gösterip yenilemede pahalıya bindirir, önermem |
| Natro / İsimtescil | ~300-500 TL | `.com.tr` almak istersen Türkiye'den almak gerekir |

**Tuzağa dikkat:** Birçok sağlayıcı ilk yıl 1-2 dolara verip ikinci yıl 25-40 dolara
yeniler. Alırken **yenileme fiyatına** bak, ilk yıl fiyatına değil.

`aliimranatabey.com` gibi bir isim mantıklı olur. `.com` bulamazsan `.dev`, `.me`
veya `.com.tr` bakabilirsin.

### 5.2 Adı sunucuya yönlendir (Seçenek A için)

Aldığın yerin DNS paneline gir ve iki kayıt ekle:

| Tip | İsim (Name) | Değer (Value) | TTL |
|---|---|---|---|
| A | `@` | `91.99.12.34` (sunucu IP'n) | Auto |
| A | `www` | `91.99.12.34` | Auto |

`@` işareti "alan adının kendisi" demektir (`aliimranatabey.com`).
İkinci satır `www.aliimranatabey.com` içindir.

Yayılması genelde 5-30 dakika sürer. Kontrol:

```bash
dig +short aliimranatabey.com
```

Sunucunun IP'sini yazdırıyorsa hazır.

### 5.3 Railway/Vercel için alan adı bağlama

- **Railway:** Servis → Settings → Networking → **Custom Domain** → alan adını gir.
  Sana bir `CNAME` değeri verir, onu DNS paneline eklersin.
- **Vercel:** Project → Settings → Domains → alan adını ekle, gösterdiği kaydı gir.

Her ikisi de HTTPS sertifikasını **otomatik** kurar, Bölüm 5.4'ü atlayabilirsin.

### 5.4 HTTPS (Seçenek A için)

Ayrıca nginx ya da sertifika kurmana gerek yok. `docker-compose.yml`'de **Caddy**
servisi hazır duruyor. Caddy sertifikayı Let's Encrypt'ten ücretsiz alır, süresi
dolmadan kendisi yeniler, `http://` isteklerini `https://`'e, `www.` ile gelenleri de
çıplak alan adına yönlendirir. Ayarları kökteki `Caddyfile`'da.

Açmak için sunucudaki `.env`'de şu üç satır yeterli (Bölüm 2.5):

```
COMPOSE_PROFILES=prod
DOMAIN=aliimranatabey.com
COOKIE_SECURE=true
```

İstek sırası: ziyaretçi → **Caddy** (443, HTTPS) → frontend'in **nginx**'i (sayfalar,
`/api`'yi iletir) → **backend**. Caddy dışında hiçbir servis internete açık değil.

Kontrol:

```bash
docker compose logs -f caddy     # "certificate obtained successfully" satırını görürsün
```

> ⚠️ Caddy'yi açmadan **önce** DNS kayıtları sunucunu göstermeli (Bölüm 5.2).
> Göstermiyorsa Let's Encrypt doğrulayamaz; Caddy bir süre sonra tekrar dener ama
> çok sık başarısız denemede haftalık limite takılabilirsin.

> `caddy_data` volume'ünü silme: sertifikalar orada durur.

Hız sınırı (`RateLimitFilter`) ziyaretçinin gerçek IP'sini `X-Forwarded-For`
zincirinden alır; Caddy sahte header'ı atıp gerçek IP'yi yazar. Caddy'de
`trusted_proxies` ayarlama, varsayılanı doğru olanıdır.

---

## Bölüm 6 — Notlarını ve Verilerini Korumak

Bu bölüm senin için muhtemelen en kritik olanı: blog yazıların ve notların
veritabanında duruyor. Sunucu çökerse, yanlış komut yazarsan veya sağlayıcı
hesabını kapatırsa bu veriler gider. **Yedek almazsan er geç kaybedersin.**

### 6.1 Veriler nerede duruyor?

İki Docker volume'ünde:

| Volume | İçinde |
|---|---|
| `mywebsite_personal_website_data` | Veritabanı: yazılar, kullanıcılar, iletişim mesajları |
| `mywebsite_personal_website_uploads` | Yüklenen kapak görselleri |

Yazıların içine yapıştırdığın resimler veritabanında durur. Sadece "Kapak görseli"
alanından yüklenenler uploads volume'üne gider. İkisini de yedeklemen gerekir.

Container'ı silsen bile volume kalır (bu yüzden `docker compose down` güvenli).
Ama şu komut **her şeyi siler**:

```bash
docker compose down -v      # ← -v volume'leri de siler, canlıda ASLA yazma
```

### 6.2 Elle yedek alma

Tek komutla veritabanının tamamını bir dosyaya döker:

```bash
docker compose exec -T postgres pg_dump -U devuser personal_site > yedek_$(date +%F).sql
```

Geri yükleme (yedek, tabloları sıfırdan kurar; bu yüzden **boş** bir veritabanına
yüklenmeli, dolu olana yüklersen "zaten var" hataları alırsın):

```bash
cat yedek_2026-09-20.sql | docker compose exec -T postgres psql -U devuser -d personal_site
```

Kapak görsellerinin yedeği:

```bash
docker run --rm -v mywebsite_personal_website_uploads:/data -v "$PWD":/yedek alpine \
  tar czf /yedek/uploads_$(date +%F).tgz -C /data .
```

### 6.3 Yedeği kendi bilgisayarına çekme

Yedek sunucuda durursa, sunucu gittiğinde yedek de gider. Kendi Mac'ine indir:

```bash
scp root@91.99.12.34:/root/mywebsite/yedek_*.sql ~/Desktop/
```

Aynı mantıkla pgAdmin'e güvenli erişmek için SSH tüneli açabilirsin
(port dışarı açık olmadan):

```bash
ssh -L 5433:localhost:5433 root@91.99.12.34
```

Bu açıkken kendi bilgisayarından `localhost:5433` ile sunucudaki veritabanına bağlanırsın.

### 6.4 Otomatik günlük yedek

Sunucuda `crontab -e` yaz ve şu satırı ekle:

```
0 3 * * * cd /root/mywebsite && docker compose exec -T postgres pg_dump -U devuser personal_site > /root/yedekler/yedek_$(date +\%F).sql 2>/dev/null
```

Her gece 03:00'te yedek alır. Önce klasörü oluştur: `mkdir -p /root/yedekler`

Görselleri de her gece yedeklemek için:

```
15 3 * * * docker run --rm -v mywebsite_personal_website_uploads:/data -v /root/yedekler:/yedek alpine tar czf /yedek/uploads_$(date +\%F).tgz -C /data . 2>/dev/null
```

Eski yedekleri temizlemek için (30 günden eskiyi sil):

```
0 4 * * * find /root/yedekler -name "yedek_*.sql" -mtime +30 -delete
0 4 * * * find /root/yedekler -name "uploads_*.tgz" -mtime +30 -delete
```

> En sağlamı: yedekleri ayrıca bir bulut depolamaya (Google Drive, S3, Backblaze)
> göndermek. Sunucu tamamen kaybolsa bile verin durur.

---

## Bölüm 7 — Güncelleme Akışı

Siteyi yayına aldıktan sonra kodda değişiklik yaptığında:

```bash
# Kendi bilgisayarında değişikliği yap, test et, GitHub'a gönder
git add .
git commit -m "Açıklama"
git push

# Sunucuda
ssh root@91.99.12.34
cd /root/mywebsite
git pull
docker compose up -d --build
```

Sadece frontend değiştiyse daha hızlısı:

```bash
docker compose up -d --build frontend
```

Bir sorun çıkarsa logdan bak:

```bash
docker compose logs -f backend
docker compose logs --tail 50 frontend
```

---

## Özet Kontrol Listesi

Kodda hazır olanlar (Kasım 2026):

- [x] Blog yazma uçları korumalı, kayıt varsayılan kapalı (Bölüm 0.1, 0.6)
- [x] Veritabanı/pgAdmin portları dışarı kapalı, pgAdmin sadece `dev` profilinde (Bölüm 0.3)
- [x] `restart`, log sınırı, uploads volume'ü, Caddy (HTTPS) compose'da (Bölüm 0.5, 5.4)
- [x] `.env.example` git'te (Bölüm 0.4)

Senin yapacakların:

- [ ] Kodu commit'leyip GitHub'a push'ladım (sunucu oradan çeker)
- [ ] Yazıları sahiplendim ya da sildim (Bölüm 0.1)
- [ ] Sunucuyu kiraladım, güvenlik duvarını kurdum, SSH şifre girişini kapattım (Bölüm 2.1–2.3)
- [ ] Alan adını aldım, DNS kayıtlarını sunucuya yönlendirdim (Bölüm 5.1–5.2)
- [ ] Sunucuda `.env`'i doldurdum: DB şifresi, Gmail uygulama şifresi, `DOMAIN`, `COMPOSE_PROFILES=prod`, `COOKIE_SECURE=true` (Bölüm 2.5)
- [ ] Yazılarımı sunucuya taşıdım ve sitede gördüm (Bölüm 2.7)
- [ ] `https://` adresim açılıyor, iletişim formundan kendime mesaj attım ve geldi
- [ ] Veritabanı **ve** görseller için otomatik yedek kurdum, bir kere geri yüklemeyi denedim (Bölüm 6)

Son madde önemli: yedeğin geri yüklenebildiğini **denemeden** yedeğin var sayılmaz.