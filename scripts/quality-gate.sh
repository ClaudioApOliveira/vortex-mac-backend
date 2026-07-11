#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "${ROOT_DIR}"

echo "==> Spotless (check)"
./mvnw -q spotless:check

echo "==> Checkstyle"
./mvnw -q checkstyle:check

echo "==> PMD"
./mvnw -q pmd:check

echo "==> Testes + JaCoCo (mínimo ${JACOCO_MINIMUM_COVERAGE:-0.14}, meta 80%)"
./mvnw -q test -DskipITs

if [ -f target/site/jacoco/jacoco.csv ]; then
  awk -F, 'NR>1 {miss+=$8; cov+=$9} END {
    total=miss+cov;
    if (total > 0) printf "Cobertura de linhas: %.1f%% (%d/%d) — meta 80%%\n", (cov/total)*100, cov, total
  }' target/site/jacoco/jacoco.csv
fi

echo "Quality gate aprovado."
