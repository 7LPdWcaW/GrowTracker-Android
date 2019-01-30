package me.anon.lib.manager

import android.content.Context
import android.content.Intent
import android.text.TextUtils
import android.widget.Toast
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import me.anon.grow.MainApplication
import me.anon.lib.helper.EncryptionHelper
import me.anon.lib.helper.GsonHelper
import me.anon.model.FeedingSchedule
import java.io.File
import java.util.*

class ScheduleManager private constructor()
{
	public val schedules: ArrayList<FeedingSchedule> = arrayListOf()
	private lateinit var context: Context

	fun initialise(context: Context)
	{
		this.context = context.applicationContext
		FILES_DIR = this.context.getExternalFilesDir(null).absolutePath

		load()
	}

	fun load()
	{
		if (FileManager.getInstance().fileExists("$FILES_DIR/schedules.json"))
		{
			val scheduleData = if (MainApplication.isEncrypted())
			{
				if (TextUtils.isEmpty(MainApplication.getKey()))
				{
					return
				}

				EncryptionHelper.decrypt(MainApplication.getKey(), FileManager.getInstance().readFile("$FILES_DIR/schedules.json"))
			}
			else
			{
				FileManager.getInstance().readFileAsString("$FILES_DIR/schedules.json")
			}

			try
			{
				if (!TextUtils.isEmpty(scheduleData))
				{
					schedules.clear()
					schedules.addAll(GsonHelper.parse<Any>(scheduleData, object : TypeToken<ArrayList<FeedingSchedule>>(){}.type) as ArrayList<FeedingSchedule>)
				}
			}
			catch (e: JsonSyntaxException)
			{
				e.printStackTrace()
			}

		}
	}

	fun save()
	{
		synchronized(schedules) {
			if (MainApplication.isEncrypted())
			{
				if (TextUtils.isEmpty(MainApplication.getKey()))
				{
					return
				}

				FileManager.getInstance().writeFile("$FILES_DIR/schedules.json", EncryptionHelper.encrypt(MainApplication.getKey(), GsonHelper.parse(schedules)))
			}
			else
			{
				FileManager.getInstance().writeFile("$FILES_DIR/schedules.json", GsonHelper.parse(schedules))
			}

			if (File("$FILES_DIR/schedules.json").length() == 0L || !File("$FILES_DIR/schedules.json").exists())
			{
				Toast.makeText(context, "There was a fatal problem saving the schedule data, please backup this data", Toast.LENGTH_LONG).show()
				val sendData = GsonHelper.parse(schedules)
				val share = Intent(Intent.ACTION_SEND)
				share.type = "text/plain"
				share.putExtra(Intent.EXTRA_TEXT, "== WARNING : PLEASE BACK UP THIS DATA == \r\n\r\n $sendData")
				context.startActivity(share)
			}
		}
	}

	fun insert(schedule: FeedingSchedule)
	{
		schedules.add(schedule)
		save()
	}

	companion object
	{
		@JvmField public val instance = ScheduleManager()
		@JvmField public var FILES_DIR: String? = null
	}
}
