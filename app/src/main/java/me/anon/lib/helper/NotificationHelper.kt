package me.anon.lib.helper

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.core.app.NotificationCompat
import me.anon.grow.R

/**
 * Helper class for sending notification
 */
object NotificationHelper
{
	@JvmStatic
	public fun createExportChannel(context: Context)
	{
		if (Build.VERSION.SDK_INT >= 26)
		{
			val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
			val channel = NotificationChannel("export", "Export status", NotificationManager.IMPORTANCE_DEFAULT)
			channel.setSound(null, null)
			channel.enableLights(false)
			channel.enableVibration(false)
			channel.lightColor = Color.GREEN

			notificationManager.createNotificationChannel(channel)
		}
	}

	@JvmStatic
	public fun sendDataTaskNotification(context: Context, title: String, message: String)
	{
		val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

		val exportNotification = NotificationCompat.Builder(context, "export")
			.setContentText(title)
			.setContentTitle(context.getString(R.string.data_task))
			.setContentIntent(PendingIntent.getActivity(context, 0, Intent(), PendingIntent.FLAG_UPDATE_CURRENT))
			.setTicker(message)
			.setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
			.setSmallIcon(R.drawable.ic_stat_name)
			.setSound(null)
			.setProgress(0, 0, false)
			.build()

		notificationManager.notify(1, exportNotification)
	}

	@JvmStatic
	public fun sendExportNotification(context: Context, title: String, message: String)
	{
		val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

		val exportNotification = NotificationCompat.Builder(context, "export")
			.setContentText(title)
			.setContentTitle(context.getString(R.string.export_progress))
			.setContentIntent(PendingIntent.getActivity(context, 0, Intent(), PendingIntent.FLAG_UPDATE_CURRENT))
			.setTicker(message)
			.setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
			.setSmallIcon(R.drawable.ic_stat_name)
			.setSound(null)
			.setProgress(0, 0, false)
			.build()

		notificationManager.notify(0, exportNotification)
	}
}
