package me.anon.controller.provider;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.appwidget.AppWidgetProviderInfo;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.media.ThumbnailUtils;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Html;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.View;
import android.widget.RemoteViews;

import me.anon.grow.PlantDetailsActivity;
import me.anon.grow.R;
import me.anon.lib.manager.PlantManager;
import me.anon.model.Plant;

/**
 * // TODO: Add class description
 *
 * @author 
 */
public class PlantWidgetProvider extends AppWidgetProvider
{
	private Context context;

	@Override public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions)
	{
		super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);
		onUpdate(context, appWidgetManager, new int[]{appWidgetId});
	}

	@Override public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds)
	{
		final int count = appWidgetIds.length;
		this.context = context;

		for (int widgetIndex = 0; widgetIndex < count; widgetIndex++)
		{
			int widgetId = appWidgetIds[widgetIndex];
			String plantId = PreferenceManager.getDefaultSharedPreferences(context).getString("widget_" + widgetId, "");

			if (!TextUtils.isEmpty(plantId))
			{
				int plantIndex = -1;
				for (int index = 0, plantsSize = PlantManager.getInstance().getPlants().size(); index < plantsSize; index++)
				{
					if (PlantManager.getInstance().getPlants().get(index).getId().equalsIgnoreCase(plantId))
					{
						plantIndex = index;
						break;
					}
				}

				if (plantIndex > -1)
				{
					Plant plant = PlantManager.getInstance().getPlants().get(plantIndex);

					RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_plant_item);
					setWidgetUi(widgetId, remoteViews, plant);

					Intent intent = new Intent(context, PlantDetailsActivity.class);
					intent.putExtra("plant_index", plantIndex);
					PendingIntent plantIntent = PendingIntent.getActivity(context, 0, intent, 0);
					remoteViews.setOnClickPendingIntent(R.id.card_view, plantIntent);

					appWidgetManager.updateAppWidget(widgetId, remoteViews);
				}
			}
		}
	}

	/**
	 * Sets the widget UI
	 *
	 * @param widgetId The widget ID
	 * @param view The view to populate
	 * @param plant The plant data to use
	 */
	private void setWidgetUi(int widgetId, RemoteViews view, Plant plant)
	{
		boolean allowImage = PreferenceManager.getDefaultSharedPreferences(context).getBoolean("widget_" + widgetId + "_image", false);
		view.setTextViewText(R.id.name, plant.getName());

		int[] size = getWidgetSize(widgetId);

		if (size[1] <= 100)
		{
			view.setTextViewText(R.id.summary, Html.fromHtml("<b>" + plant.getName() + "</b> " + plant.generateShortSummary(context)));
			view.setViewVisibility(R.id.name, View.GONE);
			view.setTextViewTextSize(R.id.summary, TypedValue.COMPLEX_UNIT_DIP, 12);
		}
		else
		{
			view.setTextViewText(R.id.summary, Html.fromHtml(plant.generateLongSummary(context)));
			view.setViewVisibility(R.id.name, View.VISIBLE);
			view.setTextViewTextSize(R.id.name, TypedValue.COMPLEX_UNIT_DIP, 16);
			view.setTextViewTextSize(R.id.summary, TypedValue.COMPLEX_UNIT_DIP, 14);
		}

		if (plant.getImages().size() > 0 && allowImage)
		{
			if (size[0] > 0 && size[1] > 0)
			{
				BitmapFactory.Options opts = new BitmapFactory.Options();
				opts.inSampleSize = recursiveSample(plant.getImages().get(plant.getImages().size() - 1), size[0], size[1]);
				Bitmap lastImage = BitmapFactory.decodeFile(plant.getImages().get(plant.getImages().size() - 1), opts);

				Bitmap output = Bitmap.createBitmap(size[0], size[1], Bitmap.Config.ARGB_8888);
				Canvas canvas = new Canvas(output);

				lastImage = ThumbnailUtils.extractThumbnail(lastImage, size[0], size[1], 0);

				Rect rect = new Rect(0, 0, output.getWidth(), output.getHeight());
				RectF rectF = new RectF(rect);

				Paint paint = new Paint();
				paint.setColor(0xff000000);
				paint.setAntiAlias(true);
				canvas.drawRoundRect(rectF, 8, 8, paint);

				paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
				canvas.drawBitmap(lastImage, rect, rect, paint);

				Paint overlay = new Paint();
				overlay.setColor(0x000000);
				overlay.setAlpha(0x7F);
				overlay.setAntiAlias(true);
				canvas.drawRoundRect(rectF, 8, 8, overlay);

				view.setImageViewBitmap(R.id.image, output);
			}
		}
	}

	/**
	 * Get the size of the target widget
	 *
	 * @param appWidgetId The id of the widget
	 *
	 * @return An array of WxH size
	 */
	private int[] getWidgetSize(int appWidgetId)
	{
		boolean isPortraitOrientation = context.getResources().getBoolean(R.bool.is_portrait);
		AppWidgetProviderInfo providerInfo = AppWidgetManager.getInstance(context).getAppWidgetInfo(appWidgetId);

		int widgetLandWidth = providerInfo.minWidth;
		int widgetPortHeight = providerInfo.minHeight;
		int widgetPortWidth = providerInfo.minWidth;
		int widgetLandHeight = providerInfo.minHeight;

		Bundle appWidgetOptions = AppWidgetManager.getInstance(context).getAppWidgetOptions(appWidgetId);
		if (appWidgetOptions != null && appWidgetOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH) > 0)
		{
			widgetPortWidth = appWidgetOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH);
			widgetLandWidth = appWidgetOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH);
			widgetLandHeight = appWidgetOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT);
			widgetPortHeight = appWidgetOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT);
		}
		else
		{
			widgetLandWidth = providerInfo.minWidth;
			widgetPortHeight = providerInfo.minHeight;
			widgetPortWidth = providerInfo.minWidth;
			widgetLandHeight = providerInfo.minHeight;
		}

		// If device is in port oriantation, use port sizes
		int widgetWidthPerOrientation = widgetPortWidth;
		int widgetHeightPerOrientation = widgetPortHeight;

		if (!isPortraitOrientation)
		{
			// Not Portrait, so use landscape sizes
			widgetWidthPerOrientation = widgetLandWidth;
			widgetHeightPerOrientation = widgetLandHeight;
		}

		return new int[]{widgetWidthPerOrientation, widgetHeightPerOrientation};
	}

	/**
	 * Recursivly samples an image to below or equal the max width/height
	 * @param path The path to the image
	 * @param maxWidth The maximum width the image can be
	 * @param maxHeight The maximum height the image can be
	 * @return The scale size of the image to use with {@link BitmapFactory.Options()}
	 */
	private int recursiveSample(String path, int maxWidth, int maxHeight)
	{
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(path, options);

		int scale = 1;
		int imageWidth = options.outWidth;
		int imageHeight = options.outHeight;
		int maxDimension = maxWidth > maxHeight ? maxWidth : maxHeight;
		boolean isMaxWidth = maxWidth > maxHeight;

		while ((isMaxWidth ? imageWidth : imageHeight) / 2 >= maxDimension)
		{
			imageWidth /= 2;
			imageHeight /= 2;
			scale *= 2;
		}

		if (scale < 1)
		{
			scale = 1;
		}

		return scale;
	}
}
