#!/usr/bin/env bash
# Sunucuyu siteye hazırlar. Yeni bir Ubuntu sunucuda, root olarak, BİR KERE çalıştırılır:
#   cd /root/mywebsite && bash deploy/kurulum.sh
# Tekrar çalıştırmak zararsızdır: yapılmış adımları atlar.
# Soru sormadan çalıştırmak için değerleri baştan ver:
#   DOMAIN=alanadin.com bash deploy/kurulum.sh
set -euo pipefail

cd "$(dirname "$0")/.."
PROJE="$(pwd)"

adim() { printf '\n\033[1;36m==> %s\033[0m\n' "$1"; }

[ "$(id -u)" -eq 0 ] || { echo "root olarak çalıştır (ssh root@...)"; exit 1; }

adim "1/7 Sistem güncelleniyor"
export DEBIAN_FRONTEND=noninteractive
apt-get update -q
apt-get upgrade -yq
apt-get install -yq git curl ufw unattended-upgrades

adim "2/7 Docker kuruluyor"
if command -v docker >/dev/null 2>&1; then
  echo "Docker zaten kurulu: $(docker --version)"
else
  curl -fsSL https://get.docker.com | sh
fi

adim "3/7 Swap (yedek bellek) açılıyor"
# Java derlemesi küçük sunucularda belleği aşabiliyor; swap bunu önler
if swapon --show | grep -q .; then
  echo "Swap zaten var"
elif fallocate -l 2G /swapfile && chmod 600 /swapfile && mkswap /swapfile && swapon /swapfile; then
  echo '/swapfile none swap sw 0 0' >> /etc/fstab
else
  rm -f /swapfile
  echo "UYARI: swap açılamadı (sunucu KVM değil olabilir). Derleme bellek yetmezse hata verebilir."
fi

adim "4/7 Güvenlik duvarı: sadece SSH, HTTP, HTTPS açık"
ufw allow OpenSSH
ufw allow 80/tcp
ufw allow 443/tcp
ufw allow 443/udp
ufw --force enable

adim "5/7 Otomatik güvenlik güncellemeleri açılıyor"
cat > /etc/apt/apt.conf.d/20auto-upgrades <<'EOF'
APT::Periodic::Update-Package-Lists "1";
APT::Periodic::Unattended-Upgrade "1";
EOF

adim "6/7 SSH'ta şifreyle giriş kapatılıyor"
if [ -s /root/.ssh/authorized_keys ]; then
  cat > /etc/ssh/sshd_config.d/99-mywebsite.conf <<'EOF'
PasswordAuthentication no
KbdInteractiveAuthentication no
PermitRootLogin prohibit-password
EOF
  systemctl restart ssh || systemctl restart sshd
  echo "Artık sadece SSH anahtarıyla girilebilir"
else
  echo "UYARI: SSH anahtarı bulunamadı, şifreli giriş açık bırakıldı"
fi

adim "7/7 .env dosyası ve günlük yedek"
if [ -f .env ]; then
  echo ".env zaten var, dokunulmadı"
else
  DOMAIN="${DOMAIN:-}"
  [ -n "$DOMAIN" ] || read -rp "Alan adın (örn. aliimranatabey.com, www olmadan): " DOMAIN
  cat > .env <<EOF
# deploy/kurulum.sh tarafından oluşturuldu
POSTGRES_PASSWORD=$(openssl rand -hex 24)
REGISTRATION_ENABLED=false
COMPOSE_PROFILES=prod
DOMAIN=$DOMAIN
COOKIE_SECURE=true
EOF
  chmod 600 .env
  echo ".env oluşturuldu (veritabanı şifresi rastgele üretildi)"
fi

mkdir -p /root/yedekler
cat > /etc/cron.d/mywebsite-yedek <<EOF
PATH=/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin
0 3 * * * root bash $PROJE/deploy/yedek.sh >> /var/log/mywebsite-yedek.log 2>&1
EOF
echo "Her gece 03:00'te yedek alınacak: /root/yedekler"

adim "Bitti"
echo "Sıradaki adım rehberde (deploy/YAYINLA.md)."
