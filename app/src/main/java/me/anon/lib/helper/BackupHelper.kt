package me.anon.lib.helper

import android.os.Environment
import me.anon.lib.manager.FileManager
import me.anon.lib.manager.PlantManager
import java.io.File

/**
 * Helper class for backing up data files
 */
object BackupHelper
{
	@JvmField
	public var FILES_PATH = Environment.getExternalStorageDirectory().absolutePath + "/backups/GrowTracker"

	@JvmStatic
	public fun backupJson(): File
	{
		val time = System.currentTimeMillis()
		val backupPath = File(FILES_PATH)
		backupPath.mkdirs()
		FileManager.getInstance().copyFile("${PlantManager.FILES_DIR}/plants.json", "$FILES_PATH/$time.plants.json.bak")
		FileManager.getInstance().copyFile("${PlantManager.FILES_DIR}/schedules.json", "$FILES_PATH/$time.schedules.json.bak")
		FileManager.getInstance().copyFile("${PlantManager.FILES_DIR}/gardens.json", "$FILES_PATH/$time.gardens.json.bak")

		return backupPath
	}
}
