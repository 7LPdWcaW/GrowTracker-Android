package me.anon.grow.fragment;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Path;
import android.os.Bundle;
import android.text.Html;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.text.DateFormat;
import java.util.Date;

import androidx.fragment.app.FragmentActivity;
import me.anon.grow.R;
import me.anon.lib.DateRenderer;
import me.anon.lib.Views;
import me.anon.lib.helper.TimeHelper;
import me.anon.model.Action;
import me.anon.model.Plant;
import me.anon.model.StageChange;

@Views.Injectable
public class CompareImageLightboxDialog extends FragmentActivity
{
	private String[] imageUrls = {};
	private Plant plant;

	@Override protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.compare_image_lightbox);

		getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
		getWindow().setGravity(Gravity.CENTER);

		if (getIntent().getExtras() != null)
		{
			imageUrls = getIntent().getExtras().getStringArray("images");
			plant = getIntent().getParcelableExtra("plant");
		}
		else
		{
			finish();
			return;
		}

		Views.inject(this);

		for (String imageUrl : imageUrls)
		{
			createImageView(imageUrl);
		}
	}

	@Override protected void onSaveInstanceState(Bundle outState)
	{
		outState.putParcelable("plant", plant);
		super.onSaveInstanceState(outState);
	}

	private void createImageView(String imageUrl)
	{
		final View imageLayout = LayoutInflater.from(this).inflate(R.layout.clippable_image_lightbox_image, findViewById(R.id.compare_box), false);
		imageLayout.setTag(imageUrl);
		loadImage(imageLayout, imageUrl);

		try
		{
			DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(this);
			DateFormat timeFormat = android.text.format.DateFormat.getTimeFormat(this);
			String[] parts = imageUrl.split("/");
			String fileDate = parts[parts.length - 1].split("\\.")[0];
			Date date = new Date(Long.parseLong(fileDate));

			StageChange lastChange = null;
			long currentChangeDate = date.getTime();

			for (int index = plant.getActions().size() - 1; index >= 0; index--)
			{
				Action action = plant.getActions().get(index);
				if (action instanceof StageChange)
				{
					if (action.getDate() < date.getTime() && lastChange == null)
					{
						lastChange = (StageChange)action;
						break;
					}
				}
			}

			String stageDayStr = "";
			if (lastChange != null)
			{
				stageDayStr = " – ";
				int totalDays = (int)TimeHelper.toDays(Math.abs(date.getTime() - plant.getPlantDate()));
				stageDayStr += (totalDays == 0 ? 1 : totalDays);

				int currentDays = (int)TimeHelper.toDays(Math.abs(currentChangeDate - lastChange.getDate()));
				currentDays = (currentDays == 0 ? 1 : currentDays);
				stageDayStr += "/" + currentDays + getString(lastChange.getNewStage().getPrintString()).substring(0, 1).toLowerCase();
			}

			String dateStr = dateFormat.format(date) + " " + timeFormat.format(date);
			((TextView)imageLayout.findViewById(R.id.taken)).setText(Html.fromHtml("<b>" + getString(R.string.taken) + "</b>: " + dateStr + stageDayStr + " (" + getString(R.string.ago,  new DateRenderer().timeAgo(date.getTime()).formattedDate) + ")"));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		((ViewGroup)findViewById(R.id.compare_box)).addView(imageLayout);
	}

	private void loadImage(final View v, String imageUrl)
	{
		final SubsamplingScaleImageView imageView = (SubsamplingScaleImageView)v.findViewById(R.id.image);
		imageView.setDoubleTapZoomScale(1.5f);

		ImageLoader.getInstance().loadImage("file://" + imageUrl, new SimpleImageLoadingListener()
		{
			@Override public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage)
			{
				imageView.setImage(ImageSource.bitmap(loadedImage));
			}
		});
	}

	public static class ClippableSubsamplingScaleImageView extends SubsamplingScaleImageView
	{
		public ClippableSubsamplingScaleImageView(Context context, AttributeSet attr)
		{
			super(context, attr);
		}

		public ClippableSubsamplingScaleImageView(Context context)
		{
			super(context);
		}

		@Override protected void onDraw(Canvas canvas)
		{
			super.onDraw(canvas);
			Path clipPath = new Path();

			clipPath.addRect(0, 0, 300, 300, Path.Direction.CW);
			canvas.clipPath(clipPath);
		}
	}
}
