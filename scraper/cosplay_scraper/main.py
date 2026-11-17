"""Punto de entrada del cron diario.

    python -m cosplay_scraper.main            # importa salones de un año atrás a un año adelante
    python -m cosplay_scraper.main --dry-run  # solo imprime el JSON, sin escribir

Variables: SUPABASE_URL y SUPABASE_SERVICE_KEY.
"""

from __future__ import annotations

import argparse
import json
import logging
import os
import sys
import time
from datetime import date

import requests

from .listadomanga import Evento, parsear_pagina, url_mes
from .supabase_sync import Supabase

log = logging.getLogger("cosplay_scraper")
USER_AGENT = "CosplayJournalBot/2.0 (+https://github.com/AsKrueger/Cosplay-Journal)"


def meses(hoy: date, atras: int = 12, adelante: int = 12) -> list[tuple[int, int]]:
    """Lista de (año, mes) desde `atras` meses antes hasta `adelante` meses después."""
    indice = hoy.year * 12 + hoy.month - 1
    return [(i // 12, i % 12 + 1) for i in range(indice - atras, indice + adelante + 1)]


def descargar(url: str, http: requests.Session, intentos: int = 3, espera: float = 2.0) -> str:
    for intento in range(1, intentos + 1):
        try:
            r = http.get(url, timeout=20)
            r.raise_for_status()
            r.encoding = "utf-8"
            return r.text
        except requests.RequestException as e:
            log.warning("Intento %d/%d fallido en %s: %s", intento, intentos, url, e)
            if intento == intentos:
                raise
            time.sleep(espera * intento)
    raise AssertionError("inalcanzable")


def importar_eventos(hoy: date, http: requests.Session, pausa: float = 1.0) -> list[Evento]:
    por_slug: dict[str, Evento] = {}
    fallos = 0
    lista = meses(hoy)
    for año, mes in lista:
        try:
            html = descargar(url_mes(año, mes), http)
        except requests.RequestException:
            fallos += 1
            continue
        for e in parsear_pagina(html):
            por_slug.setdefault(e.slug, e)
        time.sleep(pausa)  # amable con el servidor de origen
    if fallos == len(lista):
        raise RuntimeError("No se pudo descargar ningún mes de ListadoManga")
    log.info("%d eventos únicos en %d meses (%d con error)", len(por_slug), len(lista), fallos)
    return list(por_slug.values())


def main(argv: list[str] | None = None) -> int:
    logging.basicConfig(level=logging.INFO, format="%(levelname)s %(message)s")
    p = argparse.ArgumentParser(description="Importa los salones de ListadoManga a Supabase")
    p.add_argument("--dry-run", action="store_true", help="no escribe en Supabase; imprime JSON")
    args = p.parse_args(argv)

    http = requests.Session()
    http.headers["User-Agent"] = USER_AGENT
    hoy = date.today()
    eventos = importar_eventos(hoy, http)

    if args.dry_run:
        sys.stdout.reconfigure(encoding="utf-8")
        json.dump([e.to_row() for e in eventos], sys.stdout, ensure_ascii=False, indent=1)
        return 0

    db = Supabase(os.environ["SUPABASE_URL"], os.environ["SUPABASE_SERVICE_KEY"])
    db.upsert_eventos(eventos)
    db.borrar_antiguos(hoy)
    log.info("Eventos sincronizados: %d", len(eventos))
    return 0


if __name__ == "__main__":
    sys.exit(main())
