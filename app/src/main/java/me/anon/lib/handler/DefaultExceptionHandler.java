package me.anon.lib.handler;

import com.google.gson.Gson;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.Random;

import me.anon.lib.manager.FileManager;
import me.anon.model.CrashReport;

public class DefaultExceptionHandler implements UncaughtExceptionHandler
{
	private UncaughtExceptionHandler defaultExceptionHandler;

	public DefaultExceptionHandler(UncaughtExceptionHandler pDefaultExceptionHandler)
	{
		defaultExceptionHandler = pDefaultExceptionHandler;
	}

	@Override public void uncaughtException(Thread t, Throwable e)
	{
		sendException(e);
		defaultExceptionHandler.uncaughtException(t, e);
	}

	public static void sendException(Throwable e)
	{
		sendException(e, "");
	}

	public static void sendException(Throwable e, String optionalMessage)
	{
		try
		{
			CrashReport report = new CrashReport();
			report.setException(e);
			report.setAdditionalMessage(optionalMessage);
			report.setModel(android.os.Build.MODEL);
			report.setManufacturer(android.os.Build.MANUFACTURER);
			report.setOsVersion(android.os.Build.VERSION.RELEASE);
			report.setTimestamp(System.currentTimeMillis());
			report.setPackageName(ExceptionHandler.PACKAGE_NAME);
			report.setVersion(ExceptionHandler.VERSION);
			report.setVersionCode(ExceptionHandler.VERSION_CODE);

			Random generator = new Random();
			int random = generator.nextInt(99999);
			String filename = Integer.toString(random) + ".stacktrace";

			FileManager.getInstance().writeFile(ExceptionHandler.getInstance().getFilesPath(), filename, new Gson().toJson(report));
		}
		catch (Exception ebos)
		{
			ebos.printStackTrace();
		}
	}
}
