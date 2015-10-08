package me.anon.grow;

import android.app.Application;
import android.graphics.Bitmap;

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

	@Override public void onCreate()
	{
		super.onCreate();

		PlantManager.getInstance().initialise(this);

		displayImageOptions = new DisplayImageOptions.Builder()
			.cacheInMemory(true)
			.cacheOnDisk(true)
			.showImageOnLoading(R.drawable.ic_image)
			.resetViewBeforeLoading(true)
			.imageScaleType(ImageScaleType.EXACTLY_STRETCHED)
			.bitmapConfig(Bitmap.Config.RGB_565)
			.build();

		ImageLoader.getInstance().init(new ImageLoaderConfiguration.Builder(this)
			.memoryCacheExtraOptions(512, 512)
			.threadPoolSize(6)
			.build());
	}
}
