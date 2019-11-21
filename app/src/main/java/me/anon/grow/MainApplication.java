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

import com.nostra13.universalimageloader.cache.disc.DiskCache;
import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiskCache;
import com.nostra13.universalimageloader.cache.disc.naming.FileNameGenerator;
import com.nostra13.universalimageloader.core.DefaultConfigurationFactory;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.decode.BaseImageDecoder;
import com.nostra13.universalimageloader.core.decode.ImageDecodingInfo;
import com.nostra13.universalimageloader.utils.IoUtils;
import com.nostra13.universalimageloader.utils.StorageUtils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
import me.anon.lib.stream.EncryptOutputStream;

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

		PlantManager.getInstance().initialise(this);
		encrypted = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("encrypt", false) || PlantManager.isFileEncrypted();

		FileManager.IMAGE_PATH = PreferenceManager.getDefaultSharedPreferences(this).getString("image_location", "");
		if (TextUtils.isEmpty(FileManager.IMAGE_PATH)) FileManager.IMAGE_PATH = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getPath() + "/GrowTracker/";
		new File(FileManager.IMAGE_PATH).mkdir();

		isTablet = getResources().getBoolean(R.bool.is_tablet);

		PlantManager.getInstance().load();
		GardenManager.getInstance().initialise(this);
		ScheduleManager.instance.initialise(this);
		registerBackupService();

		displayImageOptions = new DisplayImageOptions.Builder()
			//.cacheInMemory(true)
			.cacheOnDisk(true)
			.showImageOnLoading(R.drawable.ic_image)
			.showImageOnFail(R.drawable.default_plant)
			.bitmapConfig(Bitmap.Config.RGB_565)
			.considerExifParams(true)
			.build();

		final FileNameGenerator fileNameGenerator = DefaultConfigurationFactory.createFileNameGenerator();
		final DiskCache diskCache = new UnlimitedDiskCache(StorageUtils.getCacheDirectory(context), getExternalCacheDir(), fileNameGenerator);
		((UnlimitedDiskCache)diskCache).setCompressFormat(Bitmap.CompressFormat.JPEG);
		((UnlimitedDiskCache)diskCache).setCompressQuality(85);
		ImageLoader.getInstance().init(new ImageLoaderConfiguration.Builder(this)
			.threadPoolSize(6)
			.diskCache(new DiskCache()
			{
				@Override public File getDirectory()
				{
					return diskCache.getDirectory();
				}

				@Override public File get(String imageUri)
				{
					if (isEncrypted()) return null;
					else return diskCache.get(imageUri);
				}

				@Override public boolean save(String imageUri, InputStream imageStream, IoUtils.CopyListener listener) throws IOException
				{
					if (encrypted)
					{
						File imageFile = getFile(imageUri);
						File tmpFile = new File(imageFile.getAbsolutePath() + ".tmp");
						boolean loaded = imageFile.exists();

						if (!loaded)
						{
							try
							{
								OutputStream os = new BufferedOutputStream(new EncryptOutputStream(key, tmpFile), 32 * 1024);
								try
								{
									loaded = IoUtils.copyStream(imageStream, os, listener, 32 * 1024);
								}
								finally
								{
									IoUtils.closeSilently(os);
								}
							}
							finally
							{
								if (loaded && !tmpFile.renameTo(imageFile))
								{
									loaded = false;
								}
								if (!loaded)
								{
									tmpFile.delete();
								}
							}
						}

						return loaded;
					}
					else
					{
						return diskCache.save(imageUri, imageStream, listener);
					}
				}

				@Override public boolean save(String imageUri, Bitmap bitmap) throws IOException
				{
					if (encrypted)
					{
						File imageFile = getFile(imageUri);
						File tmpFile = new File(imageFile.getAbsolutePath() + ".tmp");
						OutputStream os = new BufferedOutputStream(new EncryptOutputStream(key, tmpFile), 32 * 1024);
						boolean savedSuccessfully = false;
						try
						{
							savedSuccessfully = bitmap.compress(Bitmap.CompressFormat.JPEG, 85, os);
						}
						finally
						{
							IoUtils.closeSilently(os);
							if (savedSuccessfully && !tmpFile.renameTo(imageFile))
							{
								savedSuccessfully = false;
							}
							if (!savedSuccessfully)
							{
								tmpFile.delete();
							}
						}

						bitmap.recycle();
						return savedSuccessfully;
					}
					else
					{
						return diskCache.save(imageUri, bitmap);
					}
				}

				@Override public boolean remove(String imageUri)
				{
					return diskCache.remove(imageUri);
				}

				@Override public void close()
				{
					diskCache.close();
				}

				@Override public void clear()
				{
					diskCache.clear();
				}

				/**
				 * Returns file object (not null) for incoming image URI. File object can reference to non-existing file.
				 */
				protected File getFile(String imageUri)
				{
					String fileName = fileNameGenerator.generate(imageUri);
					File dir = StorageUtils.getCacheDirectory(context);
					File dir2 = StorageUtils.getIndividualCacheDirectory(context);
					if (!dir.exists() && !dir.mkdirs())
					{
						if (dir2 != null && (dir2.exists() || dir2.mkdirs()))
						{
							dir = dir2;
						}
					}

					return new File(dir, fileName);
				}
			})
			.diskCacheExtraOptions(768, 768, null)
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
