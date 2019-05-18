package me.anon.lib.helper

import android.os.Environment
import me.anon.grow.MainApplication
import me.anon.lib.ext.T
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
	public fun backupJson(): File?
	{
		if (MainApplication.isFailsafe()) return null

		val isEncrypted = MainApplication.isEncrypted()
		val time = System.currentTimeMillis()
		val backupPath = File(FILES_PATH)
		val ext = isEncrypted T "dat" ?: "bak"
		backupPath.mkdirs()
		FileManager.getInstance().copyFile("${PlantManager.FILES_DIR}/plants.json", "$FILES_PATH/$time.plants.json.$ext")
		FileManager.getInstance().copyFile("${PlantManager.FILES_DIR}/schedules.json", "$FILES_PATH/$time.schedules.json.$ext")
		FileManager.getInstance().copyFile("${PlantManager.FILES_DIR}/gardens.json", "$FILES_PATH/$time.gardens.json.$ext")

		return backupPath
	}
}
