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
import me.anon.model.Garden
import java.io.File
import java.util.*

class GardenManager private constructor()
{
	public val gardens: ArrayList<Garden> = arrayListOf()
	private lateinit var context: Context

	fun initialise(context: Context)
	{
		this.context = context.applicationContext
		FILES_DIR = this.context.getExternalFilesDir(null).absolutePath

		load()
	}

	fun load()
	{
		if (FileManager.getInstance().fileExists("$FILES_DIR/gardens.json"))
		{
			val gardensData = if (MainApplication.isEncrypted())
			{
				if (TextUtils.isEmpty(MainApplication.getKey()))
				{
					return
				}

				EncryptionHelper.decrypt(MainApplication.getKey(), FileManager.getInstance().readFile("$FILES_DIR/gardens.json"))
			}
			else
			{
				FileManager.getInstance().readFileAsString("$FILES_DIR/gardens.json")
			}

			try
			{
				if (!TextUtils.isEmpty(gardensData))
				{
					gardens.clear()
					gardens.addAll(GsonHelper.parse<Any>(gardensData, object : TypeToken<ArrayList<Garden>>(){}.type) as ArrayList<Garden>)
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
		synchronized(gardens)
		{
			if (MainApplication.isEncrypted())
			{
				if (TextUtils.isEmpty(MainApplication.getKey()))
				{
					return
				}

				FileManager.getInstance().writeFile("$FILES_DIR/gardens.json", EncryptionHelper.encrypt(MainApplication.getKey(), GsonHelper.parse(gardens)))
			}
			else
			{
				FileManager.getInstance().writeFile("$FILES_DIR/gardens.json", GsonHelper.parse(gardens))
			}

			if (File("$FILES_DIR/gardens.json").length() == 0L || !File("$FILES_DIR/gardens.json").exists())
			{
				Toast.makeText(context, "There was a fatal problem saving the garden data, please backup this data", Toast.LENGTH_LONG).show()
				val sendData = GsonHelper.parse(gardens)
				val share = Intent(Intent.ACTION_SEND)
				share.type = "text/plain"
				share.putExtra(Intent.EXTRA_TEXT, "== WARNING : PLEASE BACK UP THIS DATA == \r\n\r\n $sendData")
				context.startActivity(share)
			}
		}
	}

	fun insert(garden: Garden)
	{
		gardens.add(garden)
		save()
	}

	companion object
	{
		@JvmField public val instance = GardenManager()
		@JvmField public var FILES_DIR: String? = null

		@JvmStatic
		public fun getInstance(): GardenManager = instance
	}
}
