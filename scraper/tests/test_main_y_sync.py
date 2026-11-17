from datetime import date
from pathlib import Path

import pytest
import requests

from cosplay_scraper import main as cli
from cosplay_scraper.listadomanga import parsear_pagina
from cosplay_scraper.supabase_sync import Supabase

FIXTURE = (Path(__file__).parent / "fixtures" / "salones.html").read_text(encoding="utf-8")


class Respuesta:
    def __init__(self, status=200, text=""):
        self.status_code, self.text, self.encoding = status, text, None

    def raise_for_status(self):
        if self.status_code >= 400:
            raise requests.HTTPError(f"HTTP {self.status_code}")


class SesionFalsa:
    def __init__(self, respuestas=None):
        self.headers = {}
        self.llamadas = []
        self.respuestas = respuestas or {}

    def _registrar(self, metodo, url, **kw):
        self.llamadas.append((metodo, url, kw))
        r = self.respuestas.get(url, Respuesta())
        if isinstance(r, Exception):
            raise r
        return r

    def get(self, url, **kw):
        return self._registrar("GET", url, **kw)

    def post(self, url, **kw):
        return self._registrar("POST", url, **kw)

    def delete(self, url, **kw):
        return self._registrar("DELETE", url, **kw)


def test_meses_cubre_un_año_atras_y_adelante():
    lista = cli.meses(date(2026, 11, 17))
    assert len(lista) == 25
    assert lista[0] == (2025, 11) and lista[12] == (2026, 11) and lista[-1] == (2027, 11)


def test_meses_en_enero_cruza_de_año():
    assert cli.meses(date(2026, 1, 5), atras=1, adelante=0) == [(2025, 12), (2026, 1)]


def test_importar_deduplica_y_tolera_meses_caidos(monkeypatch):
    monkeypatch.setattr(cli, "meses", lambda hoy: [(2026, 11), (2026, 12), (2027, 1)])
    sesion = SesionFalsa({
        "https://www.listadomanga.es/salones.php?mes=11&ano=2026": Respuesta(text=FIXTURE),
        "https://www.listadomanga.es/salones.php?mes=12&ano=2026": Respuesta(text=FIXTURE),
        "https://www.listadomanga.es/salones.php?mes=1&ano=2027": Respuesta(status=503),
    })
    monkeypatch.setattr(cli.time, "sleep", lambda s: None)
    eventos = cli.importar_eventos(date(2026, 11, 17), sesion, pausa=0)
    assert len(eventos) == 3


def test_importar_falla_si_no_hay_ningun_mes(monkeypatch):
    monkeypatch.setattr(cli, "meses", lambda hoy: [(2026, 11)])
    monkeypatch.setattr(cli.time, "sleep", lambda s: None)
    sesion = SesionFalsa({"https://www.listadomanga.es/salones.php?mes=11&ano=2026": Respuesta(status=500)})
    with pytest.raises(RuntimeError):
        cli.importar_eventos(date(2026, 11, 17), sesion, pausa=0)


def test_upsert_por_slug_con_service_key():
    sesion = SesionFalsa()
    db = Supabase("https://x.supabase.co/", "clave", session=sesion)
    assert db.upsert_eventos(parsear_pagina(FIXTURE)) == 3

    metodo, url, kw = sesion.llamadas[0]
    assert (metodo, url) == ("POST", "https://x.supabase.co/rest/v1/events")
    assert kw["params"] == {"on_conflict": "slug"}
    assert "merge-duplicates" in kw["headers"]["Prefer"]
    assert sesion.headers["Authorization"] == "Bearer clave"
    assert {f["slug"] for f in kw["json"]} >= {"isla-con-2026"}
    assert all("updated_at" in f for f in kw["json"])


def test_borrar_eventos_de_hace_mas_de_dos_años():
    sesion = SesionFalsa()
    Supabase("https://x.supabase.co", "k", session=sesion).borrar_antiguos(date(2026, 11, 17))
    metodo, _, kw = sesion.llamadas[0]
    assert metodo == "DELETE" and kw["params"] == {"end_date": "lt.2024-11-17"}
