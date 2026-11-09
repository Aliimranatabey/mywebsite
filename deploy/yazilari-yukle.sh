#!/usr/bin/env bash
# SUNUCUDA çalışır. Mac'ten gelen tasima.sql ve tasima-gorseller.tgz dosyalarını yükler,
# seed'den gelen iki deneme yazısını siler.
#   bash deploy/yazilari-yukle.sh             → ilk kurulumda (backend açılmadan önce)
#   bash deploy/yazilari-yukle.sh --sifirla   → site zaten açıksa: önce yedek alır,
#                                               sunucudaki yazıları silip Mac'tekilerle değiştirir
set -euo pipefail
cd "$(dirname "$0")/.."

for f in tasima.sql tasima-gorseller.tgz; do
  [ -f "$f" ] || { echo "$f bulunamadı. Mac'te: bash deploy/tasima-hazirla.sh SUNUCU_IP"; exit 1; }
done

psql() { docker compose exec -T postgres psql -U devuser -d personal_site -v ON_ERROR_STOP=1 "$@"; }

docker compose up -d postgres
echo "Veritabanının açılması bekleniyor..."
until docker compose exec -T postgres pg_isready -U devuser -d personal_site >/dev/null 2>&1; do sleep 2; done

if [ "$(psql -tAc "select to_regclass('public.blog_posts') is not null")" = "t" ]; then
  if [ "${1:-}" != "--sifirla" ]; then
    echo "Sunucuda zaten veri var. Üzerine yazmak için: bash deploy/yazilari-yukle.sh --sifirla"
    exit 1
  fi
  echo "Önce mevcut halin yedeği alınıyor..."
  bash deploy/yedek.sh
  docker compose stop backend
  psql -q -c "drop schema public cascade; create schema public;"
fi

psql -q < tasima.sql
psql -q -c "delete from blog_posts where slug in ('vue3-ile-baslangic', 'spring-boot-giris');"

# Görseller backend'in volume'üne, backend'in kendi kullanıcısıyla açılır
docker compose run --rm --no-deps -T --entrypoint tar backend xzf - -C /app/uploads < tasima-gorseller.tgz

[ "${1:-}" = "--sifirla" ] && docker compose up -d backend

echo "Yüklendi. Yazı: $(psql -tAc 'select count(*) from blog_posts')"
echo "Not: Mac'teki kullanıcı listesi de geldi; sitede hesabın yoksa deploy/kayit.sh ac ile kayıt ol."
