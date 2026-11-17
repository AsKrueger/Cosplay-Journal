# Importador de eventos

Lee los salones de [ListadoManga](https://www.listadomanga.es/salones.php) de un año atrás a un año adelante y los guarda en la tabla `events` de Supabase. Lo ejecuta cada día el workflow [Eventos](../.github/workflows/eventos.yml), que también se puede lanzar a mano desde la pestaña Actions.

| Dato | De dónde sale |
|---|---|
| Nombre, fechas | Título y primera línea de la tarjeta. Soporta `7 de Mayo de 2026`, `Del 5 al 7 de…`, rangos entre meses y entre años |
| Cartel, web | Imagen y enlace de la columna izquierda |
| Lugar, dirección, ciudad | Bloque `DIRECCIÓN`; la última línea es `CP Ciudad` |
| Provincia | Dos primeros dígitos del código postal (para "más cercanos") |
| Precio | Bloque `PRECIO` |

El identificador (`slug`) es el nombre sin año ni acentos más el año de inicio, así las actualizaciones no duplican eventos. Los eventos que terminaron hace más de 2 años se borran.

## Uso local

```bash
cd scraper
pip install -r requirements.txt pytest
python -m pytest -q
python -m cosplay_scraper.main --dry-run > eventos.json   # sin escribir en Supabase
```
