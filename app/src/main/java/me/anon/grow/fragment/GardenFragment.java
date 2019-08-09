package me.anon.grow.fragment;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.esotericsoftware.kryo.Kryo;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import me.anon.controller.adapter.PlantAdapter;
import me.anon.controller.adapter.SimpleItemTouchHelperCallback;
import me.anon.grow.AddPlantActivity;
import me.anon.grow.AddWateringActivity;
import me.anon.grow.MainActivity;
import me.anon.grow.R;
import me.anon.grow.service.ExportService;
import me.anon.lib.SnackBar;
import me.anon.lib.SnackBarListener;
import me.anon.lib.Views;
import me.anon.lib.event.GardenChangeEvent;
import me.anon.lib.helper.BusHelper;
import me.anon.lib.helper.FabAnimator;
import me.anon.lib.helper.NotificationHelper;
import me.anon.lib.manager.GardenManager;
import me.anon.lib.manager.PlantManager;
import me.anon.model.EmptyAction;
import me.anon.model.Garden;
import me.anon.model.NoteAction;
import me.anon.model.Plant;
import me.anon.model.PlantStage;

@Views.Injectable
public class GardenFragment extends Fragment
{
	private PlantAdapter adapter;
	private Garden garden;

	public static GardenFragment newInstance(@Nullable Garden garden)
	{
		GardenFragment fragment = new GardenFragment();
		fragment.garden = garden;

		return fragment;
	}

	@Views.InjectView(R.id.name) private TextView name;
	@Views.InjectView(R.id.recycler_view) private RecyclerView recycler;
	@Views.InjectView(R.id.fab_add) private FloatingActionButton fab;

	private ArrayList<PlantStage> filterList = new ArrayList<>();
	private boolean hideHarvested = false;

	@Override public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		setHasOptionsMenu(true);
	}

	@Nullable @Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View view = inflater.inflate(R.layout.garden_view, container, false);
		Views.inject(this, view);

		return view;
	}

	@Override public void onActivityCreated(final Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);

		getActivity().setTitle(garden.getName() + " plants");

		adapter = new PlantAdapter(getActivity());
		name.setText(garden.getName());

		boolean reverse = PreferenceManager.getDefaultSharedPreferences(getActivity()).getBoolean("reverse_order", false);
		LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, reverse);
		layoutManager.setStackFromEnd(reverse);
		recycler.setLayoutManager(layoutManager);
		recycler.setAdapter(adapter);
		recycler.setNestedScrollingEnabled(false);

		ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(adapter)
		{
			@Override public boolean isLongPressDragEnabled()
			{
				return !beingFiltered();
			}

			@Override public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target)
			{
				int fromPosition = viewHolder.getAdapterPosition();
				int toPosition = target.getAdapterPosition();

				if (fromPosition < toPosition)
				{
					for (int index = fromPosition; index < toPosition; index++)
					{
						Collections.swap(PlantManager.getInstance().getPlants(), index, index + 1);
						Collections.swap(adapter.getPlants(), index, index + 1);
						adapter.notifyItemChanged(index, Boolean.TRUE);
						adapter.notifyItemChanged(index + 1, Boolean.TRUE);
					}
				}
				else
				{
					for (int index = fromPosition; index > toPosition; index--)
					{
						Collections.swap(PlantManager.getInstance().getPlants(), index, index - 1);
						Collections.swap(adapter.getPlants(), index, index - 1);
						adapter.notifyItemChanged(index, Boolean.TRUE);
						adapter.notifyItemChanged(index - 1, Boolean.TRUE);
					}
				}

				adapter.notifyItemMoved(fromPosition, toPosition);
				return true;
			}
		};
		ItemTouchHelper touchHelper = new ItemTouchHelper(callback);
		touchHelper.attachToRecyclerView(recycler);

		filterList.addAll(Arrays.asList(PlantStage.values()));

		if (hideHarvested = PreferenceManager.getDefaultSharedPreferences(getActivity()).getBoolean("hide_harvested", false))
		{
			filterList.remove(PlantStage.HARVESTED);
			hideHarvested = true;
		}
	}

	@Override public void onStart()
	{
		super.onStart();

		filter();
	}

	@Override public void onStop()
	{
		super.onStop();

		saveCurrentState();
	}

	private boolean beingFiltered()
	{
		return !(filterList.size() == PlantStage.values().length - (hideHarvested ? 1 : 0));
	}

	private synchronized void saveCurrentState()
	{
		ArrayList<Plant> plants = (ArrayList<Plant>)adapter.getPlants();

		if (!beingFiltered())
		{
			ArrayList<String> orderedPlantIds = new ArrayList<>();
			for (Plant plant : plants)
			{
				orderedPlantIds.add(plant.getId());
			}

			garden.setPlantIds(orderedPlantIds);
			GardenManager.getInstance().save();
		}
	}

	@Views.OnClick public void onFabAddClick(View view)
	{
		Intent addPlant = new Intent(getActivity(), AddPlantActivity.class);
		addPlant.putExtra("garden_index", GardenManager.getInstance().getGardens().indexOf(garden));
		startActivity(addPlant);
	}

	@Views.OnClick public void onFeedingClick(View view)
	{
		int[] plants = new int[adapter.getItemCount()];

		int index = 0;
		for (Plant plant : adapter.getPlants())
		{
			plants[index] = PlantManager.getInstance().getPlants().indexOf(plant);
			index++;
		}

		Intent feed = new Intent(getActivity(), AddWateringActivity.class);
		feed.putExtra("plant_index", plants);
		startActivityForResult(feed, 2);
	}

	@Views.OnClick public void onActionClick(final View view)
	{
		ActionDialogFragment dialogFragment = new ActionDialogFragment();
		dialogFragment.setOnActionSelected(new ActionDialogFragment.OnActionSelected()
		{
			@Override public void onActionSelected(final EmptyAction action)
			{
				for (Plant plant : adapter.getPlants())
				{
					plant.getActions().add(new Kryo().copy(action));
				}

				PlantManager.getInstance().save();

				SnackBar.show(getActivity(), "Actions added", new SnackBarListener()
				{
					@Override public void onSnackBarStarted(Object o)
					{
						if (getView() != null)
						{
							FabAnimator.animateUp(getView().findViewById(R.id.fab_add));
						}
					}

					@Override public void onSnackBarFinished(Object o)
					{
						if (getView() != null)
						{
							FabAnimator.animateDown(getView().findViewById(R.id.fab_add));
						}
					}

					@Override public void onSnackBarAction(View v)
					{
					}
				});
			}
		});
		dialogFragment.show(getFragmentManager(), null);
	}

	@Views.OnClick public void onNoteClick(final View view)
	{
		NoteDialogFragment dialogFragment = new NoteDialogFragment();
		dialogFragment.setOnDialogConfirmed(new NoteDialogFragment.OnDialogConfirmed()
		{
			@Override public void onDialogConfirmed(String notes)
			{
				for (Plant plant : adapter.getPlants())
				{
					NoteAction action = new NoteAction(notes);
					plant.getActions().add(action);
				}

				PlantManager.getInstance().save();

				SnackBar.show(getActivity(), "Notes added", new SnackBarListener()
				{
					@Override public void onSnackBarStarted(Object o)
					{
						if (getView() != null)
						{
							FabAnimator.animateUp(getView().findViewById(R.id.fab_add));
						}
					}

					@Override public void onSnackBarFinished(Object o)
					{
						if (getView() != null)
						{
							FabAnimator.animateDown(getView().findViewById(R.id.fab_add));
						}
					}

					@Override public void onSnackBarAction(View v)
					{
					}
				});
			}
		});
		dialogFragment.show(getFragmentManager(), null);
	}

	@Override public void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if (requestCode == 2)
		{
			if (resultCode != Activity.RESULT_CANCELED)
			{
				adapter.notifyDataSetChanged();
				SnackBar.show(getActivity(), "Watering added", new SnackBarListener()
				{
					@Override public void onSnackBarStarted(Object o)
					{
						if (getView() != null)
						{
							FabAnimator.animateUp(getView().findViewById(R.id.fab_add));
						}
					}

					@Override public void onSnackBarAction(View v)
					{

					}

					@Override public void onSnackBarFinished(Object o)
					{
						if (getView() != null)
						{
							FabAnimator.animateDown(getView().findViewById(R.id.fab_add));
						}
					}
				});
			}
		}

		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
	{
		inflater.inflate(R.menu.plant_list_menu, menu);

		menu.findItem(R.id.export_garden).setVisible(true);
		menu.findItem(R.id.edit_garden).setVisible(true);
		menu.findItem(R.id.delete_garden).setVisible(true);

		if (PreferenceManager.getDefaultSharedPreferences(getActivity()).getBoolean("hide_harvested", false))
		{
			menu.findItem(R.id.filter_harvested).setVisible(false);
		}

		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override public boolean onOptionsItemSelected(MenuItem item)
	{
		if (item.getItemId() == R.id.edit_garden)
		{
			GardenDialogFragment dialogFragment = new GardenDialogFragment(garden);
			dialogFragment.setOnEditGardenListener(new GardenDialogFragment.OnEditGardenListener()
			{
				@Override public void onGardenEdited(Garden garden)
				{
					int index = GardenManager.getInstance().getGardens().indexOf(GardenFragment.this.garden);
					GardenManager.getInstance().getGardens().set(index, garden);
					GardenManager.getInstance().save();
					GardenFragment.this.garden = garden;

					getActivity().setTitle(garden == null ? "All" : garden.getName() + " plants");
					filter();

					((MainActivity)getActivity()).setNavigationView();
				}
			});
			dialogFragment.show(getFragmentManager(), null);

			return true;
		}
		else if (item.getItemId() == R.id.export_garden)
		{
			Toast.makeText(getActivity(), "Exporting grow log of garden...", Toast.LENGTH_SHORT).show();
			NotificationHelper.createExportChannel(getActivity());
			NotificationHelper.sendExportNotification(getActivity(), "Exporting garden grow log", "Exporting " + garden.getName());

			ArrayList<Plant> export = new ArrayList<>(adapter.getPlants());
			ExportService.export(getActivity(), export, garden.getName().replaceAll("[^a-zA-Z0-9]+", "-"), garden.getName());
		}
		else if (item.getItemId() == R.id.delete_garden)
		{
			new AlertDialog.Builder(getActivity())
				.setTitle("Are you sure?")
				.setMessage(Html.fromHtml("Are you sure you want to delete garden <b>" + garden.getName() + "</b>? This will not delete the plants."))
				.setPositiveButton("Yes", new DialogInterface.OnClickListener()
				{
					@Override public void onClick(DialogInterface dialogInterface, int i)
					{
						final Garden oldGarden = garden;
						final int oldIndex = GardenManager.getInstance().getGardens().indexOf(garden);
						final int defaultGarden = PreferenceManager.getDefaultSharedPreferences(getActivity()).getInt("default_garden", -1);

						PreferenceManager.getDefaultSharedPreferences(getActivity()).edit().remove("default_garden").apply();
						GardenManager.getInstance().getGardens().remove(garden);
						GardenManager.getInstance().save();

						SnackBar.show(getActivity(), "Garden deleted", "undo", new SnackBarListener()
						{
							@Override public void onSnackBarStarted(Object o){}
							@Override public void onSnackBarFinished(Object o){}

							@Override public void onSnackBarAction(View o)
							{
								PreferenceManager.getDefaultSharedPreferences(o.getContext()).edit().putInt("default_garden", defaultGarden).apply();
								GardenManager.getInstance().getGardens().add(oldIndex, oldGarden);
								GardenManager.getInstance().save();

								BusHelper.getInstance().post(new GardenChangeEvent());
							}
						});

						((MainActivity)getActivity()).setNavigationView();
						((MainActivity)getActivity()).getNavigation().getMenu().findItem(R.id.all).setChecked(true);
						((MainActivity)getActivity()).onNavigationItemSelected(((MainActivity)getActivity()).getNavigation().getMenu().findItem(R.id.all));
					}
				})
				.setNegativeButton("No", null)
				.show();
		}
		else
		{
			if (item.isCheckable())
			{
				item.setChecked(!item.isChecked());
			}

			boolean filter = false;

			if (!beingFiltered())
			{
				saveCurrentState();
			}

			int[] ids = {R.id.filter_planted, R.id.filter_germination, R.id.filter_seedling, R.id.filter_cutting, R.id.filter_vegetation, R.id.filter_flowering, R.id.filter_drying, R.id.filter_curing, R.id.filter_harvested};
			PlantStage[] stages = PlantStage.values();

			for (int index = 0; index < ids.length; index++)
			{
				int id = ids[index];
				if (item.getItemId() == id)
				{
					if (filterList.contains(stages[index]))
					{
						filterList.remove(stages[index]);
					}
					else
					{
						filterList.add(stages[index]);
					}

					filter = true;
				}
			}

			if (filter)
			{
				filter();
			}
		}

		return super.onOptionsItemSelected(item);
	}

	private void filter()
	{
		ArrayList<Plant> plantList = PlantManager.getInstance().getSortedPlantList(garden);
		adapter.setPlants(plantList);

		ArrayList<String> plants = new ArrayList<>();
		for (Plant plant : plantList)
		{
			if (filterList.contains(plant.getStage()))
			{
				plants.add(plant.getId());
			}
		}

		if (garden != null || plants.size() < plantList.size())
		{
			adapter.setShowOnly(plants);
		}

		adapter.notifyDataSetChanged();
	}
}
