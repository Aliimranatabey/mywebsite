# Siteyi Yayınla — Adım Adım

Toplam süre ~1 saat. Maliyet: sunucu ~4-6 €/ay + alan adı ~10 $/yıl.
Her adımda: **Ne** yapıyoruz · **Neden** · **Sektörde** kim kullanıyor.

---

## 1. Kodu GitHub'a gönder · Mac · 2 dk
```bash
cd ~/vscode-workspace/mywebsite
git add -A && git commit -m "feat: canlıya çıkış hazırlığı" && git push
```
- **Neden:** Sunucu kodu GitHub'dan çeker. Push edilmeyen değişiklik sunucuya gitmez.
- **Sektörde:** Her yazılım şirketi. Canlıdaki kodun tek kaynağı git'tir.

## 2. SSH anahtarı oluştur · Mac · 1 dk
```bash
ssh-keygen -t ed25519          # Enter, Enter, Enter
cat ~/.ssh/id_ed25519.pub      # çıkan satırı kopyala
```
- **Neden:** Sunucuya şifre yerine anahtarla gireceksin. Botlar şifreyi deneyerek kırabilir, anahtarı kıramaz.
- **Sektörde:** Standart. Şirketlerde sunucuya şifreyle giriş yasaktır.

## 3. Sunucu kirala · hetzner.com · 10 dk
1. Hesap aç (kimlik doğrulaması isteyebilir).
2. **Cloud → New Server** ekranında:
   - Location: **Nuremberg** ya da **Helsinki**
   - Image: **Ubuntu 24.04**
   - Type: **Shared vCPU → en ucuz x86 paket** (CX23). Site ~250 MB RAM kullanıyor, en küçüğü yeter.
   - Networking: **IPv4 açık** kalsın (+0,50 €). Türkiye'de birçok internet IPv6'yı düzgün desteklemiyor.
   - SSH Keys: 2. adımda kopyaladığın satırı yapıştır.
3. **Create**, sonra çıkan **IP adresini** not et.
- **Neden:** Siten 7/24 açık duracak bir bilgisayara ihtiyaç duyuyor.
- **Sektörde:** Buna VPS denir. Girişimler ve bağımsız geliştiriciler Hetzner'i ve DigitalOcean'ı, büyük şirketler AWS ve Azure'u kullanır.
- Panelde fiyat 6 €'nun üstündeyse alternatif: **netcup.com → VPS 500**.

## 4. Alan adı al ve sunucuya yönlendir · cloudflare.com · 10 dk
1. Hesap aç, **Domain Registration → Register** ile alan adını al (örn. `aliimranatabey.com`).
2. **DNS → Records** altına iki kayıt ekle:

| Type | Name | Content | Proxy |
|---|---|---|---|
| A | `@` | sunucu IP'si | **DNS only** (gri bulut) |
| A | `www` | sunucu IP'si | **DNS only** (gri bulut) |

- **Neden:** İnsanlar IP adresi değil isim yazar. DNS, ismi IP'ye çevirir.
- **Neden gri bulut:** HTTPS sertifikasını sunucudaki Caddy alacak. Turuncu bulut buna engel olur.
- **Sektörde:** Cloudflare, internetteki sitelerin yaklaşık %20'sinin DNS'ini yönetir. Alan adını maliyetine satar, yenilemede zam yapmaz.

## 5. Sunucuyu kur · Sunucu · 10 dk
```bash
ssh root@SUNUCU_IP
git clone https://github.com/Aliimranatabey/mywebsite.git
cd mywebsite
bash deploy/kurulum.sh
```
Script senden alan adını, Gmail adresini ve Gmail uygulama şifresini ister.
Uygulama şifresi şöyle alınır: myaccount.google.com → Güvenlik → 2 Adımlı Doğrulama'yı aç → aramaya "Uygulama şifreleri" yaz → oluştur.

Script ne yapıyor:
- Sistemi günceller, Docker'ı kurar
- 2 GB swap açar: derleme sırasında bellek yetmezse diye
- Güvenlik duvarı kurar: sadece 22, 80 ve 443 portları açık kalır
- Güvenlik güncellemelerini otomatik yapar
- SSH'ta şifreli girişi kapatır
- `.env` dosyasını oluşturur; veritabanı şifresi rastgele üretilir
- Her gece 03:00'te yedek alacak şekilde ayarlar

- **Sektörde:** Buna "provisioning" denir. Şirketler Ansible, Terraform ya da cloud-init kullanır; bu script onların mini hali.

## 6. Yazılarını taşı · Mac, sonra Sunucu · 5 dk
**Mac'te** (yeni bir terminal sekmesinde):
```bash
cd ~/vscode-workspace/mywebsite
docker compose up -d postgres
docker compose exec -T postgres pg_dump -U devuser personal_site > tasima.sql
scp tasima.sql root@SUNUCU_IP:/root/mywebsite/
```
**Sunucuda:**
```bash
bash deploy/yazilari-yukle.sh tasima.sql     # "Yazı sayısı: 9" demeli
```
- **Neden:** 9 yazın sadece Mac'indeki veritabanında duruyor. Script ayrıca seed'den gelen 2 deneme yazısını siler.
- **Sektörde:** `pg_dump` PostgreSQL'in standart taşıma ve yedek aracıdır.

## 7. Siteyi aç · Sunucu · 10 dk
```bash
dig +short ALANADIN.com        # sunucu IP'sini yazmalı; yazmıyorsa birkaç dakika bekle
docker compose up -d --build   # ilk derleme 5-10 dk sürer
docker compose logs -f caddy   # "certificate obtained" görünce Ctrl+C
```
Tarayıcıda `https://ALANADIN.com` adresini aç. 🎉
- **Neden:** Docker; veritabanını, backend'i, frontend'i ve Caddy'yi tek komutla, Mac'te denediğin halleriyle ayağa kaldırır.
- **HTTPS:** Caddy, Let's Encrypt'ten ücretsiz sertifika alır ve kendisi yeniler.
- **Sektörde:** Docker hemen her şirkette var; büyükler üstüne Kubernetes kurar. İnternetteki sitelerin yarısından fazlası Let's Encrypt sertifikası kullanır.

## 8. Hesabını aç, yazıları sahiplen · Sunucu + tarayıcı · 3 dk
```bash
bash deploy/kayit.sh ac
```
Sitede **/register** sayfasına git ve kayıt ol. Sonra:
```bash
bash deploy/sahiplen.sh senin@epostan.com    # "9 yazı sana bağlandı"
bash deploy/kayit.sh kapat
```
- **Neden:** Yazıları düzenleyebilmek için senin hesabına bağlı olmaları gerekiyor. Kaydı kapatınca başka kimse hesap açıp yazı yayınlayamaz.

## 9. Son kontroller · 5 dk
- İletişim formundan kendine mesaj at, mail geliyor mu bak.
- `bash deploy/yedek.sh` çalıştır, ardından `ls /root/yedekler` ile dosyaların oluştuğunu gör.
- Ayda bir yedeği Mac'ine çek: `scp root@SUNUCU_IP:/root/yedekler/db_* ~/Desktop/`
  - **Sektörde:** "3-2-1 kuralı": verinin kopyası farklı yerlerde durmalı. Sunucu yanarsa yedek de yanmasın.
- [search.google.com/search-console](https://search.google.com/search-console) adresine alan adını ekle, sitemap olarak `sitemap.xml` ver.
  - **Neden:** Google siteni daha hızlı bulur.

---

## Sonra: kodu değiştirince
```bash
git push                          # Mac'te
ssh root@SUNUCU_IP
cd mywebsite && bash deploy/guncelle.sh
```
- **Sektörde:** Şirketler bunu GitHub Actions ile otomatik yapar (CI/CD). İstersen ileride kurarız.

## Sorun çıkarsa
| Belirti | Bak |
|---|---|
| Site açılmıyor | `docker compose ps` (hepsi Up mı?) |
| HTTPS hatası | `docker compose logs caddy` (DNS doğru mu, bulut gri mi?) |
| Sayfa var, veri yok | `docker compose logs backend` |
| Mail gelmiyor | `.env`'deki `MAIL_PASSWORD` uygulama şifresi mi? |
