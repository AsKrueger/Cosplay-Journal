"""Escritura en Supabase mediante su API REST (PostgREST) con la service key."""

from __future__ import annotations

from datetime import date, datetime, timedelta, timezone

import requests

from .listadomanga import Evento

LOTE = 200


class Supabase:
    def __init__(self, url: str, service_key: str, session: requests.Session | None = None):
        self.raiz = url.rstrip("/")
        self.base = self.raiz + "/rest/v1"
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

    def archivos_sobrantes(self, dias_chat: int = 90) -> list[tuple[str, str]]:
        """(bucket, ruta) de las fotos de chat antiguas y de los archivos que ya no usa nadie."""
        r = self.http.post(f"{self.base}/rpc/storage_purge_candidates", json={"p_chat_days": dias_chat}, timeout=30)
        r.raise_for_status()
        return [(f["bucket"], f["path"]) for f in r.json()]

    def borrar_archivos(self, archivos: list[tuple[str, str]]) -> int:
        """Borra con la API de Storage (quita fila y fichero). Devuelve cuántos se pidieron borrar."""
        por_bucket: dict[str, list[str]] = {}
        for bucket, ruta in archivos:
            por_bucket.setdefault(bucket, []).append(ruta)
        for bucket, rutas in por_bucket.items():
            for i in range(0, len(rutas), 100):
                r = self.http.delete(f"{self.raiz}/storage/v1/object/{bucket}",
                                     json={"prefixes": rutas[i:i + 100]}, timeout=30)
                r.raise_for_status()
        return len(archivos)
