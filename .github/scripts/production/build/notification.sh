#!/usr/bin/env bash
set -euo pipefail

cd services/service-notification
npm ci
npm run build