package com.example.exempleworkmanager

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.exempleworkmanager.api.CrudApi
import com.example.exempleworkmanager.api.Dita


class UpdateWorker(
    val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {  // Hereta de CoroutineWorker

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun doWork(): Result {    // Runs every 15 minutes
        val dita = CrudApi.getDita() ?: return Result.retry()  // retry with backoff if the API is unreachable

        showNotification(context, dita)
        return Result.success()
    }

}

@RequiresApi(Build.VERSION_CODES.O)
fun showNotification(context: Context, dita: Dita?) {
    val channelId = "updates_channel"
    val notificationId = 1

    val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    // 1. Crear el canal (Solo necesario una vez)
    val channel = NotificationChannel(
        channelId,
        "Dites",
        NotificationManager.IMPORTANCE_DEFAULT
    )
    manager.createNotificationChannel(channel)

    // 2. Construir la notificación
    val notification = NotificationCompat.Builder(context, channelId)
        .setSmallIcon(android.R.drawable.stat_notify_sync) // Usa un icono de tu app
        .setContentTitle(dita?.dita)
        .setContentText(dita?.explicacio)
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .setAutoCancel(true) // Se borra al tocarla
        .build()

    // 3. Lanzar
    manager.notify(notificationId, notification)
}