#!/usr/bin/env bash
# Kodda değişiklik yapıp GitHub'a push'ladıktan sonra sunucuda çalıştır:
#   bash deploy/guncelle.sh
set -euo pipefail
cd "$(dirname "$0")/.."

git pull --ff-only
docker compose up -d --build
docker image prune -f >/dev/null   # eski imajlar diski doldurmasın
docker compose ps
