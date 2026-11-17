from datetime import date
from pathlib import Path

import pytest

from cosplay_scraper.listadomanga import parsear_fechas, parsear_pagina, slugificar, url_mes
from cosplay_scraper.provincias import provincia_de_cp

FIXTURE = Path(__file__).parent / "fixtures" / "salones.html"


@pytest.mark.parametrize("texto, esperado", [
    ("Del 5 al 7 de Noviembre de 2026", (date(2026, 11, 5), date(2026, 11, 7))),
    ("7 de Noviembre de 2026", (date(2026, 11, 7), date(2026, 11, 7))),
    ("del 30 de octubre al 1 de noviembre de 2026", (date(2026, 10, 30), date(2026, 11, 1))),
    ("Del 30 de Diciembre al 2 de Enero de 2027", (date(2026, 12, 30), date(2027, 1, 2))),
    ("Del 30 de Diciembre de 2026 al 2 de Enero de 2027", (date(2026, 12, 30), date(2027, 1, 2))),
    ("5 y 6 de Septiembre de 2026", (date(2026, 9, 5), date(2026, 9, 6))),
    ("Del  12  al 13 de\nEnero de 2026", (date(2026, 1, 12), date(2026, 1, 13))),
])
def test_parsear_fechas(texto, esperado):
    assert parsear_fechas(texto) == esperado


def test_fecha_no_reconocida():
    assert parsear_fechas("Fecha por confirmar") is None


def test_slug_estable_sin_año_ni_acentos():
    assert slugificar("Salón del Manga de Prueba 2026", 2026) == "salon-del-manga-de-prueba-2026"
    assert slugificar("Salón del Manga de Prueba", 2026) == "salon-del-manga-de-prueba-2026"
    assert slugificar("2026", 2026) == "evento-2026"


def test_provincia_por_codigo_postal():
    assert provincia_de_cp("28045") == "28"
    assert provincia_de_cp("99000") is None
    assert provincia_de_cp(None) is None
    assert provincia_de_cp("280") is None


def test_url_mes():
    assert url_mes(2027, 3) == "https://www.listadomanga.es/salones.php?mes=3&ano=2027"


def test_parsear_pagina():
    eventos = parsear_pagina(FIXTURE.read_text(encoding="utf-8"))
    assert [e.name for e in eventos] == ["Salón del Manga de Prueba 2026", "Isla Con", "Fin de Año Friki"]

    madrid, isla, fin = eventos
    assert madrid.start_date == date(2026, 11, 5) and madrid.end_date == date(2026, 11, 7)
    assert madrid.venue == "Palacio de Congresos"
    assert madrid.address == "Avenida Principal, 1"
    assert madrid.city == "Madrid" and madrid.province_code == "28"
    assert madrid.price == "1 día: 8 € · Abono: 15 €"
    assert madrid.image_url == "https://static.listadomanga.com/aaa.jpg"
    assert madrid.website == "https://ejemplo-salon.es/"

    assert isla.city == "Villa de Mazo (La Palma)" and isla.province_code == "38"
    assert isla.address is None and isla.price is None

    assert fin.end_date == date(2027, 1, 2)
    assert fin.venue is None and fin.province_code is None and fin.website is None
    assert fin.image_url == "https://static.listadomanga.com/ccc.jpg"


def test_to_row_serializa_fechas():
    fila = parsear_pagina(FIXTURE.read_text(encoding="utf-8"))[0].to_row()
    assert fila["start_date"] == "2026-11-05" and fila["slug"] == "salon-del-manga-de-prueba-2026"
