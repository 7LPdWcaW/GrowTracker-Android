package me.anon.grow;

import android.app.Application;
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

import lombok.Getter;
import lombok.Setter;
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
}
