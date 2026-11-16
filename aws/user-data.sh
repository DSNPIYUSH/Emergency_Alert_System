#!/bin/bash
# ==============================================================================
# AWS EC2 UserData Provisioning Script
# OS: Amazon Linux 2023 / RHEL 9 / Ubuntu 22.04 LTS
# Provisions Java 21, Docker, fetches app, and launches via systemd
# ==============================================================================

set -euo pipefail

echo "==> Starting Emergency Alert System automated host deployment..."

# 1. Update OS packages & install Docker + Git
if command -v dnf &>/dev/null; then
    dnf update -y
    dnf install -y docker git
    systemctl enable --now docker
elif command -v apt-get &>/dev/null; then
    apt-get update -y
    apt-get install -y docker.io docker-compose git
    systemctl enable --now docker
fi

# Allow ec2-user/ubuntu to use docker
usermod -aG docker ec2-user || true
usermod -aG docker ubuntu || true

# 2. Setup project application directory
APP_DIR="/opt/emergency-alert"
mkdir -p "$APP_DIR"
cd "$APP_DIR"

# 3. Write environment configuration (In production, populate via AWS SSM Parameter Store or Secrets Manager)
cat << 'EOF' > "$APP_DIR/.env"
SPRING_PROFILES_ACTIVE=prod
SERVER_PORT=8080
DB_HOST=REPLACE_WITH_YOUR_RDS_ENDPOINT
DB_PORT=3306
DB_NAME=emergency_db
DB_USERNAME=emergency_user
DB_PASSWORD=REPLACE_WITH_YOUR_RDS_PASSWORD
JWT_SECRET=ReplaceWithYourProductionSecretAtLeast256BitsLengthForSecureHMAC256!
JWT_EXPIRATION_MS=86400000
EOF

chmod 600 "$APP_DIR/.env"

# 4. Create systemd unit service file for automated restart
cat << 'EOF' > /etc/systemd/system/emergency-alert.service
[Unit]
Description=Emergency Alert System Container Service
After=docker.service network-online.target
Requires=docker.service

[Service]
Type=simple
WorkingDirectory=/opt/emergency-alert
Restart=always
RestartSec=10
ExecStartPre=-/usr/bin/docker stop emergency-alert-app
ExecStartPre=-/usr/bin/docker rm emergency-alert-app
ExecStart=/usr/bin/docker run --name emergency-alert-app \
  --env-file /opt/emergency-alert/.env \
  -p 8080:8080 \
  emergency-alert-system:latest
ExecStop=/usr/bin/docker stop emergency-alert-app

[Install]
WantedBy=multi-user.target
EOF

systemctl daemon-reload
echo "==> Host deployment preparation complete. Build image and run: systemctl start emergency-alert"
