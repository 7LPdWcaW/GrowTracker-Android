package me.anon.lib.handler;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Environment;

import java.io.File;
import java.io.FilenameFilter;

public class ExceptionHandler
{
	public static String VERSION = "";
	public static String VERSION_CODE = "";
	public static String PACKAGE_NAME = "";

	private String filesPath = "/";
	private String[] stackTraceFileList = null;

	private static ExceptionHandler instance;

	public static ExceptionHandler getInstance()
	{
		if (instance == null)
		{
			synchronized (ExceptionHandler.class)
			{
				if (instance == null)
				{
					instance = new ExceptionHandler();
				}
			}
		}

		return instance;
	}

	public String getFilesPath()
	{
		return filesPath;
	}

	/**
	 * @param context
	 */
	public void register(Context context)
	{
		PackageManager pm = context.getPackageManager();

		try
		{
			PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);
			PACKAGE_NAME = pi.packageName;
			VERSION = String.valueOf(pi.versionName);
			VERSION_CODE = String.valueOf(pi.versionCode);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		filesPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getPath() + "/GrowTracker/crashes/";
		new File(filesPath).mkdirs();

		new Thread()
		{
			@Override public void run()
			{
				UncaughtExceptionHandler currentHandler = Thread.getDefaultUncaughtExceptionHandler();

				// don't register again if already registered
				if (!(currentHandler instanceof DefaultExceptionHandler))
				{
					Thread.setDefaultUncaughtExceptionHandler(new DefaultExceptionHandler(currentHandler));
				}
			}
		}.start();
	}

	/**
	 * Search for stack trace files.
	 *
	 * @return
	 */
	public String[] searchForStackTraces()
	{
		if (stackTraceFileList != null)
		{
			return stackTraceFileList;
		}

		File dir = new File(filesPath);

		// Try to create the files folder if it doesn't exist
		dir.mkdir();

		FilenameFilter filter = new FilenameFilter()
		{
			@Override public boolean accept(File dir, String name)
			{
				return name.endsWith(".stacktrace");
			}
		};

		return (stackTraceFileList = dir.list(filter));
	}

	/**
	 * Forces a manual exception post
	 * @param e
	 */
	public void sendException(Exception e)
	{
		DefaultExceptionHandler.sendException(e, "CAUGHT EXCEPTION");
	}
}
