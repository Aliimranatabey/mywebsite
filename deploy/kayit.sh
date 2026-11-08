#!/usr/bin/env bash
# Yeni üye kaydını açar ya da kapatır.
#   bash deploy/kayit.sh ac      → kendi hesabını açmadan önce
#   bash deploy/kayit.sh kapat   → hesabını açtıktan hemen sonra
set -euo pipefail
cd "$(dirname "$0")/.."

case "${1:-}" in
  ac) DEGER=true ;;
  kapat) DEGER=false ;;
  *) echo "kullanım: bash deploy/kayit.sh ac|kapat"; exit 1 ;;
esac

if grep -q '^REGISTRATION_ENABLED=' .env; then
  sed -i.bak "s/^REGISTRATION_ENABLED=.*/REGISTRATION_ENABLED=$DEGER/" .env && rm -f .env.bak
else
  echo "REGISTRATION_ENABLED=$DEGER" >> .env
fi

docker compose up -d backend
echo "Kayıt: $1"
