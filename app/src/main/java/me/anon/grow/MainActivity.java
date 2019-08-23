package me.anon.grow;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.navigation.NavigationView;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.drawerlayout.widget.DrawerLayout;
import me.anon.grow.fragment.GardenDialogFragment;
import me.anon.grow.fragment.GardenFragment;
import me.anon.grow.fragment.PlantListFragment;
import me.anon.lib.Views;
import me.anon.lib.event.GardenChangeEvent;
import me.anon.lib.helper.BusHelper;
import me.anon.lib.helper.PermissionHelper;
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
public class MainActivity extends BaseActivity implements NavigationView.OnNavigationItemSelectedListener
{
	private static final String TAG_FRAGMENT = "current_fragment";

	@Views.InjectView(R.id.toolbar) private MaterialToolbar toolbar;
	@Views.InjectView(R.id.toolbar_layout) public AppBarLayout toolbarLayout;
	@Nullable @Views.InjectView(R.id.drawer_layout) private DrawerLayout drawer;
	@Views.InjectView(R.id.navigation_view) private NavigationView navigation;
	private int selectedItem = 0;

	public NavigationView getNavigation()
	{
		return navigation;
	}

	@Override protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		if (!PermissionHelper.hasPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE))
		{
			PermissionHelper.doPermissionCheck(this, Manifest.permission.WRITE_EXTERNAL_STORAGE, 1, getString(R.string.permission_summary));
		}

		setContentView(R.layout.main_view);
		Views.inject(this);

		setSupportActionBar(toolbar);
		setNavigationView();
		showUpdateDialog();

		if (savedInstanceState == null)
		{
			int defaultGarden = PreferenceManager.getDefaultSharedPreferences(this).getInt("default_garden", -1);

			if (defaultGarden == -1)
			{
				navigation.getMenu().findItem(R.id.all).setChecked(true);
				onNavigationItemSelected(navigation.getMenu().findItem(R.id.all));
			}
			else
			{
				MenuItem item = navigation.getMenu().findItem(100 + defaultGarden);

				if (item != null)
				{
					item.setChecked(true);
					onNavigationItemSelected(item);
				}
			}
		}
		else
		{
			selectedItem = savedInstanceState.getInt("index");

			if (navigation.getMenu().findItem(selectedItem).isCheckable())
			{
				navigation.getMenu().findItem(selectedItem).setChecked(true);
			}
		}

		BusHelper.getInstance().register(this);
	}

	@Override protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);
		getSupportFragmentManager().findFragmentById(R.id.fragment_holder).onActivityResult(requestCode, resultCode, data);
	}

	@Override protected void onResume()
	{
		super.onResume();
		showDrawerToggle();
	}

	@Override protected void onSaveInstanceState(Bundle outState)
	{
		outState.putInt("index", selectedItem);
		super.onSaveInstanceState(outState);
	}

	@Override protected void onDestroy()
	{
		super.onDestroy();
		BusHelper.getInstance().register(this);
	}

	public void showUpdateDialog()
	{
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		int lastVersion = prefs.getInt("last_version", -1);
		if (lastVersion != BuildConfig.VERSION_CODE && lastVersion != -1)
		{
			new AlertDialog.Builder(this)
				.setTitle(R.string.update_dialog_title)
				.setMessage(getString(R.string.update_dialog_message, BuildConfig.VERSION_NAME))
				.setPositiveButton(R.string.update_dialog_view_changes_button, new DialogInterface.OnClickListener()
				{
					@Override public void onClick(DialogInterface dialog, int which)
					{
						Intent intent = new Intent(Intent.ACTION_VIEW);
						intent.setData(Uri.parse("https://github.com/7LPdWcaW/GrowTracker-Android/releases/tag/v" + BuildConfig.VERSION_NAME));
						startActivity(intent);
					}
				})
				.setNegativeButton(R.string.update_dialog_dismiss_button, null)
				.show();
		}

		prefs.edit().putInt("last_version", BuildConfig.VERSION_CODE).apply();
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
			navigation.getMenu().findItem(R.id.garden_menu).getSubMenu().add(R.id.garden_menu, 100 + index, 1, garden.getName()).setCheckable(true);
		}

		MenuItem item = navigation.getMenu().findItem(selectedItem);

		if (item != null)
		{
			item.setChecked(true);
		}

		try
		{
			navigation.getMenu().findItem(R.id.version).setTitle(getString(R.string.version, getPackageManager().getPackageInfo(getPackageName(), 0).versionName));
		}
		catch (PackageManager.NameNotFoundException e)
		{
			e.printStackTrace();
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
		menu.add(1, 1, 1, R.string.menu_settings);

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
			view.setData(Uri.parse("http://github.com/7lpdwcaw/"));
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

					MenuItem item = navigation.getMenu().findItem(selectedItem);

					if (item != null)
					{
						item.setChecked(false);
					}

					selectedItem = 100 + index;

					setNavigationView();
					onNavigationItemSelected(navigation.getMenu().findItem(selectedItem));
				}
			});
			dialogFragment.show(getSupportFragmentManager(), null);
			item.setChecked(false);

			if (drawer != null)
			{
				drawer.closeDrawers();
			}

			return false;
		}
		else if (item.getItemId() == R.id.settings)
		{
			item.setChecked(false);

			Intent settings = new Intent(this, SettingsActivity.class);
			startActivity(settings);
		}
		else if (item.getItemId() == R.id.feeding_schedule)
		{
			Intent schedule = new Intent(this, FeedingScheduleActivity.class);
			startActivity(schedule);
		}
		else if (item.getItemId() == R.id.all)
		{
			selectedItem = item.getItemId();
			getSupportFragmentManager().beginTransaction().replace(R.id.fragment_holder, PlantListFragment.newInstance(), TAG_FRAGMENT).commit();
		}
		else if (item.getItemId() >= 100 && item.getItemId() < Integer.MAX_VALUE)
		{
			navigation.getMenu().findItem(R.id.garden_menu).getSubMenu().findItem(R.id.all).setChecked(false);

			MenuItem selected = navigation.getMenu().findItem(selectedItem);
			if (selected != null)
			{
				selected.setChecked(false);
			}

			selectedItem = item.getItemId();
			item.setChecked(true);
			int gardenIndex = item.getItemId() - 100;
			getSupportFragmentManager().beginTransaction().replace(R.id.fragment_holder, GardenFragment.newInstance(GardenManager.getInstance().getGardens().get(gardenIndex)), TAG_FRAGMENT).commit();
		}

		if (drawer != null)
		{
			drawer.closeDrawers();
		}

		return true;
	}
}
