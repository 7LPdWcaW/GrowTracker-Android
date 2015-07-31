package me.anon.grow;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

import lombok.experimental.Accessors;
import me.anon.grow.fragment.PlantListFragment;
import me.anon.lib.Views;
import me.anon.lib.helper.GsonHelper;
import me.anon.lib.manager.PlantManager;

/**
 * // TODO: Add class description
 *
 * @author 7LPdWcaW
 * @documentation // TODO Reference flow doc
 * @project GrowTracker
 */
@Views.Injectable
@Accessors(prefix = {"m", ""}, chain = true)
public class MainActivity extends AppCompatActivity
{
	private static final String TAG_FRAGMENT = "current_fragment";

	@Override protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		setContentView(R.layout.fragment_holder);
		Views.inject(this);

		if (getFragmentManager().findFragmentByTag(TAG_FRAGMENT) == null)
		{
			getFragmentManager().beginTransaction().replace(R.id.fragment_holder, new PlantListFragment(), TAG_FRAGMENT).commit();
		}
	}

	@Override public boolean onCreateOptionsMenu(Menu menu)
	{
		menu.add(1, 1, 1, "Readme");
		menu.add(2, 2, 2, "Export data");
		menu.add(3, 3, 3, "Version 0.1");
		return super.onCreateOptionsMenu(menu);
	}

	@Override public boolean onOptionsItemSelected(MenuItem item)
	{
		if (item.getItemId() == 1)
		{
			String readme = "";

			try
			{
				InputStream stream = new BufferedInputStream(getAssets().open("readme.html"), 8192);
				int len = 0;
				byte[] buffer = new byte[8192];

				while ((len = stream.read(buffer)) != -1)
				{
					readme += new String(buffer, 0, len);
				}
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}

			new AlertDialog.Builder(this)
				.setMessage(Html.fromHtml(readme))
				.show();

			return true;
		}
		else if (item.getItemId() == 2)
		{
			String json = GsonHelper.parse(PlantManager.getInstance().getPlants());

			Intent share = new Intent(Intent.ACTION_SEND);
			share.putExtra(Intent.EXTRA_TEXT, json);
			share.setType("text/plain");
			startActivity(share);
		}

		return super.onOptionsItemSelected(item);
	}
}
