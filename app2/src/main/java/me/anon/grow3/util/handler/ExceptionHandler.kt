package me.anon.grow3.util.handler

import android.content.Context
import java.io.File
import java.io.FilenameFilter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExceptionHandler @Inject constructor(context: Context)
{
	companion object
	{
		var VERSION = ""
		var VERSION_CODE = ""
		var PACKAGE_NAME = ""
	}

	private var exceptionHandler: DefaultExceptionHandler? = null
	public val filesPath: String = File(context.filesDir.absolutePath + "/crashreports/").apply {
		mkdirs()
	}.absolutePath

	init {
		val pm = context.packageManager
		try
		{
			val pi = pm.getPackageInfo(context.packageName, 0)
			PACKAGE_NAME = pi.packageName
			VERSION = pi.versionName.toString()
			VERSION_CODE = pi.versionCode.toString()
		}
		catch (e: Exception)
		{
			e.printStackTrace()
		}

		object : Thread()
		{
			override fun run()
			{
				val currentHandler = getDefaultUncaughtExceptionHandler()
				if (currentHandler !is DefaultExceptionHandler)
				{
					exceptionHandler = DefaultExceptionHandler()
					exceptionHandler!!.filesPath = filesPath
					exceptionHandler!!.handler = currentHandler
					setDefaultUncaughtExceptionHandler(exceptionHandler)
				}
			}
		}.start()
	}

	/**
	 * Search for stack trace files.
	 */
	public fun searchForStackTraces(): List<String>
	{
		val dir = File(filesPath)
		dir.mkdirs()
		val filter = FilenameFilter { _, name -> name.endsWith(".json") }
		return dir.list(filter)?.asList() ?: arrayListOf()
	}

	/**
	 * Forces a manual exception post
	 */
	public fun sendException(e: Exception)
	{
		exceptionHandler?.sendException(e, "CAUGHT EXCEPTION")
	}
}
