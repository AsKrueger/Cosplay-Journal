#!/usr/bin/env bash
# Aplica el stub de Supabase, las migraciones y los tests sobre un Postgres.
# Uso: PGHOST=... PGUSER=postgres PGPASSWORD=... ./supabase/tests/run.sh
set -euo pipefail
cd "$(dirname "$0")/.."

psql_run() { psql -v ON_ERROR_STOP=1 -X -q "$@"; }

psql_run -c "drop database if exists cj_test" -d postgres
psql_run -c "create database cj_test" -d postgres
psql_run -d cj_test -f tests/00_supabase_stub.sql
for f in migrations/*.sql; do
  echo "Migración: $f"
  psql_run -d cj_test -f "$f"
done
psql_run -d cj_test -f tests/10_politicas.sql
