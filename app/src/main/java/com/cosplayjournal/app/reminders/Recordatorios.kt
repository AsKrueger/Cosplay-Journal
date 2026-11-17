package com.cosplayjournal.app.reminders

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.cosplayjournal.app.R
import com.cosplayjournal.app.ui.MainActivity
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

/** Avisa la víspera de un evento al que vas, a las 18:00. */
object Recordatorios {
    const val CANAL = "recordatorios"
    private val HORA = LocalTime.of(18, 0)

    fun crearCanal(context: Context) {
        val canal = NotificationChannel(CANAL, context.getString(R.string.recordatorio_canal), NotificationManager.IMPORTANCE_DEFAULT)
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(canal)
    }

    /** Momento del aviso, o null si ya ha pasado. */
    fun momento(inicio: LocalDate, ahora: LocalDateTime): LocalDateTime? =
        LocalDateTime.of(inicio.minusDays(1), HORA).takeIf { it.isAfter(ahora) }

    fun programar(context: Context, eventoId: Long, nombre: String, inicio: LocalDate) {
        val cuando = momento(inicio, LocalDateTime.now()) ?: return
        val trabajo = OneTimeWorkRequestBuilder<RecordatorioWorker>()
            .setInitialDelay(Duration.between(LocalDateTime.now(), cuando))
            .setInputData(workDataOf("id" to eventoId, "nombre" to nombre))
            .build()
        WorkManager.getInstance(context).enqueueUniqueWork(nombreTrabajo(eventoId), ExistingWorkPolicy.REPLACE, trabajo)
    }

    fun cancelar(context: Context, eventoId: Long) {
        WorkManager.getInstance(context).cancelUniqueWork(nombreTrabajo(eventoId))
    }

    fun cancelarTodos(context: Context) {
        WorkManager.getInstance(context).cancelAllWork()
    }

    private fun nombreTrabajo(id: Long) = "recordatorio-$id"
}

class RecordatorioWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val ctx = applicationContext
        if (ContextCompat.checkSelfPermission(ctx, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED &&
            android.os.Build.VERSION.SDK_INT >= 33
        ) return Result.success()

        val id = inputData.getLong("id", 0)
        val abrir = PendingIntent.getActivity(
            ctx, id.toInt(), Intent(ctx, MainActivity::class.java).putExtra("evento", id),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        val n = NotificationCompat.Builder(ctx, Recordatorios.CANAL)
            .setSmallIcon(R.drawable.ic_notificacion)
            .setContentTitle(ctx.getString(R.string.recordatorio_titulo))
            .setContentText(ctx.getString(R.string.recordatorio_texto, inputData.getString("nombre")))
            .setContentIntent(abrir)
            .setAutoCancel(true)
            .build()
        runCatching { NotificationManagerCompat.from(ctx).notify(id.toInt(), n) }
        return Result.success()
    }
}
