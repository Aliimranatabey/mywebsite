#!/usr/bin/env bash
# Mac'ten gelen veritabanı dökümünü (tasima.sql) BOŞ sunucu veritabanına yükler,
# seed'den gelen iki deneme yazısını siler. Backend hiç açılmadan ÖNCE çalıştırılır:
#   bash deploy/yazilari-yukle.sh tasima.sql
set -euo pipefail
cd "$(dirname "$0")/.."

DOSYA="${1:?kullanım: bash deploy/yazilari-yukle.sh tasima.sql}"
[ -f "$DOSYA" ] || { echo "$DOSYA bulunamadı"; exit 1; }

docker compose up -d postgres
echo "Veritabanının açılması bekleniyor..."
until docker compose exec -T postgres pg_isready -U devuser -d personal_site >/dev/null 2>&1; do sleep 2; done

psql() { docker compose exec -T postgres psql -U devuser -d personal_site -v ON_ERROR_STOP=1 "$@"; }

if [ "$(psql -tAc "select to_regclass('public.blog_posts') is not null")" = "t" ]; then
  echo "Veritabanı boş değil (backend daha önce açılmış). Yükleme yapılmadı."
  echo "Sıfırdan başlamak için: docker compose down && docker volume rm mywebsite_personal_website_data"
  exit 1
fi

psql -q < "$DOSYA"
psql -c "delete from blog_posts where slug in ('vue3-ile-baslangic', 'spring-boot-giris');"
echo "Yüklendi. Yazı sayısı: $(psql -tAc 'select count(*) from blog_posts')"
