#!/usr/bin/env sh

set -eu

BASE_URL="${BASE_URL:-http://host.docker.internal:8080}"

docker run \
  --rm \
  -i \
  --add-host=host.docker.internal:host-gateway \
  -e BASE_URL="$BASE_URL" \
  grafana/k6:latest \
  run - \
  < performance/k6/resources-load-test.js