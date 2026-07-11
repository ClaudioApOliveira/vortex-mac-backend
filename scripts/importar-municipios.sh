#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
OUTPUT_DIR="${ROOT_DIR}/docker/postgres/seeds"
OUTPUT_FILE="${OUTPUT_DIR}/municipios.sql"
IBGE_ESTADOS_URL="https://servicodados.ibge.gov.br/api/v1/localidades/estados"
IBGE_MUNICIPIOS_URL="https://servicodados.ibge.gov.br/api/v1/localidades/estados"

mkdir -p "${OUTPUT_DIR}"

if ! command -v jq >/dev/null 2>&1; then
  echo "Erro: jq é obrigatório. Instale com: brew install jq"
  exit 1
fi

echo "Buscando estados no IBGE..."
UFS=$(curl -fsSL "${IBGE_ESTADOS_URL}" | jq -r '.[].sigla | @sh' | tr -d "'")

{
  echo "-- Gerado automaticamente por scripts/importar-municipios.sh"
  echo "-- Fonte: ${IBGE_MUNICIPIOS_URL}/{UF}/municipios"
  echo "BEGIN;"
  echo "DELETE FROM municipios;"
} > "${OUTPUT_FILE}"

TOTAL=0

for UF in ${UFS}; do
  echo "Importando municípios de ${UF}..."
  RESPONSE=$(curl -fsSL "${IBGE_MUNICIPIOS_URL}/${UF}/municipios")
  echo "${RESPONSE}" | jq -r --arg uf "${UF}" '
    def sql_str: gsub("\u0027"; "\u0027\u0027") | "'"'"'" + . + "'"'"'";
    .[] |
    "INSERT INTO municipios (id, nome, uf) VALUES (\(.id), \(.nome | sql_str), '"'"'" + $uf + "'"'"');"
  ' >> "${OUTPUT_FILE}"
  COUNT=$(echo "${RESPONSE}" | jq 'length')
  TOTAL=$((TOTAL + COUNT))
done

echo "COMMIT;" >> "${OUTPUT_FILE}"

echo ""
echo "Arquivo gerado: ${OUTPUT_FILE}"
echo "Total de municípios: ${TOTAL}"
echo ""
echo "Para carregar no PostgreSQL local:"
echo "  docker exec -i mec-postgres psql -U postgres -d mec < docker/postgres/migrations/006_municipios.sql"
echo "  docker exec -i mec-postgres psql -U postgres -d mec < docker/postgres/seeds/municipios.sql"
