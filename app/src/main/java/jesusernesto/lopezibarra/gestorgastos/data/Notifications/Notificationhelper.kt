package jesusernesto.lopezibarra.gestorgastos.data.Notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import jesusernesto.lopezibarra.gestorgastos.MainActivity
import jesusernesto.lopezibarra.gestorgastos.R

object Notificationhelper {

    private const val CHANNEL_ID = "presupuesto_alertas"
    private const val CHANNEL_NAME = "Alertas de Presupuesto"
    private const val CHANNEL_DESC = "Notificaciones cuando te acercas al límite"

    fun crearCanal(context: Context) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (manager.getNotificationChannel(CHANNEL_ID) != null) return

        val canal = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = CHANNEL_DESC
            enableVibration(true)
        }
        manager.createNotificationChannel(canal)
    }

    fun enviarAlertaPresupuesto(
        context: Context,
        categoria: String,
        porcentajeUsado: Int,
        gastado: Double,
        limite: Double,
        notifId: Int = (System.currentTimeMillis() % Int.MAX_VALUE).toInt()
    ){
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pedingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val titulo = "Alerta de presupuesto: $categoria"
        val mensaje = "Has usado el $porcentajeUsado% de tu límite " + "($${"%,.0f".format(gastado)} de $${"%,.0f".format(limite)})"

        val notif = NotificationCompat.Builder(context, CHANNEL_ID).
                setSmallIcon(R.drawable.ic_launcher_foreground).
                setContentTitle(titulo).
                setContentText(mensaje).
                setStyle(NotificationCompat.BigTextStyle().bigText(mensaje)).
                setPriority(NotificationCompat.PRIORITY_DEFAULT).
                setContentIntent(pedingIntent).
                setAutoCancel(true).
                build()

        manager.notify(notifId, notif)
    }

    fun enviarAlertaExcedido(
        context: Context,
        categoria: String,
        gastado: Double,
        limite: Double,
        notifId: Int = ((System.currentTimeMillis() + 1) % Int.MAX_VALUE).toInt()
    ){
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val exceso = gastado - limite
        val titulo = "¡Presupuesto excedido! $categoria"
        val mensaje = "Superaste tu límite por $${"%,.0f".format(exceso)}" + " (Gastado: $${"%,.0f".format(gastado)} / Limite: $${"%,.0f".format(limite)})"

        val notif = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(titulo)
            .setContentText(mensaje)
            .setStyle(NotificationCompat.BigTextStyle().bigText(mensaje))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        manager.notify(notifId, notif)
    }

}
