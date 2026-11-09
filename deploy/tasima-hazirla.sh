#!/usr/bin/env bash
# MAC'TE çalışır. Yazıları (veritabanı) ve görselleri sunucuya taşımak için paketler,
# IP verilirse sunucuya da gönderir:
#   bash deploy/tasima-hazirla.sh              → sadece dosyaları hazırlar
#   bash deploy/tasima-hazirla.sh 91.99.12.34  → hazırlar ve sunucuya yollar
set -euo pipefail
cd "$(dirname "$0")/.."

docker compose up -d postgres >/dev/null
until docker compose exec -T postgres pg_isready -U devuser -d personal_site >/dev/null 2>&1; do sleep 1; done

docker compose exec -T postgres pg_dump -U devuser personal_site > tasima.sql
docker compose run --rm --no-deps -T --entrypoint tar backend czf - -C /app/uploads . > tasima-gorseller.tgz

echo "Hazır: tasima.sql ($(du -h tasima.sql | cut -f1)), tasima-gorseller.tgz ($(du -h tasima-gorseller.tgz | cut -f1))"

if [ -n "${1:-}" ]; then
  scp tasima.sql tasima-gorseller.tgz "root@$1:/root/mywebsite/"
  echo "Sunucuya gönderildi. Sunucuda: bash deploy/yazilari-yukle.sh"
fi
