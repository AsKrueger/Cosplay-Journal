"""Lectura de los salones publicados en https://www.listadomanga.es/salones.php."""

from __future__ import annotations

import re
import unicodedata
from dataclasses import asdict, dataclass
from datetime import date

from bs4 import BeautifulSoup, Tag

from .provincias import provincia_de_cp

BASE_URL = "https://www.listadomanga.es/salones.php"

MESES = {
    "enero": 1, "febrero": 2, "marzo": 3, "abril": 4, "mayo": 5, "junio": 6, "julio": 7,
    "agosto": 8, "septiembre": 9, "setiembre": 9, "octubre": 10, "noviembre": 11, "diciembre": 12,
}
_MES = "(" + "|".join(MESES) + ")"
_AÑO = r"(\d{4})"

# "Del 30 de Diciembre de 2026 al 2 de Enero de 2027"
_RANGO_AÑOS = re.compile(rf"del\s+(\d{{1,2}})\s+de\s+{_MES}\s+de\s+{_AÑO}\s+al\s+(\d{{1,2}})\s+de\s+{_MES}\s+de\s+{_AÑO}")
# "Del 30 de Octubre al 1 de Noviembre de 2026"
_RANGO_MESES = re.compile(rf"del\s+(\d{{1,2}})\s+de\s+{_MES}\s+al\s+(\d{{1,2}})\s+de\s+{_MES}\s+de\s+{_AÑO}")
# "Del 5 al 7 de Noviembre de 2026" (también "5 y 6 de ...")
_RANGO_DIAS = re.compile(rf"(?:del\s+)?(\d{{1,2}})\s+(?:al|y|-)\s+(\d{{1,2}})\s+de\s+{_MES}\s+de\s+{_AÑO}")
# "7 de Noviembre de 2026"
_DIA = re.compile(rf"(\d{{1,2}})\s+de\s+{_MES}\s+de\s+{_AÑO}")

_CP_CIUDAD = re.compile(r"^(\d{5})\s+(.+)$")


@dataclass(frozen=True)
class Evento:
    slug: str
    name: str
    start_date: date
    end_date: date
    venue: str | None
    address: str | None
    city: str | None
    province_code: str | None
    price: str | None
    image_url: str | None
    website: str | None

    def to_row(self) -> dict:
        fila = asdict(self)
        fila["start_date"] = self.start_date.isoformat()
        fila["end_date"] = self.end_date.isoformat()
        return fila


def url_mes(año: int, mes: int) -> str:
    return f"{BASE_URL}?mes={mes}&ano={año}"


def parsear_fechas(texto: str) -> tuple[date, date] | None:
    """Convierte el texto de fechas de una tarjeta en (inicio, fin). None si no se reconoce."""
    t = re.sub(r"\s+", " ", texto.lower()).strip()
    if m := _RANGO_AÑOS.search(t):
        return (date(int(m[3]), MESES[m[2]], int(m[1])), date(int(m[6]), MESES[m[5]], int(m[4])))
    if m := _RANGO_MESES.search(t):
        año = int(m[5])
        inicio_mes, fin_mes = MESES[m[2]], MESES[m[4]]
        # Un rango que cruza el año sin repetirlo: "Del 30 de Diciembre al 2 de Enero de 2027"
        inicio_año = año - 1 if inicio_mes > fin_mes else año
        return (date(inicio_año, inicio_mes, int(m[1])), date(año, fin_mes, int(m[3])))
    if m := _RANGO_DIAS.search(t):
        año, mes = int(m[4]), MESES[m[3]]
        inicio, fin = int(m[1]), int(m[2])
        return (date(año, mes, inicio), date(año, mes, max(inicio, fin)))
    if m := _DIA.search(t):
        d = date(int(m[3]), MESES[m[2]], int(m[1]))
        return (d, d)
    return None


def slugificar(nombre: str, año: int) -> str:
    """Identificador estable: nombre sin año ni acentos + año de inicio."""
    sin_acentos = unicodedata.normalize("NFKD", nombre).encode("ascii", "ignore").decode()
    sin_año = re.sub(r"\b(19|20)\d{2}\b", "", sin_acentos.lower())
    base = re.sub(r"[^a-z0-9]+", "-", sin_año).strip("-")
    return f"{base or 'evento'}-{año}"


def _lineas(celda: Tag) -> list[str]:
    """Texto de la tarjeta separado por <br> y <hr>, sin líneas vacías."""
    for br in celda.find_all(["br", "hr"]):
        br.replace_with("\n")
    return [re.sub(r"\s+", " ", l).strip() for l in celda.get_text().split("\n") if l.strip()]


def _bloque(lineas: list[str], cabecera: str) -> list[str]:
    """Líneas que siguen a una cabecera en mayúsculas ("DIRECCIÓN", "PRECIO") hasta la siguiente."""
    fuera: list[str] = []
    dentro = False
    for linea in lineas:
        es_cabecera = linea.isupper() and len(linea) < 30 and not any(c.isdigit() for c in linea)
        if es_cabecera:
            dentro = linea.startswith(cabecera)
            continue
        if dentro:
            fuera.append(linea)
    return fuera


def parsear_pagina(html: str) -> list[Evento]:
    sopa = BeautifulSoup(html, "html.parser")
    eventos: list[Evento] = []
    for h2 in sopa.find_all("h2"):
        celda = h2.parent
        if not isinstance(celda, Tag) or celda.name != "td":
            continue
        nombre = re.sub(r"\s+", " ", h2.get_text(" ")).strip()
        if not nombre:
            continue

        h2.extract()
        lineas = _lineas(celda)
        fechas = parsear_fechas(" ".join(lineas[:2])) if lineas else None
        if fechas is None:
            continue

        imagen = web = None
        anterior = celda.find_previous_sibling("td")
        if anterior is not None:
            if (img := anterior.find("img")) and img.get("src"):
                imagen = img["src"]
            if (a := anterior.find("a")) and a.get("href", "").startswith("http"):
                web = a["href"]

        direccion = _bloque(lineas, "DIRECCI")
        cp = ciudad = None
        if direccion and (m := _CP_CIUDAD.match(direccion[-1])):
            cp, ciudad = m[1], m[2]
            direccion = direccion[:-1]
        lugar = direccion[0] if direccion else None
        calle = ", ".join(direccion[1:]) or None
        precio = " · ".join(l.lstrip("- ").strip() for l in _bloque(lineas, "PRECIO")) or None

        eventos.append(Evento(
            slug=slugificar(nombre, fechas[0].year),
            name=nombre,
            start_date=fechas[0],
            end_date=fechas[1],
            venue=lugar,
            address=calle,
            city=ciudad,
            province_code=provincia_de_cp(cp),
            price=precio[:200] if precio else None,
            image_url=imagen,
            website=web,
        ))
    return eventos
