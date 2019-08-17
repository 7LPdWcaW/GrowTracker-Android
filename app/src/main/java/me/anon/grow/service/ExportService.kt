package me.anon.grow.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.IBinder
import com.google.gson.reflect.TypeToken
import me.anon.grow.fragment.PlantDetailsFragment
import me.anon.lib.ExportCallback
import me.anon.lib.helper.ExportHelper
import me.anon.lib.helper.GsonHelper
import me.anon.lib.helper.NotificationHelper
import me.anon.lib.manager.PlantManager
import me.anon.model.Plant
import java.io.File

/**
 * Service for exporting
 */
class ExportService : Service()
{
	companion object
	{
		@JvmStatic
		public fun export(context: Context, plants: ArrayList<Plant>, title: String, name: String)
		{
			val intent = Intent(context, ExportService::class.java)
			intent.putStringArrayListExtra("plants", ArrayList(plants.map { it.id }))
			intent.putExtra("title", title)
			intent.putExtra("name", name)
			context.startService(intent)
		}
	}

	override fun onBind(intent: Intent?): IBinder? = null

	override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int
	{
		intent?.let {
			val plantsIds = it.extras?.getStringArrayList("plants") ?: arrayListOf()

			val plants = ArrayList(PlantManager.getInstance().plants.filter { plantsIds.contains(it.id) })
			val title = it.getStringExtra("title")
			val name = it.getStringExtra("name")

			NotificationHelper.createExportChannel(this)
			NotificationHelper.sendExportNotification(this, "Exporting grow log for $name", "Exporting grow log for $name")

			ExportHelper.exportPlants(this, plants, title, object : ExportCallback()
			{
				override fun onCallback(context: Context, file: File)
				{
					super.onCallback(context, file)
					NotificationHelper.sendExportCompleteNotification(context, "Export of $name complete", "Exported $name to ${file.absolutePath}", file)

					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
					{
						PlantDetailsFragment.MediaScannerWrapper(context, file.parent, "application/zip").scan()
					}
					else
					{
						context.sendBroadcast(Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.fromFile(file)))
					}

					stopSelf()
				}
			})
		}

		return START_NOT_STICKY
	}
}
