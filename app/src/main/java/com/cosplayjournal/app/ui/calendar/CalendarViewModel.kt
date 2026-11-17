package com.cosplayjournal.app.ui.calendar

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cosplayjournal.app.data.clasificar
import com.cosplayjournal.app.data.events.EventRepository
import com.cosplayjournal.app.data.events.EventoConEstado
import com.cosplayjournal.app.data.settings.SettingsRepository
import com.cosplayjournal.app.domain.Consulta
import com.cosplayjournal.app.domain.Filtro
import com.cosplayjournal.app.domain.filtrar
import com.cosplayjournal.app.ui.common.texto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth

enum class Vista { LISTA, MES }

data class CalendarioUi(
    val cargandoPrimeraVez: Boolean = true,
    val actualizando: Boolean = false,
    @StringRes val error: Int? = null,
    val vista: Vista = Vista.LISTA,
    val consulta: Consulta = Consulta(),
    val lista: List<EventoConEstado> = emptyList(),
    /** Eventos con filtro y búsqueda pero sin separar pasados, para la vista de mes. */
    val delMes: List<EventoConEstado> = emptyList(),
    val mes: YearMonth = YearMonth.now(),
    val diaSeleccionado: LocalDate = LocalDate.now(),
    val hayEventos: Boolean = false,
    val hoy: LocalDate = LocalDate.now(),
)

private data class Controles(
    val vista: Vista = Vista.LISTA,
    val filtro: Filtro = Filtro.TODOS,
    val texto: String = "",
    val pasados: Boolean = false,
    val mes: YearMonth = YearMonth.now(),
    val dia: LocalDate = LocalDate.now(),
)

private data class Red(val actualizando: Boolean = false, val error: Int? = null, val primeraVez: Boolean = true)

class CalendarViewModel(
    private val repo: EventRepository,
    private val settings: SettingsRepository,
    private val hoy: () -> LocalDate = LocalDate::now,
) : ViewModel() {

    private val controles = MutableStateFlow(Controles(mes = YearMonth.from(hoy()), dia = hoy()))
    private val red = MutableStateFlow(Red())

    val ui: StateFlow<CalendarioUi> = combine(repo.eventos, settings.ajustes, controles, red) { eventos, ajustes, c, r ->
        val d = hoy()
        val consulta = Consulta(c.filtro, c.texto, c.pasados, ajustes.miProvincia)
        CalendarioUi(
            cargandoPrimeraVez = r.primeraVez && eventos.isEmpty(),
            actualizando = r.actualizando,
            error = r.error,
            vista = c.vista,
            consulta = consulta,
            lista = filtrar(eventos, consulta, d),
            delMes = filtrar(eventos, consulta.copy(separarPasados = false), d),
            mes = c.mes,
            diaSeleccionado = c.dia,
            hayEventos = eventos.isNotEmpty(),
            hoy = d,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CalendarioUi())

    init {
        actualizar()
    }

    /** Se llama al abrir la app y al tirar hacia abajo. */
    fun actualizar() {
        if (red.value.actualizando) return
        red.update { it.copy(actualizando = true, error = null) }
        viewModelScope.launch {
            val r = runCatching { repo.actualizar() }
            red.update { Red(actualizando = false, error = r.exceptionOrNull()?.let { clasificar(it).texto() }, primeraVez = false) }
        }
    }

    fun errorVisto() = red.update { it.copy(error = null) }
    fun vista(v: Vista) = controles.update { it.copy(vista = v) }
    fun filtro(f: Filtro) = controles.update { it.copy(filtro = if (it.filtro == f) Filtro.TODOS else f) }
    fun buscar(t: String) = controles.update { it.copy(texto = t) }
    fun pasados(p: Boolean) = controles.update { it.copy(pasados = p) }
    fun mes(delta: Long) = controles.update { it.copy(mes = it.mes.plusMonths(delta)) }
    fun dia(d: LocalDate) = controles.update { it.copy(dia = d, mes = YearMonth.from(d)) }
    fun irAHoy() = controles.update { it.copy(dia = hoy(), mes = YearMonth.from(hoy())) }

    fun miProvincia(codigo: String?) {
        viewModelScope.launch {
            settings.setMiProvincia(codigo)
            if (codigo != null) controles.update { it.copy(filtro = Filtro.CERCANOS) }
        }
    }

    fun favorito(id: Long) = viewModelScope.launch { repo.cambiarFavorito(id) }
}
