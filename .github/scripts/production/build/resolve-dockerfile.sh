#!/usr/bin/env bash
set -euo pipefail

: "${SERVICE:?SERVICE is required}"
: "${GITHUB_OUTPUT:?GITHUB_OUTPUT is required}"

if [ -f "builds/develop/Dockerfile.${SERVICE}" ]; then
  file="builds/develop/Dockerfile.${SERVICE}"
else
  file="builds/develop/Dockerfile"
fi

echo "file=${file}" >> "$GITHUB_OUTPUT"
echo "Using ${file}"