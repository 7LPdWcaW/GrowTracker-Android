package me.anon.grow3.util.handler

import android.os.Build
import android.util.JsonWriter
import java.io.BufferedWriter
import java.io.FileWriter
import java.io.PrintWriter
import java.io.StringWriter
import javax.inject.Singleton

@Singleton
class DefaultExceptionHandler : Thread.UncaughtExceptionHandler
{
	public var filesPath: String = ""
	public var handler: Thread.UncaughtExceptionHandler? = null

	override fun uncaughtException(t: Thread, e: Throwable)
	{
		sendException(e as Exception)
		handler?.uncaughtException(t, e)
	}

	public fun sendException(e: Exception, optionalMessage: String? = "")
	{
		try
		{
			val strWriter = StringWriter()
			val writer = PrintWriter(strWriter)
			e.printStackTrace(writer)

			val filename = "${System.currentTimeMillis()}.json"
			val bufferedWriter = BufferedWriter(FileWriter("$filesPath/$filename"))
			val report = JsonWriter(bufferedWriter)
			report.beginObject()
			report.name("stacktrace").value(strWriter.toString())
			report.name("message").value(optionalMessage)
			report.name("model").value(Build.MODEL)
			report.name("manufacturer").value(Build.MANUFACTURER)
			report.name("osVersion").value(Build.VERSION.RELEASE)
			report.name("timestamp").value(System.currentTimeMillis())
			report.name("packageName").value(ExceptionHandler.PACKAGE_NAME)
			report.name("version").value(ExceptionHandler.VERSION)
			report.name("versionCode").value(ExceptionHandler.VERSION_CODE)
			report.endObject()

			report.flush()
			report.close()
		}
		catch (e: Exception)
		{
			e.printStackTrace()
		}
	}
}
