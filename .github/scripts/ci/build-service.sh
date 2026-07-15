#!/usr/bin/env bash
set -euo pipefail

: "${SERVICE:?SERVICE is required}"

case "$SERVICE" in
  gateway|auth|user|wakeup-song|inapp|file|neis|out-sleeping|nightstudy|oauth)
    chmod +x gradlew
    ./gradlew ":services:service-${SERVICE}:build" -x test --no-daemon
    ;;
  notification)
    cd services/service-notification
    npm ci
    npm run build
    ;;
  *)
    echo "Unsupported service: $SERVICE" >&2
    exit 1
    ;;
esac
