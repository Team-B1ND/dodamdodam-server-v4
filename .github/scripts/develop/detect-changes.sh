#!/usr/bin/env bash
set -euo pipefail

: "${HEAD_SHA:?HEAD_SHA is required}"
: "${GITHUB_OUTPUT:?GITHUB_OUTPUT is required}"

all_services=(gateway auth user wakeup-song inapp file neis out-sleeping nightstudy oauth notification)
base_sha=${BASE_SHA:-}
force_all=${FORCE_ALL:-false}

if [ "$force_all" = true ] || [ -z "$base_sha" ] || [[ "$base_sha" =~ ^0+$ ]]; then
  changed_files=$(git ls-tree -r --name-only "$HEAD_SHA")
  build_all=true
else
  changed_files=$(git diff --name-only "$base_sha" "$HEAD_SHA")
  build_all=false
fi

if printf '%s\n' "$changed_files" | grep -Eq '^(.github/workflows/cd-develop.yml|.github/scripts/develop/|builds/develop/Dockerfile(\..*)?)$'; then
  build_all=true
fi

services=()
if [ "$build_all" = true ] || printf '%s\n' "$changed_files" | grep -Eq '^(core/|app/|utils/|buildSrc/|gradle/|build\.gradle(\.kts)?$|settings\.gradle(\.kts)?$|gradle\.properties$)'; then
  services=("${all_services[@]}")
else
  for service in "${all_services[@]}"; do
    if printf '%s\n' "$changed_files" | grep -q "^services/service-${service}/"; then
      services+=("$service")
    fi
  done
fi

services_json='[]'
has_services=false
if [ "${#services[@]}" -gt 0 ]; then
  services_json=$(printf '%s\n' "${services[@]}" | jq -Rsc 'split("\n") | map(select(length > 0))')
  has_services=true
fi

spring_services=()
has_notification=false
for service in "${services[@]}"; do
  if [ "${service}" = notification ]; then
    has_notification=true
  else
    spring_services+=("${service}")
  fi
done

spring_services_json='[]'
has_spring=false
if [ "${#spring_services[@]}" -gt 0 ]; then
  spring_services_json=$(printf '%s\n' "${spring_services[@]}" | jq -Rsc 'split("\n") | map(select(length > 0))')
  has_spring=true
fi

should_deploy=$has_services
if printf '%s\n' "$changed_files" | grep -Eq '^builds/develop/'; then
  should_deploy=true
fi

echo "services=$services_json" >> "$GITHUB_OUTPUT"
echo "spring_services=$spring_services_json" >> "$GITHUB_OUTPUT"
echo "has_services=$has_services" >> "$GITHUB_OUTPUT"
echo "has_spring=$has_spring" >> "$GITHUB_OUTPUT"
echo "has_notification=$has_notification" >> "$GITHUB_OUTPUT"
echo "should_deploy=$should_deploy" >> "$GITHUB_OUTPUT"
echo "Changed services: ${services[*]:-none}"
echo "Deploy required: $should_deploy"
