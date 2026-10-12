# Siteyi Canlıya Çıkarma Rehberi

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

### 0.2 Veritabanı şifresini değiştir

Şu an `docker-compose.yml` içinde açık açık yazıyor: `devuser` / `devpass`.
Bu şifreler internette dolaşan tarama botlarının ilk denediği şifrelerdendir.

Uzun ve rastgele bir şifre üret:

```bash
openssl rand -base64 24
```

Çıkan değeri sunucudaki `.env` dosyana yaz, `docker-compose.yml` içindeki sabit
`devpass` yazılarını `${POSTGRES_PASSWORD}` gibi değişkenlerle değiştir.

> ⚠️ PostgreSQL imajı `POSTGRES_PASSWORD`'ü **sadece veritabanı ilk kez kurulurken**
> (volume boşken) kullanır. Volume zaten doluysa `.env`'deki şifreyi değiştirmek bir şey
> yapmaz; şifreyi veritabanının içinden değiştirmen gerekir:
> ```bash
> docker compose exec postgres psql -U devuser -d personal_site -c "alter user devuser with password 'YENI_SIFRE';"
> ```
> Yeni sunucuda boş volume'le başlıyorsan (Bölüm 2.7'deki taşıma yolu) bu sorun çıkmaz.

### 0.3 Veritabanı ve pgAdmin portlarını dışarı açma

Şu anki `docker-compose.yml` PostgreSQL'i `5433`, pgAdmin'i `5051` portundan
**dışarıya** açıyor:

```yaml
ports:
  - "5433:5432"     # ← internete açık, tehlikeli
  - "5051:80"       # ← pgAdmin şifresi admin/admin, çok tehlikeli
```

Sunucuda bunları ya tamamen kaldır, ya da sadece sunucunun kendisine aç:

```yaml
ports:
  - "127.0.0.1:5433:5432"   # sadece sunucunun içinden erişilir
```

`127.0.0.1:` öneki "bu portu sadece bu makineden görülebilir yap" demektir. Dışarıdan
erişmen gerekirse SSH tüneli kullanırsın (Bölüm 6.3).

pgAdmin'i canlıda hiç çalıştırmamak en temizi. Gerekirse `admin@admin.com` / `admin`
bilgilerini mutlaka değiştir.

### 0.4 Mail şifresini .env'de tut

Bu kısım hazır: kök dizindeki `.env` dosyası `.gitignore`'da, backend oradan okuyor.
Sunucuda da aynı `.env` dosyasını oluşturman yeterli (Bölüm 2.5).

> ⚠️ **`.env.example` şu an git'te değil.** Sunucuda `git clone` yaptığında gelmez ve
> Bölüm 2.5'teki `cp .env.example .env` komutu hata verir. İçindeki değerler şablon
> (gerçek şifre yok), commit'lemek güvenli:
> ```bash
> git add .env.example && git commit -m "chore: .env şablonunu ekle"
> ```
> Eski `backend/.env` dosyası silindi. İlk commit'le GitHub'a gitmişti; içindeki
> veritabanı/pgAdmin şifrelerini başka bir yerde kullanıyorsan değiştir.

### 0.5 Container'lar kendi kendine yeniden başlasın, loglar diski doldurmasın

Sunucu yeniden başladığında sitenin de otomatik kalkması ve Docker loglarının
sınırsız büyümemesi için her servise ekle:

```yaml
restart: unless-stopped
logging:
  options:
    max-size: "10m"
    max-file: "3"
```

### 0.6 Kayıt herkese açık: kapat ya da bilerek aç

Şu an siteye giren **herkes** hesap açıp yazı yayınlayabilir ve bu yazılar ana
sayfadaki vitrine/kutulara düşer. İnternete açıldığı gün spam botları bunu bulur.

Kişisel blog için önerim: kendi hesabını açtıktan sonra kaydı kapatmak. En basit yol,
`SecurityConfig.java`'da `/api/auth/register`'ı `permitAll` listesinden çıkarıp
`denyAll()` yapmak ve menüden "Yazmaya başla" bağlantılarını kaldırmak. Bu bir kod
değişikliği; istersen yaparım.

### 0.7 Canlı ortam ayarları

`docker-compose.yml`'de backend'e şunları da ver (Bölüm 2.6'daki örnekte var):

| Ortam değişkeni | Neden |
|---|---|
| `SPRING_JPA_SHOW_SQL: "false"` | Her SQL sorgusu loga basılmasın |
| `UPLOADS_DIR: /app/uploads` ve uploads volume'ü | Yüklenen kapak görselleri container yenilenince kaybolmasın |
| `SERVER_SERVLET_SESSION_COOKIE_SECURE: "true"` | **HTTPS kurulduktan sonra** (Bölüm 5.4): oturum çerezi sadece şifreli bağlantıda gitsin |

---

## Bölüm 1 — Seçenekler

### Karşılaştırma

| | **A) VPS kirala** | **B) Hazır platform** | **C) Karma** |
|---|---|---|---|
| Ne yaparsın | Bir Linux sunucu kiralar, şu an localde çalıştırdığın `docker compose up` komutunu orada çalıştırırsın | Railway/Render'a git reposunu bağlarsın, kendisi build eder | Frontend Vercel'de, backend+DB Railway'de |
| Aylık maliyet | ~4-6 EUR (Hetzner CX22) | 5-20 USD (ücretsiz katman DB için yetersiz) | ~5-10 USD |
| Kurulum süresi | 1-2 saat | 30-60 dakika | 1-2 saat |
| Kod değişikliği | **Yok** — mevcut yapı aynen çalışır | **Var** — nginx proxy yapısı bozulur, CORS ve API adresi ayarı gerekir | **Var** — B ile aynı |
| Veritabanı kontrolü | Tamamen sende, volume'de | Platformun yönetilen DB'si | Platformun yönetilen DB'si |
| Yedekleme | Sen kurarsın (Bölüm 6) | Çoğunlukla otomatik | Çoğunlukla otomatik |
| Sunucu bakımı | Sende (güncelleme, güvenlik) | Platformda | Platformda |
| Öğrenme değeri | Yüksek — Linux, Docker, nginx, SSL | Düşük | Orta |

### Hangisi sana uygun?

**Seçenek A (VPS) — çoğu durumda en mantıklısı.** Çünkü projen zaten tamamen
Docker'lanmış ve `docker-compose.yml` dosyan 4 servisi birlikte ayağa kaldırıyor.
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
| **Hetzner** | CX22 (2 vCPU, 4GB RAM) | ~4 EUR/ay | Fiyat/performans en iyisi, Almanya/Finlandiya |
| DigitalOcean | Basic Droplet (1GB) | 6 USD/ay | Dokümantasyonu çok iyi, yeni başlayan dostu |
| Vultr | Regular (1GB) | 5-6 USD/ay | İstanbul lokasyonu var, Türkiye'den hızlı |
| Contabo | VPS S | ~5 EUR/ay | Ucuz ama destek yavaş |

> Fiyatlar ve ürün adları değişebiliyor; almadan önce sağlayıcının sitesinden güncelini kontrol et.

Bu proje için **en az 2GB RAM** öneririm. Java (Spring Boot) + PostgreSQL + Nginx
birlikte 1GB'a sığar ama build sırasında Maven zorlanır. Hetzner CX22 (4GB) rahat eder.

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
nano .env
```

İçini doldur:

```
MAIL_USERNAME=aliimranatabey@gmail.com
MAIL_PASSWORD=16haneliuygulamasifresi
CONTACT_RECIPIENT=aliimranatabey@gmail.com
POSTGRES_PASSWORD=<openssl rand -base64 24 çıktısı>
```

Kaydet: `Ctrl+O`, `Enter`, `Ctrl+X`.

### 2.6 Üretim ayarlarını uygula

Bölüm 0'daki değişiklikleri `docker-compose.yml` üzerinde yap. Hedeflenen hali
şuna benzemeli:

```yaml
services:
  postgres:
    image: postgres:16
    container_name: personal_website_db
    restart: unless-stopped
    logging:
      options: { max-size: "10m", max-file: "3" }
    environment:
      POSTGRES_DB: personal_site
      POSTGRES_USER: devuser
      POSTGRES_PASSWORD: ${POSTGRES_PASSWORD}
    ports:
      - "127.0.0.1:5433:5432"      # sadece sunucu içinden
    volumes:
      - personal_website_data:/var/lib/postgresql/data

  # pgadmin servisini canlıda yorum satırı yap veya sil

  backend:
    build: ./backend
    container_name: personal_website_backend
    restart: unless-stopped
    logging:
      options: { max-size: "10m", max-file: "3" }
    depends_on:
      - postgres
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/personal_site
      SPRING_DATASOURCE_USERNAME: devuser
      SPRING_DATASOURCE_PASSWORD: ${POSTGRES_PASSWORD}
      SPRING_JPA_HIBERNATE_DDL_AUTO: validate
      SPRING_JPA_SHOW_SQL: "false"
      SPRING_FLYWAY_ENABLED: "true"
      MAIL_USERNAME: ${MAIL_USERNAME:-}
      MAIL_PASSWORD: ${MAIL_PASSWORD:-}
      CONTACT_RECIPIENT: ${CONTACT_RECIPIENT:-aliimranatabey@gmail.com}
      UPLOADS_DIR: /app/uploads
      # HTTPS kurulduktan sonra (Bölüm 5.4) aç:
      # SERVER_SERVLET_SESSION_COOKIE_SECURE: "true"
    volumes:
      - personal_website_uploads:/app/uploads   # kapak görselleri; silinirse görseller gider
    # ports satırını sil — backend'e sadece frontend'in nginx'i erişecek

  frontend:
    build: ./frontend
    container_name: personal_website_frontend
    restart: unless-stopped
    logging:
      options: { max-size: "10m", max-file: "3" }
    depends_on:
      - backend
    ports:
      - "80:80"                     # 8090 değil, artık gerçek web portu

volumes:
  personal_website_data:
  personal_website_uploads:
```

> Eski rehberdeki örnekte uploads volume'ü ve `UPLOADS_DIR` yoktu. O haliyle
> yüklediğin kapak görselleri container her yenilendiğinde silinirdi.

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

```bash
docker compose up -d --build
```

İlk build 5-10 dakika sürebilir (Maven bağımlılıkları + npm paketleri iner).

Kontrol et:

```bash
docker compose ps          # hepsi "Up" olmalı
docker compose logs -f     # hata var mı bak, çıkmak için Ctrl+C
```

Tarayıcıdan `http://91.99.12.34` adresini aç. Siten yayında.

### 2.9 Sonrası

Bu noktada site çalışıyor ama:
- Adres çirkin bir IP → **Bölüm 5**'e geç, alan adı al
- Bağlantı `http://`, şifreli değil → **Bölüm 5.4**'te HTTPS kuracaksın
- Yedek yok → **Bölüm 6** şart

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

Ayrıca `config/WebConfig.java`'daki sabit `localhost:5173` CORS tanımını sil; ikinci
bir CORS kaynağı olarak kafa karıştırıyor.

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

### 5.4 HTTPS kur (Seçenek A için)

Alan adın çalışınca sitenin `https://` olması gerekir. Tarayıcılar `http://`
sitelerde "Güvenli değil" uyarısı gösterir, üstelik iletişim formundan gelen
mesajlar şifresiz gider.

En kolay yol **Caddy** — sertifikayı Let's Encrypt'ten kendisi alır, süresi dolunca
kendisi yeniler, hiç uğraştırmaz.

Sunucuda `Caddyfile` adında bir dosya oluştur:

```
aliimranatabey.com, www.aliimranatabey.com {
    reverse_proxy frontend:80
}
```

`docker-compose.yml` dosyasına Caddy servisini ekle:

```yaml
  caddy:
    image: caddy:2
    container_name: personal_website_caddy
    restart: unless-stopped
    depends_on:
      - frontend
    ports:
      - "80:80"
      - "443:443"
    volumes:
      - ./Caddyfile:/etc/caddy/Caddyfile
      - caddy_data:/data
      - caddy_config:/config
```

Ve `frontend` servisinden `ports` satırlarını **kaldır** (artık dışarıya Caddy
bakıyor, frontend'e sadece Caddy erişecek). `volumes:` bölümünün altına da ekle:

```yaml
volumes:
  personal_website_data:
  caddy_data:
  caddy_config:
```

Sonra:

```bash
docker compose up -d
docker compose logs -f caddy
```

Loglarda sertifika alındığını görürsün. `https://aliimranatabey.com` artık yeşil
kilitle açılır. `http://` adresini de otomatik `https://`'e yönlendirir.

> `caddy_data` volume'ünü silme — sertifikaların orada durur. Silersen Let's Encrypt'in
> haftalık limitine takılabilirsin.

HTTPS çalıştıktan sonra iki son ayar:

1. `docker-compose.yml`'de backend'in altındaki
   `SERVER_SERVLET_SESSION_COOKIE_SECURE: "true"` satırının yorumunu kaldır ve
   `docker compose up -d backend` çalıştır. Oturum çerezi artık sadece şifreli
   bağlantıda gider.
2. Hız sınırı (`RateLimitFilter`) ziyaretçinin IP'sini `X-Forwarded-For` header'ından
   okuyor. Caddy bu header'ı ziyaretçinin gerçek IP'siyle yazar, yani Caddy önündeyken
   doğru çalışır. Caddy'siz, sadece nginx ile `http://` yayında kalırsan ziyaretçi bu
   header'ı kendisi gönderip sınırı atlatabilir. Bu da HTTPS'i ertelememek için bir
   sebep daha.

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

Yayına çıkmadan önce:

- [x] Blog yazma uçları korumalı (Bölüm 0.1) — üyelik sistemi eklendi
- [ ] Kayıt olup eski yazıları sahiplendim (Bölüm 0.1)
- [ ] Kaydı kapattım ya da açık bırakmaya bilerek karar verdim (Bölüm 0.6)
- [ ] Veritabanı şifresini değiştirdim (Bölüm 0.2)
- [ ] postgres/pgadmin portlarını dışarı kapattım (Bölüm 0.3)
- [ ] `.env.example`'ı commit'leyip push'ladım (Bölüm 0.4)
- [ ] Sunucuda `.env` dosyasını oluşturdum, Gmail uygulama şifresini girdim (Bölüm 0.4)
- [ ] `restart` ve log sınırı ekledim (Bölüm 0.5)
- [ ] Uploads volume'ü ve `SPRING_JPA_SHOW_SQL=false` compose'da (Bölüm 0.7 / 2.6)
- [ ] Güvenlik duvarını kurdum, SSH şifre girişini kapattım (Bölüm 2.2–2.3)
- [ ] Yazılarımı sunucuya taşıdım ve sitede gördüm (Bölüm 2.7)
- [ ] Alan adını bağladım (Bölüm 5.2)
- [ ] HTTPS çalışıyor, oturum çerezi `Secure` (Bölüm 5.4)
- [ ] Veritabanı **ve** görseller için otomatik yedek kurdum, bir kere geri yüklemeyi denedim (Bölüm 6)

Son madde önemli: yedeğin geri yüklenebildiğini **denemeden** yedeğin var sayılmaz.
