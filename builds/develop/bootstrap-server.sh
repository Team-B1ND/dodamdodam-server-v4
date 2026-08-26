#!/usr/bin/env bash
set -euo pipefail

dockerhub_username=${1:?usage: bootstrap-server.sh DOCKERHUB_USERNAME [PUBLIC_BASE_URL]}
public_base_url=${2:-http://localhost}
deploy_dir=${DODAM_DEPLOY_DIR:-"${HOME}/dodamdodam"}
env_file="${deploy_dir}/.env"

if ! command -v docker >/dev/null 2>&1 || ! docker compose version >/dev/null 2>&1 || ! command -v openssl >/dev/null 2>&1 || ! command -v curl >/dev/null 2>&1; then
  sudo apt-get update
  sudo DEBIAN_FRONTEND=noninteractive apt-get install -y docker.io docker-compose-v2 curl openssl
  sudo systemctl enable --now docker
fi

mkdir -p "${deploy_dir}"
umask 077

if [ ! -f "${env_file}" ]; then
  key_dir=$(mktemp -d)
  trap 'rm -rf "${key_dir}"' EXIT

  openssl genpkey -algorithm RSA -pkeyopt rsa_keygen_bits:2048 -out "${key_dir}/auth-private.pem" 2>/dev/null
  openssl pkey -in "${key_dir}/auth-private.pem" -pubout -out "${key_dir}/auth-public.pem" 2>/dev/null
  openssl genpkey -algorithm RSA -pkeyopt rsa_keygen_bits:2048 -out "${key_dir}/oauth-private.pem" 2>/dev/null
  openssl pkey -in "${key_dir}/oauth-private.pem" -pubout -out "${key_dir}/oauth-public.pem" 2>/dev/null

  auth_private_key=$(sed '/^-----/d' "${key_dir}/auth-private.pem" | tr -d '\r\n')
  auth_public_key=$(sed '/^-----/d' "${key_dir}/auth-public.pem" | tr -d '\r\n')
  oauth_private_key=$(sed '/^-----/d' "${key_dir}/oauth-private.pem" | tr -d '\r\n')
  oauth_public_key=$(sed '/^-----/d' "${key_dir}/oauth-public.pem" | tr -d '\r\n')

  cat > "${env_file}" <<EOF
DOCKERHUB_USERNAME=${dockerhub_username}
MYSQL_ROOT_PASSWORD=$(openssl rand -hex 32)
MYSQL_USER=dodam
MYSQL_PASSWORD=$(openssl rand -hex 32)
JWT_PUBLIC_KEY=${auth_public_key}
JWT_PRIVATE_KEY=${auth_private_key}
JWT_ISSUER=${public_base_url}
JWT_ACCESS_SECONDS=3600
JWT_REFRESH_SECONDS=1209600
OAUTH_ISSUER=${public_base_url}
OAUTH_RSA_PRIVATE_KEY=${oauth_private_key}
OAUTH_RSA_PUBLIC_KEY=${oauth_public_key}
COOKIE_DOMAIN=
COOKIE_SECURE=false
COOKIE_SAME_SITE=Lax
EOF
  chmod 600 "${env_file}"
  echo "Created development runtime configuration at ${env_file}."
fi

upsert_env() {
  key=$1
  value=$2
  updated_env=$(mktemp)
  awk -v key="${key}" -v value="${value}" '
    BEGIN { found = 0 }
    index($0, key "=") == 1 {
      if (!found) print key "=" value
      found = 1
      next
    }
    { print }
    END { if (!found) print key "=" value }
  ' "${env_file}" > "${updated_env}"
  chmod 600 "${updated_env}"
  mv "${updated_env}" "${env_file}"
}

upsert_env DOCKERHUB_USERNAME "${dockerhub_username}"
upsert_env JWT_ISSUER "${public_base_url}"
upsert_env OAUTH_ISSUER "${public_base_url}"
echo "Updated deployment endpoints in ${env_file}; existing secrets were preserved."

sudo usermod -aG docker "${USER}" || true
echo "Server bootstrap complete. Runtime configuration: ${env_file}"
