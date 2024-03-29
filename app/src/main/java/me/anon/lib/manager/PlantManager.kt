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
import me.anon.grow.MainApplication
import me.anon.lib.helper.BackupHelper
import me.anon.lib.helper.MoshiHelper
import me.anon.lib.stream.DecryptInputStream
import me.anon.lib.stream.EncryptOutputStream
import me.anon.lib.task.AsyncCallback
import me.anon.model.Garden
import me.anon.model.Plant
import java.io.*
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
	public val fileExt: String
		get() {
			if (MainApplication.isEncrypted()) return "dat"
			else return "json"
		}

	public fun initialise(context: Context)
	{
		this.context = context.applicationContext
		FILES_DIR = this.context.getExternalFilesDir(null)!!.absolutePath
	}

	public fun indexOf(plant: Plant) = plants.indexOfFirst { it.id == plant.id }

	public fun getPlant(id: String) = plants.find { it.id == id }

	public fun getSortedPlantList(garden: Garden?): ArrayList<Plant>
	{
		if (MainApplication.isFailsafe())
		{
			return ArrayList()
		}

		synchronized(this.plants) {
			val list = garden?.let {
				ArrayList((garden?.plantIds ?: arrayListOf<String>()).map { id ->
					plants.firstOrNull { it.id == id }
				}).filterNotNull() as ArrayList
			} ?: plants

			return list
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
		if (MainApplication.isFailsafe()) return

		object : AsyncTask<Void?, Void?, Void?>()
		{
			override fun doInBackground(vararg params: Void?): Void?
			{
				plants.find { it.id == plant.id }?.let { plant ->
					val parents = plant.images?.mapNotNull { File(it).parentFile } ?: arrayListOf()

					// Delete images
					plant.images?.forEach { filePath ->
						File(filePath).delete()
					}

					parents.distinct().forEach {
						var children = (it.list() ?: arrayOf())
						if (children.firstOrNull()?.endsWith(".nomedia") == true) File(it, children.first()).delete()
						children = (it.list() ?: arrayOf())
						if (children.isEmpty()) it.delete()
					}

					// Remove plant
					plants.removeAll { it.id == plant.id }

					// Remove from shared prefs
//						val prefs = PreferenceManager.getDefaultSharedPreferences(context)
//						prefs.edit().remove(plantIndex.toString()).apply()
				}

				return null
			}

			override fun onPostExecute(aVoid: Void?)
			{
				callback?.callback()
			}
		}.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR)
	}

	public fun upsert(plant: Plant) = upsert(arrayListOf(plant))

	public fun upsert(plants: ArrayList<Plant>)
	{
		plants.forEach { plant ->
			var index = this.plants.indexOfFirst { it.id == plant.id }

			if (index < 0)
			{
				if (!MainApplication.isFailsafe())
				{
					this.plants.add(plant)
				}
			}
			else
			{
				this.plants[index] = plant
			}
		}

		save()
	}

	@JvmOverloads
	public fun load(fromRestore: Boolean = false): Boolean
	{
		if (MainApplication.isFailsafe())
		{
			return false
		}

		// redundancy check
		if (File(FILES_DIR, "/plants.$fileExt").lastModified() < File(FILES_DIR, "/plants.$fileExt.bak").lastModified())
		{
			FileManager.getInstance().copyFile("$FILES_DIR/plants.$fileExt.bak", "$FILES_DIR/plants.$fileExt")
		}

		if (FileManager.getInstance().fileExists("$FILES_DIR/plants.$fileExt"))
		{
			try
			{
				if (MainApplication.isEncrypted())
				{
					if (TextUtils.isEmpty(MainApplication.getKey()))
					{
						return false
					}

					val stream = DecryptInputStream(MainApplication.getKey(), File(FILES_DIR, "/plants.$fileExt"))

					plants.clear()
					plants.addAll(MoshiHelper.parse(stream, Types.newParameterizedType(ArrayList::class.java, Plant::class.java)))
					MainApplication.setFailsafe(false)
					MainApplication.isPanic = false
				}
				else
				{
					plants.clear()
					plants.addAll(MoshiHelper.parse(File(FILES_DIR, "/plants.$fileExt"), Types.newParameterizedType(ArrayList::class.java, Plant::class.java)))
					MainApplication.setFailsafe(false)
					MainApplication.isPanic = false
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
					MainApplication.isPanic = true
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
					MainApplication.isPanic = true
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

			if ((!ignoreCheck && plants.size > 0) || ignoreCheck)
			{
				saveTask.add(object : SaveAsyncTask(plants)
				{
					override fun doInBackground(vararg params: Void?): Int
					{
						FileManager.getInstance().copyFile("$FILES_DIR/plants.$fileExt", "$FILES_DIR/plants.$fileExt.bak")

						try
						{
							var outstream: OutputStream

							if (MainApplication.isEncrypted())
							{
								if (TextUtils.isEmpty(MainApplication.getKey()))
								{
									return 1
								}

								outstream = EncryptOutputStream(MainApplication.getKey(), File("$FILES_DIR/plants.$fileExt"))
							}
							else
							{
								outstream = FileOutputStream(File("$FILES_DIR/plants.$fileExt"))
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

						if (File("$FILES_DIR/plants.$fileExt").length() == 0L || !File("$FILES_DIR/plants.$fileExt").exists())
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

//						AddonHelper.broadcastPlantList(context)
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
				//Not sure if we need this any more
//				load()
//				val restart = Intent(context, BootActivity::class.java)
//				restart.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
//				context.startActivity(restart)
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

		@JvmStatic
		public fun isFileEncrypted(): Boolean = File(FILES_DIR, "plants.dat").exists() && !File(FILES_DIR, "plants.json").exists()
	}
}
