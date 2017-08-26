package me.anon.grow;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.widget.Toast;

import java.util.ArrayList;

import me.anon.controller.provider.PlantWidgetProvider;
import me.anon.grow.fragment.PlantSelectDialogFragment;
import me.anon.lib.manager.PlantManager;

import static android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_ID;
import static android.appwidget.AppWidgetManager.INVALID_APPWIDGET_ID;

/**
 * // TODO: Add class description
 */
public class PlantSelectActivity extends BaseActivity
{
	private int appWidgetId = INVALID_APPWIDGET_ID;

	@Override protected void onCreate(@Nullable Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		setResult(RESULT_CANCELED);

		if (MainApplication.isEncrypted())
		{
			Toast.makeText(this, "Widget is not available when encrypt setting is enabled", Toast.LENGTH_LONG).show();
			finish();
			return;
		}

		Intent intent = getIntent();
		Bundle extras = intent.getExtras();
		if (extras != null)
		{
			appWidgetId = extras.getInt(EXTRA_APPWIDGET_ID, INVALID_APPWIDGET_ID);
		}

		if (appWidgetId == INVALID_APPWIDGET_ID)
		{
			finish();
		}

		PlantSelectDialogFragment dialogFragment = new PlantSelectDialogFragment();
		dialogFragment.setOnDialogActionListener(new PlantSelectDialogFragment.OnDialogActionListener()
		{
			@Override public void onDialogAccept(ArrayList<Integer> plantIndex, boolean showImage)
			{
				configureAndFinish(plantIndex.get(0), showImage);
			}
		});
		dialogFragment.show(getFragmentManager(), null);
	}

	private void configureAndFinish(int plantIndex, boolean allowImage)
	{
		Intent result = new Intent();
		result.putExtra(EXTRA_APPWIDGET_ID, appWidgetId);
		setResult(RESULT_OK, result);

		PreferenceManager.getDefaultSharedPreferences(PlantSelectActivity.this).edit()
			.putString("widget_" + appWidgetId, PlantManager.getInstance().getPlants().get(plantIndex).getId())
			.putBoolean("widget_" + appWidgetId + "_image", allowImage)
			.apply();

		PlantWidgetProvider.triggerUpdate(this, new int[]{appWidgetId});

		finish();
	}
}
