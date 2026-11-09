# Siteyi Yayınla — Adım Adım

Site ziyaretçiden kişisel veri toplamaz: form, çerez, analitik, dış font ya da dış görsel yok.
Bu yüzden KVKK yükümlülüğü (aydınlatma, yurt dışı aktarım, VERBİS) doğmaz.
Sana kalan iş: **sunucu kiralamak + alan adı almak**. Gerisini scriptler yapar.

## Maliyet (Kasım 2026)
| Kalem | Fiyat | TL (1 € ≈ 55,5 TL) |
|---|---|---|
| Hetzner CX23 sunucu (2 çekirdek, 4 GB, IPv4 dahil) | 5,99 €/ay | ~330 TL/ay |
| Cloudflare `.com` alan adı | ~10,5 $/yıl | ~500 TL/yıl |
| **İlk ay toplam** | | **~830 TL** |
| **Yıllık toplam** | | **~4.500 TL** (ayda ortalama ~375 TL) |

- Türkiye'den bireysel müşteriye Hetzner KDV eklemiyor; bankan yurt dışı işlem ücreti alabilir.
- Hetzner saatlik faturalar: sunucuyu silersen o andan sonrası için ödeme yapmazsın.

---

## ✅ 1. Kod GitHub'da
## ✅ 2. SSH anahtarı hazır (`~/.ssh/id_ed25519.pub`)

## 3. Sunucu kirala · hetzner.com · 10 dk
1. Hesap aç (kimlik doğrulaması isteyebilir), kartını ekle.
2. **Cloud → New Server** ekranında:
   - Location: **Nuremberg** / **Falkenstein** / **Helsinki** (hangisinde stok varsa)
   - Image: **Ubuntu 24.04**
   - Type: **Shared vCPU → x86 → CX23**
     - Stok yoksa **Arm64 → CAX11**; site ARM'de de çalışır.
   - Networking: **IPv4 açık** kalsın
   - SSH Keys: aşağıdaki satırı yapıştır
     ```
     ssh-ed25519 AAAAC3NzaC1lZDI1NTE5AAAAIB5H8Btrj4GTrOpQnCofk7eK/vvVNzWhhf+GIr5Ih1rv ali-mac
     ```
3. **Create** → **IP adresini** not et.
- **Neden Hetzner:** Avrupa'da fiyat/performans lideri. Girişimler ve bağımsız geliştiriciler yaygın kullanır.

## 4. Alan adı al · cloudflare.com · 10 dk
1. Hesap aç → **Domain Registration → Register Domains** → alan adını al.
2. **DNS → Records** altına iki kayıt ekle:

| Type | Name | Content | Proxy status |
|---|---|---|---|
| A | `@` | sunucu IP'si | **DNS only** (gri bulut) |
| A | `www` | sunucu IP'si | **DNS only** (gri bulut) |

- **Neden Cloudflare:** Maliyetine satar, yenilemede zam yapmaz, DNS ve WHOIS gizliliği ücretsiz.
- **Neden gri bulut:** Ziyaretçi trafiği Cloudflare'den geçmez. HTTPS sertifikasını sunucudaki Caddy alır.

**→ Buraya kadar sen yap. IP adresini ve alan adını Claude'a ver, 5-8 arası adımları o da yapabilir.**

---

## 5. Sunucuyu kur · 10 dk
```bash
ssh root@SUNUCU_IP
git clone https://github.com/Aliimranatabey/mywebsite.git && cd mywebsite
DOMAIN=alanadin.com bash deploy/kurulum.sh
```
Script şunları yapar: Docker, güvenlik duvarı, swap, otomatik güvenlik güncellemesi, SSH'ta şifre girişini kapatma, `.env` (rastgele veritabanı şifresi), gece 03:00 yedeği.

## 6. Yazıları ve görselleri taşı · 5 dk
```bash
# Mac'te (proje klasöründe)
bash deploy/tasima-hazirla.sh SUNUCU_IP
# Sunucuda
bash deploy/yazilari-yukle.sh
```
- 9 yazı ve 47 görsel gider; seed'den gelen 2 deneme yazısı silinir.
- Site açıldıktan sonra taşırsan: `bash deploy/yazilari-yukle.sh --sifirla` (önce otomatik yedek alır).

## 7. Siteyi aç · 10 dk
```bash
dig +short alanadin.com           # sunucu IP'sini yazmalı
docker compose up -d --build      # ilk derleme 5-10 dk
docker compose logs -f caddy      # "certificate obtained" → Ctrl+C
```
`https://alanadin.com` adresini aç. 🎉

## 8. Hesabını aç, yazıları sahiplen · 3 dk
```bash
bash deploy/kayit.sh ac
# sitede /register sayfasından kayıt ol
bash deploy/sahiplen.sh senin@epostan.com
bash deploy/kayit.sh kapat
```

## 9. Bir kere kontrol et
- `bash deploy/yedek.sh` → `ls /root/yedekler`
- Ayda bir yedeği Mac'e çek: `scp root@SUNUCU_IP:/root/yedekler/db_* ~/Desktop/`
- [Search Console](https://search.google.com/search-console)'a alan adını ekle → sitemap: `sitemap.xml`

## Kod değişince
```bash
git push                                   # Mac
ssh root@SUNUCU_IP 'cd mywebsite && bash deploy/guncelle.sh'
```

## KVKK'yı korumak için eklememen gerekenler
Google Analytics · YouTube/Twitter gömme · Google Fonts · dış sitelerden görsel · ziyaretçiden veri alan form.
Bunlardan biri eklenirse aydınlatma metni ve yurt dışı aktarım yükümlülükleri geri gelir.

## Sorun çıkarsa
| Belirti | Bak |
|---|---|
| Site açılmıyor | `docker compose ps` |
| HTTPS hatası | `docker compose logs caddy` (DNS doğru mu, bulut gri mi?) |
| Sayfa var, veri yok | `docker compose logs backend` |
