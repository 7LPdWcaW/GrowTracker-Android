package me.anon.lib.manager

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.AsyncTask
import android.text.TextUtils
import android.widget.Toast
import com.squareup.moshi.JsonDataException
import com.squareup.moshi.Types
import me.anon.grow.BootActivity
import me.anon.grow.MainApplication
import me.anon.lib.helper.AddonHelper
import me.anon.lib.helper.BackupHelper
import me.anon.lib.helper.MoshiHelper
import me.anon.lib.stream.DecryptInputStream
import me.anon.lib.stream.EncryptOutputStream
import me.anon.lib.task.AsyncCallback
import me.anon.model.Garden
import me.anon.model.Plant
import java.io.*
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicBoolean

class PlantManager private constructor()
{
	var plants = ArrayList<Plant>()
		set(plants) = synchronized(this.plants) {
			if (!MainApplication.isFailsafe())
			{
				this.plants.clear()
				this.plants.addAll(plants)
			}
		}

	private lateinit var context: Context
	private val saveTask = ConcurrentLinkedQueue<SaveAsyncTask>()
	private val isSaving = AtomicBoolean(false)

	public fun initialise(context: Context)
	{
		this.context = context.applicationContext
		FILES_DIR = this.context.getExternalFilesDir(null)!!.absolutePath

		load()
	}

	public fun indexOf(plant: Plant) = plants.indexOfFirst { it.id == plant.id }

	public fun getSortedPlantList(garden: Garden?): ArrayList<Plant>
	{
		if (MainApplication.isFailsafe())
		{
			return ArrayList()
		}

		synchronized(this.plants) {
			val plantsSize = PlantManager.instance.plants.size
			val ordered = arrayOfNulls<Plant>(garden?.plantIds?.size ?: plantsSize)

			for (index in 0 until plantsSize)
			{
				val plant = PlantManager.instance.plants[index]

				if (garden != null && !garden.plantIds.contains(plant.id))
				{
					continue
				}

				ordered[garden?.plantIds?.indexOf(plant.id) ?: index] = plant
			}

			val finalList = ArrayList<Plant>()
			finalList.addAll(Arrays.asList<Plant>(*ordered))
			finalList.removeAll { it == null }

			return finalList
		}
	}

	public fun addPlant(plant: Plant)
	{
		synchronized(this.plants) {
			if (!MainApplication.isFailsafe())
			{
				plants.add(plant)
				save()
			}
		}
	}

	@SuppressLint("StaticFieldLeak")
	public fun deletePlant(plant: Plant, callback: AsyncCallback?)
	{
		//		synchronized (this.mPlants)
		run {
			if (MainApplication.isFailsafe()) return

			object : AsyncTask<Void, Void, Void>()
			{
				override fun doInBackground(vararg params: Void): Void?
				{
					plants.find { it.id == plant.id }?.let { plant ->
						// Delete images
						plant.images?.forEach { filePath ->
							File(filePath).delete()
						}

						// Remove plant
						plants.removeAll { it.id == plant.id }

						// Remove from shared prefs
//						val prefs = PreferenceManager.getDefaultSharedPreferences(context)
//						prefs.edit().remove(plantIndex.toString()).apply()
					}

					return null
				}

				override fun onPostExecute(aVoid: Void)
				{
					callback?.callback()
				}
			}.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR)
		}
	}

	public fun upsert(plant: Plant)
	{
		var index = plants.indexOfFirst { it.id == plant.id }

		if (index < 0)
		{
			addPlant(plant)
		}
		else
		{
			plants[index] = plant
			save()
		}
	}

	@JvmOverloads
	public fun load(fromRestore: Boolean = false): Boolean
	{
		if (MainApplication.isFailsafe())
		{
			return false
		}

		// redundancy check
		if (File(FILES_DIR, "/plants.json").lastModified() < File(FILES_DIR, "/plants.json.bak").lastModified())
		{
			FileManager.getInstance().copyFile("$FILES_DIR/plants.json.bak", "$FILES_DIR/plants.json")
		}

		if (FileManager.getInstance().fileExists("$FILES_DIR/plants.json"))
		{
			try
			{
				if (MainApplication.isEncrypted())
				{
					if (TextUtils.isEmpty(MainApplication.getKey()))
					{
						return false
					}

					val stream = DecryptInputStream(MainApplication.getKey(), File(FILES_DIR, "/plants.json"))

					plants.clear()
					plants.addAll(MoshiHelper.parse(stream, Types.newParameterizedType(ArrayList::class.java, Plant::class.java)))
					MainApplication.setFailsafe(false)
				}
				else
				{
					plants.clear()
					plants.addAll(MoshiHelper.parse(File(FILES_DIR, "/plants.json"), Types.newParameterizedType(ArrayList::class.java, Plant::class.java)))
					MainApplication.setFailsafe(false)
				}

				return true
			}
			catch (e: JsonDataException)
			{
				e.printStackTrace()

				if (!fromRestore)
				{
					val backupPath = BackupHelper.backupJson()
					Toast.makeText(context, "There is a syntax error in your app data. Your data has been backed up to " + backupPath!!.path + ". Please fix before re-opening the app.\n" + e.message, Toast.LENGTH_LONG).show()

					// prevent save
					MainApplication.setFailsafe(true)
				}
			}
			catch (e: Exception)
			{
				e.printStackTrace()

				if (!fromRestore)
				{
					val backupPath = BackupHelper.backupJson()
					Toast.makeText(context, "There is a problem loading your app data. Your data has been backed up to " + backupPath!!.path, Toast.LENGTH_LONG).show()

					// prevent save
					MainApplication.setFailsafe(true)
				}
			}
		}

		return false
	}

	@SuppressLint("StaticFieldLeak")
	@JvmOverloads
	public fun save(callback: AsyncCallback? = null, ignoreCheck: Boolean = false)
	{
		synchronized(plants) {
			if (MainApplication.isFailsafe()) return

			if (!ignoreCheck && plants.size > 0 || ignoreCheck)
			{

				saveTask.add(object : SaveAsyncTask(plants)
				{
					override fun doInBackground(vararg params: Void?): Int
					{
						FileManager.getInstance().copyFile("$FILES_DIR/plants.json", "$FILES_DIR/plants.json.bak")

						try
						{
							var outstream: OutputStream

							if (MainApplication.isEncrypted())
							{
								if (TextUtils.isEmpty(MainApplication.getKey()))
								{
									return 1
								}

								outstream = EncryptOutputStream(MainApplication.getKey(), File("$FILES_DIR/plants.json"))
							}
							else
							{
								outstream = FileOutputStream(File("$FILES_DIR/plants.json"))
							}

							val output = MoshiHelper.toJson(plants, Types.newParameterizedType(ArrayList::class.java, Plant::class.java))
							val writer = BufferedWriter(OutputStreamWriter(outstream))
							writer.write(output)
							writer.flush()
							writer.close()
						}
						catch (e: Exception)
						{
							e.printStackTrace()
						}

						return 0
					}

					override fun onPostExecute(aVoid: Int)
					{
						callback?.callback()

						if (File("$FILES_DIR/plants.json").length() == 0L || !File("$FILES_DIR/plants.json").exists())
						{
							Toast.makeText(context, "There was a fatal problem saving the plant data, please backup this data", Toast.LENGTH_LONG).show()
							val sendData = MoshiHelper.toJson(this.plants, Types.newParameterizedType(ArrayList::class.java, Plant::class.java))
							FileManager.getInstance().writeFile(context.getExternalFilesDir(null)?.absolutePath + "/temp.txt", sendData)

							val share = Intent(Intent.ACTION_SEND)
							share.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
							share.type = "text/plain"
							share.putExtra(Intent.EXTRA_TEXT, "== WARNING : PLEASE BACK UP THIS DATA ==")
							val uri = Uri.parse("file://${context.getExternalFilesDir(null)?.absolutePath}/temp.txt")
							share.putExtra(Intent.EXTRA_STREAM, uri)
							context.startActivity(Intent.createChooser(share, "backup to"))
						}

						if (saveTask.size > 0)
						{
							saveTask.poll()!!.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR)
						}
						else
						{
							isSaving.set(false)
						}

						AddonHelper.broadcastPlantList(context)
					}
				})

				if (!isSaving.get())
				{
					isSaving.set(true)
					saveTask.poll()!!.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR)
				}
			}
			else
			{
				load()
				val restart = Intent(context, BootActivity::class.java)
				restart.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
				context.startActivity(restart)
			}
		}
	}

	abstract class SaveAsyncTask(plants: List<Plant>) : AsyncTask<Void, Void, Int>()
	{
		protected var plants: ArrayList<Plant> = ArrayList(plants)
	}

	companion object
	{
		@JvmStatic
		@SuppressLint("StaticFieldLeak")
		val instance = PlantManager()
		@JvmField
		var FILES_DIR: String = ""
	}
}
