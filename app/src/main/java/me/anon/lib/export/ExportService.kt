package me.anon.lib.export

import android.app.Service
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.IBinder
import me.anon.grow.R
import me.anon.grow.fragment.PlantDetailsFragment
import me.anon.lib.helper.NotificationHelper
import me.anon.lib.manager.PlantManager
import me.anon.model.Garden

/**
 * Service for exporting
 */
class ExportService : Service()
{
	override fun onBind(intent: Intent?): IBinder? = null

	override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int
	{
		intent?.let {
			val plantsIds = it.extras?.getStringArrayList("plants") ?: arrayListOf()

			val plants = java.util.ArrayList(PlantManager.instance.plants.filter { plantsIds.contains(it.id) })
			val processor: Class<out ExportProcessor> = (it.getSerializableExtra("processor") as Class<ExportProcessor>?) ?: MarkdownProcessor::class.java
			val title = it.getStringExtra("title") ?: ""
			val name = it.getStringExtra("name") ?: ""
			val garden = it.getParcelableExtra<Garden?>("garden")
			val includeImages = it.getBooleanExtra("include_images", true)

			NotificationHelper.createExportChannel(this)
			NotificationHelper.sendExportNotification(this, getString(R.string.exporting_start, name), getString(R.string.exporting_start, name))

			ExportHelper(this, processor, includeImages)
				.executeExport(plants, garden, title, name) { file, context ->
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
		}

		return START_NOT_STICKY
	}
}
