package me.anon.controller.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import me.anon.lib.helper.BackupHelper
import org.threeten.bp.DateTimeUtils
import org.threeten.bp.LocalDate
import java.io.File
import java.sql.Date

class BackupService : BroadcastReceiver()
{
	override fun onReceive(context: Context, intent: Intent)
	{
		try
		{
			var backup = true
			File(BackupHelper.FILES_PATH).listFiles()?.let {
				if (it.isEmpty()) return@let
				val sorted = ArrayList(it.sortedBy { it.lastModified() })
				val today = LocalDate.now()
				backup = DateTimeUtils.toLocalDate(Date(sorted.last().lastModified())) != today
			}

			if (backup)
			{
				BackupHelper.backupJson()
			}
		}
		catch (e: Exception)
		{
		}
	}
}
