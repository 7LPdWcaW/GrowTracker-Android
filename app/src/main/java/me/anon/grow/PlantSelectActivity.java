package me.anon.grow;

import android.app.AlertDialog;
import android.appwidget.AppWidgetManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import java.util.ArrayList;

import me.anon.controller.provider.PlantWidgetProvider;
import me.anon.lib.manager.PlantManager;
import me.anon.model.Plant;

import static android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_ID;
import static android.appwidget.AppWidgetManager.INVALID_APPWIDGET_ID;

/**
 * // TODO: Add class description
 *
 * @author 
 */
public class PlantSelectActivity extends AppCompatActivity
{
	private int appWidgetId = INVALID_APPWIDGET_ID;

	@Override protected void onCreate(@Nullable Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		setResult(RESULT_CANCELED);

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

		ArrayList<String> plantNames = new ArrayList<>();
		for (Plant plant : PlantManager.getInstance().getPlants())
		{
			plantNames.add(plant.getName());
		}

		new AlertDialog.Builder(this)
			.setTitle("Select plant")
			.setItems(plantNames.toArray(new String[plantNames.size()]), new DialogInterface.OnClickListener()
			{
				@Override public void onClick(DialogInterface dialog, int which)
				{
					final int plantIndex = which;
					if (!BuildConfig.DISCRETE)
					{
						new AlertDialog.Builder(PlantSelectActivity.this)
							.setTitle("Allow images?")
							.setMessage("Allow last taken image to show in widget?")
							.setPositiveButton("Yes", new DialogInterface.OnClickListener()
							{
								@Override public void onClick(DialogInterface dialog, int which)
								{

									configureAndFinish(plantIndex, true);
								}
							})
							.setNegativeButton("No", new DialogInterface.OnClickListener()
							{
								@Override public void onClick(DialogInterface dialog, int which)
								{
									configureAndFinish(plantIndex, false);
								}
							})
							.show();
					}
					else
					{
						configureAndFinish(plantIndex, false);
					}
				}
			})
			.show();
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

		Intent intent = new Intent(PlantSelectActivity.this, PlantWidgetProvider.class);
		intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
		int[] ids = {appWidgetId};
		intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
		sendBroadcast(intent);

		finish();
	}
}
