package me.anon.lib.helper

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.support.v4.app.NotificationCompat
import android.support.v4.content.FileProvider
import me.anon.grow.R
import java.io.File

/**
 * // TODO: Add class description
 */
object NotificationHelper
{
	@JvmStatic
	public fun createExportChannel(context: Context)
	{
		if (Build.VERSION.SDK_INT >= 26)
		{
			val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
			val channel = NotificationChannel("export", "Export status", NotificationManager.IMPORTANCE_HIGH)
			channel.setSound(null, null)
			channel.enableLights(false)
			channel.enableVibration(false)
			channel.lightColor = Color.GREEN

			notificationManager.createNotificationChannel(channel)
		}
	}

	@JvmStatic
	public fun sendExportNotification(context: Context, title: String, message: String)
	{
		val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
		notificationManager.cancel(0)

		val exportNotification = NotificationCompat.Builder(context, "export")
			.setContentText(title)
			.setContentTitle("Exporting")
			.setContentIntent(PendingIntent.getActivity(context, 0, Intent(), PendingIntent.FLAG_UPDATE_CURRENT))
			.setTicker(message)
			.setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
			.setSmallIcon(R.drawable.ic_stat_name)
			.setSound(null)
			.build()

		notificationManager.notify(0, exportNotification)
	}

	@JvmStatic
	public fun sendExportCompleteNotification(context: Context, title: String, message: String, file: File)
	{
		val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
		notificationManager.cancel(0)

		val openIntent = Intent(Intent.ACTION_VIEW)
		val apkURI = FileProvider.getUriForFile(context, context.applicationContext.packageName + ".provider", file)
		openIntent.setDataAndType(apkURI, "application/zip")
		openIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

		val finishNotification = NotificationCompat.Builder(context, "export")
			.setContentText(message)
			.setTicker(title)
			.setContentTitle("Export Complete")
			.setContentIntent(PendingIntent.getActivity(context, 0, openIntent, PendingIntent.FLAG_UPDATE_CURRENT))
			.setStyle(NotificationCompat.BigTextStyle()
				.bigText(message)
			)
			.setSmallIcon(R.drawable.ic_stat_done)
			.setPriority(NotificationCompat.PRIORITY_HIGH)
			.setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
			.setAutoCancel(true)
			.setSound(null)
			.build()

		notificationManager.notify(0, finishNotification)
	}
}
