package me.anon.lib.stream;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.net.Uri;

import com.davemorrissey.labs.subscaleview.decoder.DecoderFactory;
import com.davemorrissey.labs.subscaleview.decoder.ImageDecoder;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import me.anon.grow.MainApplication;

public class InputStreamImageDecoder implements ImageDecoder
{
	public static class Factory implements DecoderFactory<InputStreamImageDecoder>
	{
		private final Config bitmapConfig;

		public Factory()
		{
			this(null);
		}

		public Factory(Config bitmapConfig)
		{
			this.bitmapConfig = bitmapConfig;
		}

		@Override
		public InputStreamImageDecoder make() throws IllegalAccessException, InstantiationException
		{
			return new InputStreamImageDecoder(bitmapConfig);
		}
	}

	private final Config bitmapConfig;

	private InputStreamImageDecoder(Config bitmapConfig)
	{
		if (bitmapConfig == null)
		{
			this.bitmapConfig = Config.RGB_565;
		}
		else
		{
			this.bitmapConfig = bitmapConfig;
		}
	}

	@Override
	public Bitmap decode(Context context, Uri uri) throws Exception
	{
		InputStream inputStream;

		if (MainApplication.isEncrypted())
		{
			inputStream = new DecryptInputStream(MainApplication.getKey(), new File(uri.getPath()));
		}
		else
		{
			inputStream = new FileInputStream(new File(uri.getPath()));
		}

		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inPreferredConfig = bitmapConfig;
		Bitmap bitmap = BitmapFactory.decodeStream(inputStream, null, options);
		return bitmap;
	}
}
