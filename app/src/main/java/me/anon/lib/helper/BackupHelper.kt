package me.anon.lib.helper

import android.os.Environment
import me.anon.grow.MainApplication
import me.anon.lib.ext.toSafeInt
import me.anon.lib.manager.FileManager
import me.anon.lib.manager.GardenManager
import me.anon.lib.manager.PlantManager
import me.anon.lib.manager.ScheduleManager
import org.threeten.bp.DateTimeUtils
import org.threeten.bp.format.DateTimeFormatter
import java.io.File
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

/**
 * Helper class for backing up data files
 */
object BackupHelper
{
	@JvmField
	public var FILES_PATH = Environment.getExternalStorageDirectory().absolutePath + "/backups/GrowTracker"

	@JvmStatic
	public fun getLastBackup(): String
	{
		File(FILES_PATH).listFiles()?.let {
			val sorted = ArrayList(it.sortedBy { it.lastModified() })
			val parts = sorted.last().name.split(".")
			var date = Date()
			try
			{
				date = Date(java.lang.Long.parseLong(parts[0]))
			}
			catch (e: NumberFormatException)
			{
				try
				{
					date = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").parse(parts[0])
				}
				catch (e2: Exception)
				{
					date = Date(sorted.last().lastModified())
				}
			}

			return DateTimeUtils.toLocalDateTime(Timestamp(date.time)).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
		}

		return ""
	}

	@JvmStatic
	public fun backupJson(): File?
	{
		if (MainApplication.isFailsafe()) return null

		val time = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(Date())
		val backupPath = File(FILES_PATH)
		backupPath.mkdirs()
		limitBackups()

		FileManager.getInstance().copyFile("${PlantManager.FILES_DIR}/plants.${PlantManager.instance.fileExt}", "$FILES_PATH/$time.plants.${PlantManager.instance.fileExt}.bak")
		FileManager.getInstance().copyFile("${PlantManager.FILES_DIR}/schedules.${ScheduleManager.instance.fileExt}", "$FILES_PATH/$time.schedules.${ScheduleManager.instance.fileExt}.bak")
		FileManager.getInstance().copyFile("${PlantManager.FILES_DIR}/gardens.${GardenManager.getInstance().fileExt}", "$FILES_PATH/$time.gardens.${GardenManager.getInstance().fileExt}.bak")

		return backupPath
	}

	@JvmStatic
	public fun backupSize(): Long
	{
		val path = File(FILES_PATH)
		return path.listFiles()?.fold(0L, { acc, file -> acc + file.length() }) ?: 0L
	}

	@JvmStatic
	public fun limitBackups(size: String = MainApplication.getDefaultPreferences().getString("backup_size", "20")!!)
	{
		File(FILES_PATH).listFiles()?.let {
			val sorted = ArrayList(it.sortedBy { it.lastModified() })
			val limit = size.toSafeInt() * 1_048_576

			var currentSize = backupSize()
			while (currentSize > limit)
			{
				val remove = sorted.removeAt(0)
				val len = remove.length()
				if (remove.delete())
				{
					currentSize -= len
				}
			}
		}
	}
}
