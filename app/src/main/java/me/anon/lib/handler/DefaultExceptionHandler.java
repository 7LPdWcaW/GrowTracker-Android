package me.anon.lib.handler;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.Random;

import me.anon.lib.helper.MoshiHelper;
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
			StringWriter strWriter = new StringWriter();
			PrintWriter writer = new PrintWriter(strWriter);
			e.printStackTrace(writer);

			CrashReport report = new CrashReport();
			report.setException(e);
			report.setAdditionalMessage(strWriter.toString() + "\r\n" + optionalMessage);
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

			BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(ExceptionHandler.getInstance().getFilesPath() + "/" + filename));
			bufferedWriter.write(MoshiHelper.toJson(report, CrashReport.class));
			bufferedWriter.close();
		}
		catch (Exception ebos)
		{
			ebos.printStackTrace();
		}
	}
}
