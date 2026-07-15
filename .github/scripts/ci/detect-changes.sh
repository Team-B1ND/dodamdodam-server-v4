#!/usr/bin/env bash
set -euo pipefail

: "${BASE_SHA:?BASE_SHA is required}"
: "${HEAD_SHA:?HEAD_SHA is required}"
: "${GITHUB_OUTPUT:?GITHUB_OUTPUT is required}"

all_services=(gateway auth user wakeup-song inapp file neis out-sleeping nightstudy oauth notification)
changed_files=$(git diff --name-only "$BASE_SHA" "$HEAD_SHA")
services=()
shared_changed=false

if printf '%s\n' "$changed_files" | grep -Eq '^(core/|app/|utils/|buildSrc/|gradle/|build\.gradle(\.kts)?$|settings\.gradle(\.kts)?$|gradle\.properties$|builds/develop/Dockerfile$)'; then
  shared_changed=true
fi

if [ "$shared_changed" = true ]; then
  services=("${all_services[@]}")
else
  for service in "${all_services[@]}"; do
    service_changed=false

    if printf '%s\n' "$changed_files" | grep -q "^services/service-${service}/"; then
      service_changed=true
    fi

    if printf '%s\n' "$changed_files" | grep -q "^builds/develop/Dockerfile\.${service}$"; then
      service_changed=true
    fi

    if [ "$service_changed" = true ]; then
      services+=("$service")
    fi
  done
fi

services_json=$(printf '%s\n' "${services[@]}" | jq -Rsc 'split("\n") | map(select(length > 0))')
has_services=false
if [ "${#services[@]}" -gt 0 ]; then
  has_services=true
fi

echo "services=$services_json" >> "$GITHUB_OUTPUT"
echo "has_services=$has_services" >> "$GITHUB_OUTPUT"
echo "Changed services: ${services[*]:-none}"
