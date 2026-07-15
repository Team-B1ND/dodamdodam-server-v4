#!/usr/bin/env bash
set -euo pipefail

: "${GH_TOKEN:?GH_TOKEN is required}"
: "${K8S_REPOSITORY:?K8S_REPOSITORY is required}"
: "${SERVICES:?SERVICES is required}"
: "${TAG:?TAG is required}"

mapfile -t services < <(jq -r '.[]' <<< "$SERVICES")

wait_for_run() {
  local service="$1"
  local dispatched_at="$2"
  local title="Update ${service} image to ${TAG}"
  local run_id=''

  for attempt in $(seq 1 30); do
    run_id=$(gh api "repos/${K8S_REPOSITORY}/actions/workflows/update-service-image.yml/runs?event=repository_dispatch&per_page=50" \
      | jq -r --arg title "$title" --arg dispatched_at "$dispatched_at" \
      '.workflow_runs
      | map(select(.display_title == $title and .created_at >= $dispatched_at))
      | sort_by(.created_at)
      | last
      | .id // empty' || true)
    [ -n "$run_id" ] && break
    sleep 10
  done

  test -n "$run_id" || { echo "Could not find K8s workflow for $service" >&2; exit 1; }
  echo "Waiting for $service deployment workflow: $run_id"
  gh run watch "$run_id" --repo "$K8S_REPOSITORY" --exit-status
}

for ((index=0; index<${#services[@]}; index+=2)); do
  batch=("${services[index]}")
  [ $((index + 1)) -lt ${#services[@]} ] && batch+=("${services[index + 1]}")
  declare -A dispatched_at=()

  echo "Dispatching batch: ${batch[*]}"
  for service in "${batch[@]}"; do
    dispatched_at["$service"]=$(date -u +%Y-%m-%dT%H:%M:%SZ)
    gh api "repos/${K8S_REPOSITORY}/dispatches" \
      --method POST \
      -f event_type=service-image-published \
      -f "client_payload[service]=$service" \
      -f "client_payload[tag]=$TAG"
  done

  for service in "${batch[@]}"; do
    wait_for_run "$service" "${dispatched_at[$service]}"
  done
  echo "Batch completed: ${batch[*]}"
done
