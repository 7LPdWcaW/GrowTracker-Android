package me.anon.grow;

import android.app.Application;
import android.graphics.Bitmap;
import android.preference.PreferenceManager;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

import lombok.Getter;
import me.anon.lib.manager.PlantManager;

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
	@Getter private static boolean encrypted = false;

	@Override public void onCreate()
	{
		super.onCreate();

		encrypted = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("encrypt", false);

		PlantManager.getInstance().initialise(this);

		displayImageOptions = new DisplayImageOptions.Builder()
			.cacheInMemory(true)
			.cacheOnDisk(true)
			.showImageOnLoading(R.drawable.ic_image)
			.imageScaleType(ImageScaleType.EXACTLY_STRETCHED)
			.bitmapConfig(Bitmap.Config.RGB_565)
			.build();

		ImageLoader.getInstance().init(new ImageLoaderConfiguration.Builder(this)
			.threadPoolSize(6)
			.build());
	}
}
