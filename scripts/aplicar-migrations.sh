#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
CONTAINER="${MEC_POSTGRES_CONTAINER:-mec-postgres}"
DB_USER="${MEC_DB_USER:-postgres}"
DB_NAME="${MEC_DB_NAME:-mec}"
MIGRATIONS_DIR="${ROOT_DIR}/docker/postgres/migrations"

if ! docker ps --format '{{.Names}}' | grep -qx "${CONTAINER}"; then
  echo "Erro: container '${CONTAINER}' não está em execução."
  echo "Suba o Postgres com: docker compose up -d postgres"
  exit 1
fi

echo "Aplicando migrations em ${CONTAINER}/${DB_NAME}..."

for migration in "${MIGRATIONS_DIR}"/*.sql; do
  echo "  -> $(basename "${migration}")"
  docker exec -i "${CONTAINER}" psql -v ON_ERROR_STOP=1 -U "${DB_USER}" -d "${DB_NAME}" < "${migration}"
done

echo "Migrations aplicadas com sucesso."
