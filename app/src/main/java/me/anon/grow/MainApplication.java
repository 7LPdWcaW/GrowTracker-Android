package me.anon.grow;

import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.decode.BaseImageDecoder;
import com.nostra13.universalimageloader.core.decode.ImageDecodingInfo;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import me.anon.controller.receiver.BackupService;
import me.anon.lib.handler.ExceptionHandler;
import me.anon.lib.manager.FileManager;
import me.anon.lib.manager.GardenManager;
import me.anon.lib.manager.PlantManager;
import me.anon.lib.manager.ScheduleManager;
import me.anon.lib.stream.DecryptInputStream;

/**
 * // TODO: Add class description
 *
 * @author 7LPdWcaW
 * @documentation // TODO Reference flow doc
 * @project GrowTracker
 */
public class MainApplication extends Application
{
	private static DisplayImageOptions displayImageOptions;
	private static boolean encrypted = false;
	private static String key = "";
	private static boolean failsafe = false;
	private static boolean isTablet = false;
	public static boolean isPanic = false;

	public static void setEncrypted(boolean encrypted)
	{
		MainApplication.encrypted = encrypted;
	}

	public static void setKey(String key)
	{
		MainApplication.key = key;
	}

	public static void setFailsafe(boolean failsafe)
	{
		MainApplication.failsafe = failsafe;
	}

	public static void setIsTablet(boolean isTablet)
	{
		MainApplication.isTablet = isTablet;
	}

	public static DisplayImageOptions getDisplayImageOptions()
	{
		return displayImageOptions;
	}

	public static boolean isEncrypted()
	{
		return encrypted;
	}

	public static String getKey()
	{
		return key;
	}

	public static boolean isFailsafe()
	{
		return failsafe;
	}

	public static boolean isTablet()
	{
		return isTablet;
	}

	public static AtomicBoolean dataTaskRunning = new AtomicBoolean(false);
	private static Context context;
	public static SharedPreferences getDefaultPreferences()
	{
		return PreferenceManager.getDefaultSharedPreferences(context);
	}

	@Override public void onCreate()
	{
		super.onCreate();

		context = this;
		ExceptionHandler.getInstance().register(this);

		encrypted = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("encrypt", false) || PlantManager.isFileEncrypted();

		FileManager.IMAGE_PATH = PreferenceManager.getDefaultSharedPreferences(this).getString("image_location", "");
		if (TextUtils.isEmpty(FileManager.IMAGE_PATH)) FileManager.IMAGE_PATH = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getPath() + "/GrowTracker/";
		new File(FileManager.IMAGE_PATH).mkdir();

		isTablet = getResources().getBoolean(R.bool.is_tablet);

		PlantManager.getInstance().initialise(this);
		GardenManager.getInstance().initialise(this);
		ScheduleManager.instance.initialise(this);
		registerBackupService();

		displayImageOptions = new DisplayImageOptions.Builder()
			.cacheInMemory(true)
			.cacheOnDisk(true)
			.showImageOnLoading(R.drawable.ic_image)
			.showImageOnFail(R.drawable.default_plant)
			.imageScaleType(ImageScaleType.EXACTLY)
			.bitmapConfig(Bitmap.Config.RGB_565)
			.considerExifParams(true)
			.build();

		ImageLoader.getInstance().init(new ImageLoaderConfiguration.Builder(this)
			.threadPoolSize(6)
			.diskCacheExtraOptions(512, 512, null)

			.imageDecoder(new BaseImageDecoder(false)
			{
				@Override protected InputStream getImageStream(ImageDecodingInfo decodingInfo) throws IOException
				{
					if (encrypted)
					{
						try
						{
							return new DecryptInputStream(key, new File(new URI(decodingInfo.getOriginalImageUri())));
						}
						catch (URISyntaxException e)
						{
							//e.printStackTrace();
						}
					}

					return super.getImageStream(decodingInfo);
				}
			})
			.build());
	}

	public void registerBackupService()
	{
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

		if (prefs.getBoolean("auto_backup", false))
		{
			Intent backupIntent = new Intent(this, BackupService.class);

			AlarmManager alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
			alarmManager.cancel(PendingIntent.getBroadcast(this, 0, backupIntent, 0));
			alarmManager.setRepeating(AlarmManager.RTC, System.currentTimeMillis(), TimeUnit.DAYS.toMillis(1), PendingIntent.getBroadcast(this, 0, backupIntent, 0));
		}
	}
}
