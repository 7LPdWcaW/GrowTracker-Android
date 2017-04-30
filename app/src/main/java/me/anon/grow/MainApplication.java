package me.anon.grow;

import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.preference.PreferenceManager;

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

import lombok.Getter;
import lombok.Setter;
import me.anon.controller.receiver.BackupService;
import me.anon.lib.handler.ExceptionHandler;
import me.anon.lib.manager.GardenManager;
import me.anon.lib.manager.PlantManager;
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
	@Getter private static DisplayImageOptions displayImageOptions;
	@Getter @Setter private static boolean encrypted = false;
	@Getter @Setter private static String key = "";
	@Getter @Setter private static boolean failsafe = false;
	@Getter @Setter private static boolean isTablet = false;

	@Override public void onCreate()
	{
		super.onCreate();

		ExceptionHandler.getInstance().register(this);

		encrypted = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("encrypt", false);
		isTablet = getResources().getBoolean(R.bool.is_tablet);

		PlantManager.getInstance().initialise(this);
		GardenManager.getInstance().initialise(this);
		registerBackupService();

		displayImageOptions = new DisplayImageOptions.Builder()
			.cacheInMemory(true)
			.cacheOnDisk(true)
			.showImageOnLoading(R.drawable.ic_image)
			.showImageOnFail(R.drawable.default_plant)
			.imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2)
			.bitmapConfig(Bitmap.Config.RGB_565)
			.build();

		ImageLoader.getInstance().init(new ImageLoaderConfiguration.Builder(this)
			.threadPoolSize(6)
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
							e.printStackTrace();
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
