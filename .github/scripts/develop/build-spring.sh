#!/usr/bin/env bash
set -euo pipefail

: "${SERVICES_JSON:?SERVICES_JSON is required}"

services=()
while IFS= read -r service; do
  services+=("${service}")
done < <(jq -r '.[]' <<< "${SERVICES_JSON}")

if [ "${#services[@]}" -eq 0 ]; then
  echo "No Spring services to build."
  exit 0
fi

tasks=()
for service in "${services[@]}"; do
  tasks+=(":services:service-${service}:build")
done

chmod +x gradlew
./gradlew "${tasks[@]}" -x test --no-daemon

artifact_dir=build/develop-spring-jars
mkdir -p "${artifact_dir}"

for service in "${services[@]}"; do
  libs_dir="services/service-${service}/build/libs"
  jar=$(find "${libs_dir}" -name "service-${service}*.jar" ! -name "*-plain.jar" -print -quit)
  if [ -z "${jar}" ]; then
    echo "No boot jar found for service-${service} in ${libs_dir}" >&2
    exit 1
  fi

  cp "${jar}" "${artifact_dir}/service-${service}.jar"
done

echo "Prepared Spring artifacts for: ${services[*]}"
