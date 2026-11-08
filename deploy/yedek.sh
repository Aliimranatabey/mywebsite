#!/usr/bin/env bash
# Veritabanının ve kapak görsellerinin yedeğini /root/yedekler'e alır, 14 günden eskiyi siler.
# kurulum.sh bunu her gece 03:00'te çalıştıracak şekilde ayarlar. Elle de çalışır:
#   bash deploy/yedek.sh
set -euo pipefail
cd "$(dirname "$0")/.."

HEDEF="${YEDEK_DIZINI:-/root/yedekler}"
TARIH=$(date +%F)
mkdir -p "$HEDEF"

docker compose exec -T postgres pg_dump -U devuser personal_site | gzip > "$HEDEF/db_$TARIH.sql.gz"
docker compose exec -T backend tar czf - -C /app/uploads . > "$HEDEF/uploads_$TARIH.tgz"

find "$HEDEF" -name 'db_*.sql.gz' -mtime +14 -delete
find "$HEDEF" -name 'uploads_*.tgz' -mtime +14 -delete

echo "$(date '+%F %T') yedek tamam: $(du -sh "$HEDEF" | cut -f1) toplam"
