#!/usr/bin/env bash
set -euo pipefail

DESTINO="$(cd "$(dirname "$0")/.." && pwd)/src/main/resources/keys"
mkdir -p "$DESTINO"

openssl genrsa -out "$DESTINO/privateKey.pem" 2048
openssl rsa -in "$DESTINO/privateKey.pem" -pubout -out "$DESTINO/publicKey.pem"

chmod 600 "$DESTINO/privateKey.pem"
echo "Chaves JWT geradas em $DESTINO"
echo "A chave privada está no .gitignore — não commite privateKey.pem."
