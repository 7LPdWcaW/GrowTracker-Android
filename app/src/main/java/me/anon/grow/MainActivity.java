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

import com.squareup.otto.Subscribe;

import java.util.ArrayList;

import lombok.Getter;
import lombok.experimental.Accessors;
import me.anon.grow.fragment.GardenDialogFragment;
import me.anon.grow.fragment.PlantListFragment;
import me.anon.lib.Views;
import me.anon.lib.event.GardenChangeEvent;
import me.anon.lib.helper.BusHelper;
import me.anon.lib.manager.GardenManager;
import me.anon.model.Garden;

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
	@Getter @Views.InjectView(R.id.navigation_view) private NavigationView navigation;
	private int selectedItem = 0;

	@Override protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		setContentView(R.layout.main_view);
		Views.inject(this);

		setSupportActionBar(toolbar);
		setNavigationView();
		showDrawerToggle();

		if (getFragmentManager().findFragmentByTag(TAG_FRAGMENT) == null)
		{
			navigation.getMenu().findItem(R.id.all).setChecked(true);
			onNavigationItemSelected(navigation.getMenu().findItem(R.id.all));
		}

		BusHelper.getInstance().register(this);
	}

	@Override protected void onDestroy()
	{
		super.onDestroy();
		BusHelper.getInstance().register(this);
	}

	public void setNavigationView()
	{
		navigation.getMenu().clear();
		navigation.setNavigationItemSelectedListener(this);
		navigation.inflateMenu(R.menu.navigation_drawer);

		ArrayList<Garden> gardens = GardenManager.getInstance().getGardens();
		for (int index = 0, gardensSize = gardens.size(); index < gardensSize; index++)
		{
			Garden garden = gardens.get(index);
			navigation.getMenu().add(R.id.gardens, 100 + index, 1, garden.getName()).setCheckable(true);
		}

		MenuItem item = navigation.getMenu().findItem(selectedItem);

		if (item != null)
		{
			item.setChecked(true);
		}
	}

	@Subscribe public void onGargenChangeEvent(GardenChangeEvent event)
	{
		setNavigationView();
	}

	public void showDrawerToggle()
	{
		if (drawer != null)
		{
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
		selectedItem = item.getItemId();

		if (item.getItemId() == R.id.website)
		{
			Intent view = new Intent(Intent.ACTION_VIEW);
			view.setData(Uri.parse("http://github.com/7lpdwcaw/GrowTracker-Android/"));
			startActivity(view);
		}
		else if (item.getItemId() == R.id.add)
		{
			GardenDialogFragment dialogFragment = new GardenDialogFragment();
			dialogFragment.setOnEditGardenListener(new GardenDialogFragment.OnEditGardenListener()
			{
				@Override public void onGardenEdited(Garden garden)
				{
					int index = 0;

					if (!GardenManager.getInstance().getGardens().contains(garden))
					{
						GardenManager.getInstance().insert(garden);
						index = GardenManager.getInstance().getGardens().size() - 1;
					}
					else
					{
						index = GardenManager.getInstance().getGardens().indexOf(garden);
						GardenManager.getInstance().getGardens().set(index, garden);
					}

					selectedItem = 100 + index;

					setNavigationView();
					onNavigationItemSelected(navigation.getMenu().findItem(selectedItem));
				}
			});
			dialogFragment.show(getFragmentManager(), null);
			item.setChecked(false);

			drawer.closeDrawers();

			return false;
		}
		else if (item.getItemId() == R.id.all)
		{
			getFragmentManager().beginTransaction().replace(R.id.fragment_holder, PlantListFragment.newInstance(null), TAG_FRAGMENT).commit();
		}
		else if (item.getItemId() >= 100 && item.getItemId() < Integer.MAX_VALUE)
		{
			int gardenIndex = item.getItemId() - 100;
			getFragmentManager().beginTransaction().replace(R.id.fragment_holder, PlantListFragment.newInstance(GardenManager.getInstance().getGardens().get(gardenIndex)), TAG_FRAGMENT).commit();
		}

		drawer.closeDrawers();

		return true;
	}
}
