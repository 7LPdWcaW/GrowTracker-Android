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
	@JvmStatic
	public fun backupJson(): File
	{
		val time = System.currentTimeMillis()
		val backupPath = File(Environment.getExternalStorageDirectory(), "/backups/GrowTracker/")
		backupPath.mkdirs()
		FileManager.getInstance().copyFile("${PlantManager.FILES_DIR}/plants.json", Environment.getExternalStorageDirectory().absolutePath + "/backups/GrowTracker/$time.plants.json.bak")
		FileManager.getInstance().copyFile("${PlantManager.FILES_DIR}/schedules.json", Environment.getExternalStorageDirectory().absolutePath + "/backups/GrowTracker/$time.schedules.json.bak")
		FileManager.getInstance().copyFile("${PlantManager.FILES_DIR}/gardens.json", Environment.getExternalStorageDirectory().absolutePath + "/backups/GrowTracker/$time.gardens.json.bak")

		return backupPath
	}
}
