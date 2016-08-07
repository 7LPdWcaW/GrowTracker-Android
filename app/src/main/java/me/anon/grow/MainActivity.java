package me.anon.grow;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import lombok.experimental.Accessors;
import me.anon.grow.fragment.PlantListFragment;
import me.anon.lib.Views;

/**
 * // TODO: Add class description
 *
 * @author 7LPdWcaW
 * @documentation // TODO Reference flow doc
 * @project GrowTracker
 */
@Views.Injectable
@Accessors(prefix = {"m", ""}, chain = true)
public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener
{
	private static final String TAG_FRAGMENT = "current_fragment";

	@Views.InjectView(R.id.toolbar) private Toolbar toolbar;
	@Views.InjectView(R.id.drawer_layout) private DrawerLayout drawer;
	@Views.InjectView(R.id.navigation_view) private NavigationView navigation;

	@Override protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		setContentView(R.layout.fragment_holder);
		Views.inject(this);

		setSupportActionBar(toolbar);
		showDrawerToggle();

		if (getFragmentManager().findFragmentByTag(TAG_FRAGMENT) == null)
		{
			getFragmentManager().beginTransaction().replace(R.id.fragment_holder, new PlantListFragment(), TAG_FRAGMENT).commit();
		}
	}

	public void showDrawerToggle()
	{
		if (drawer != null)
		{
			navigation.setNavigationItemSelectedListener(this);
			navigation.getMenu()
				.add(R.id.gardens, Menu.NONE, 0, "Garden #1");

			ActionBarDrawerToggle drawerToggle = new ActionBarDrawerToggle(this, drawer, toolbar, 0, 0)
			{
				@Override public void onDrawerSlide(View drawerView, float slideOffset)
				{
					super.onDrawerSlide(drawerView, slideOffset);

					if (getCurrentFocus() != null)
					{
						InputMethodManager inputMethodManager = (InputMethodManager)MainActivity.this.getSystemService(Activity.INPUT_METHOD_SERVICE);
						inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
					}
				}
			};

			drawer.setDrawerListener(drawerToggle);
			drawerToggle.syncState();
		}
	}

	@Override public boolean onCreateOptionsMenu(Menu menu)
	{
		menu.add(1, 1, 1, "Settings");

		return super.onCreateOptionsMenu(menu);
	}

	@Override public boolean onOptionsItemSelected(MenuItem item)
	{
		if (item.getItemId() == 1)
		{
			Intent settings = new Intent(this, SettingsActivity.class);
			startActivity(settings);
		}

		return super.onOptionsItemSelected(item);
	}

	@Override public boolean onNavigationItemSelected(MenuItem item)
	{
		if (item.getItemId() == R.id.website)
		{
			Intent view = new Intent(Intent.ACTION_VIEW);
			view.setData(Uri.parse("http://github.com/7lpdwcaw/GrowTracker-Android/"));
			startActivity(view);
		}
		else if (item.getItemId() == R.id.add)
		{

		}

		return true;
	}
}
