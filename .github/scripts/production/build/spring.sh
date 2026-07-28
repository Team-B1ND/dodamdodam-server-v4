#!/usr/bin/env bash
set -euo pipefail

: "${SERVICE:?SERVICE is required}"

chmod +x gradlew
./gradlew ":services:service-${SERVICE}:build" -x test --no-daemon

dir="services/service-${SERVICE}/build/libs"
jar=$(find "$dir" -name "service-${SERVICE}-*.jar" ! -name "*-plain.jar" | head -1)

if [ -z "$jar" ]; then
  echo "No boot jar found for service-${SERVICE} in ${dir}" >&2
  exit 1
fi

cp "$jar" "${dir}/service-${SERVICE}.jar"
echo "Prepared ${dir}/service-${SERVICE}.jar"