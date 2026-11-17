"""Provincias de España por los 2 primeros dígitos del código postal, con su centroide aproximado.

Sirve para ordenar eventos por cercanía sin usar servicios de geolocalización de pago.
"""

PROVINCIAS: dict[str, tuple[str, float, float]] = {
    "01": ("Álava", 42.85, -2.68), "02": ("Albacete", 38.99, -1.86), "03": ("Alicante", 38.35, -0.48),
    "04": ("Almería", 36.84, -2.46), "05": ("Ávila", 40.66, -4.70), "06": ("Badajoz", 38.88, -6.97),
    "07": ("Illes Balears", 39.57, 2.65), "08": ("Barcelona", 41.39, 2.17), "09": ("Burgos", 42.34, -3.70),
    "10": ("Cáceres", 39.47, -6.37), "11": ("Cádiz", 36.53, -6.29), "12": ("Castellón", 39.99, -0.05),
    "13": ("Ciudad Real", 38.99, -3.93), "14": ("Córdoba", 37.89, -4.78), "15": ("A Coruña", 43.36, -8.41),
    "16": ("Cuenca", 40.07, -2.14), "17": ("Girona", 41.98, 2.82), "18": ("Granada", 37.18, -3.60),
    "19": ("Guadalajara", 40.63, -3.17), "20": ("Gipuzkoa", 43.32, -1.98), "21": ("Huelva", 37.26, -6.95),
    "22": ("Huesca", 42.14, -0.41), "23": ("Jaén", 37.77, -3.79), "24": ("León", 42.60, -5.57),
    "25": ("Lleida", 41.62, 0.62), "26": ("La Rioja", 42.47, -2.45), "27": ("Lugo", 43.01, -7.56),
    "28": ("Madrid", 40.42, -3.70), "29": ("Málaga", 36.72, -4.42), "30": ("Murcia", 37.99, -1.13),
    "31": ("Navarra", 42.81, -1.64), "32": ("Ourense", 42.34, -7.86), "33": ("Asturias", 43.36, -5.85),
    "34": ("Palencia", 42.01, -4.53), "35": ("Las Palmas", 28.12, -15.43), "36": ("Pontevedra", 42.43, -8.64),
    "37": ("Salamanca", 40.97, -5.66), "38": ("Santa Cruz de Tenerife", 28.46, -16.25),
    "39": ("Cantabria", 43.46, -3.80), "40": ("Segovia", 40.95, -4.12), "41": ("Sevilla", 37.39, -5.98),
    "42": ("Soria", 41.76, -2.46), "43": ("Tarragona", 41.12, 1.25), "44": ("Teruel", 40.34, -1.11),
    "45": ("Toledo", 39.86, -4.02), "46": ("Valencia", 39.47, -0.38), "47": ("Valladolid", 41.65, -4.72),
    "48": ("Bizkaia", 43.26, -2.93), "49": ("Zamora", 41.50, -5.75), "50": ("Zaragoza", 41.65, -0.89),
    "51": ("Ceuta", 35.89, -5.32), "52": ("Melilla", 35.29, -2.94),
}


def provincia_de_cp(codigo_postal: str | None) -> str | None:
    """Devuelve el código de provincia ("28") de un código postal ("28045"), o None."""
    if not codigo_postal or len(codigo_postal) != 5 or not codigo_postal.isdigit():
        return None
    prefijo = codigo_postal[:2]
    return prefijo if prefijo in PROVINCIAS else None
