#!/usr/bin/env bash
set -euo pipefail

: "${GITHUB_REF:?GITHUB_REF is required}"
: "${GITHUB_OUTPUT:?GITHUB_OUTPUT is required}"

tag="${GITHUB_REF#refs/tags/}"
previous_tag=$(git tag --sort=-creatordate | grep -v "^${tag}$" | head -1 || true)
if [ -n "$previous_tag" ]; then
  changed=$(git diff --name-only "$previous_tag" "$tag")
else
  changed=$(git ls-tree -r --name-only HEAD)
fi

all_services=(gateway auth user wakeup-song inapp file neis out-sleeping nightstudy oauth notification)
services=()

core_changed=false
if printf '%s\n' "$changed" | grep -Eq '^(core/|app/|utils/|buildSrc/|gradle/|build\.gradle|settings\.gradle|gradle\.properties)'; then
  core_changed=true
fi

if [ "$core_changed" = true ]; then
  services=("${all_services[@]}")
  echo "Core or shared build code changed; deploying all services."
else
  for service in "${all_services[@]}"; do
    if printf '%s\n' "$changed" | grep -q "^services/service-${service}/"; then
      services+=("$service")
    fi
  done
fi

services_json=$(printf '%s\n' "${services[@]}" | jq -Rsc 'split("\n") | map(select(length > 0))')
echo "services=$services_json" >> "$GITHUB_OUTPUT"
echo "has_services=$([ ${#services[@]} -gt 0 ] && echo true || echo false)" >> "$GITHUB_OUTPUT"
echo "tag=$tag" >> "$GITHUB_OUTPUT"
echo "Changed services: ${services[*]:-none}"
