"""Escritura en Supabase mediante su API REST (PostgREST) con la service key."""

from __future__ import annotations

from datetime import date, datetime, timedelta, timezone

import requests

from .listadomanga import Evento

LOTE = 200


class Supabase:
    def __init__(self, url: str, service_key: str, session: requests.Session | None = None):
        self.base = url.rstrip("/") + "/rest/v1"
        self.http = session or requests.Session()
        self.http.headers.update({
            "apikey": service_key,
            "Authorization": f"Bearer {service_key}",
            "Content-Type": "application/json",
        })

    def upsert_eventos(self, eventos: list[Evento]) -> int:
        """Inserta o actualiza por slug. Devuelve cuántas filas se enviaron."""
        ahora = datetime.now(timezone.utc).isoformat()
        filas = [e.to_row() | {"updated_at": ahora} for e in eventos]
        for i in range(0, len(filas), LOTE):
            r = self.http.post(
                f"{self.base}/events",
                params={"on_conflict": "slug"},
                headers={"Prefer": "resolution=merge-duplicates,return=minimal"},
                json=filas[i:i + LOTE],
                timeout=30,
            )
            r.raise_for_status()
        return len(filas)

    def borrar_antiguos(self, hoy: date, años: int = 2) -> None:
        """Elimina eventos que terminaron hace más de `años` años para no llenar la base."""
        limite = hoy - timedelta(days=365 * años)
        r = self.http.delete(f"{self.base}/events", params={"end_date": f"lt.{limite.isoformat()}"}, timeout=30)
        r.raise_for_status()
