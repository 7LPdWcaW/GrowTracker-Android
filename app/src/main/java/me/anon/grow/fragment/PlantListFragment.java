package me.anon.grow.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.Html;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.esotericsoftware.kryo.Kryo;
import com.kenny.snackbar.SnackBar;
import com.kenny.snackbar.SnackBarListener;

import java.util.ArrayList;
import java.util.Arrays;

import lombok.Setter;
import me.anon.controller.adapter.PlantAdapter;
import me.anon.controller.adapter.SimpleItemTouchHelperCallback;
import me.anon.grow.AddPlantActivity;
import me.anon.grow.AddWateringActivity;
import me.anon.grow.MainActivity;
import me.anon.grow.MainApplication;
import me.anon.grow.R;
import me.anon.lib.Views;
import me.anon.lib.event.GardenChangeEvent;
import me.anon.lib.helper.BusHelper;
import me.anon.lib.helper.FabAnimator;
import me.anon.lib.manager.GardenManager;
import me.anon.lib.manager.PlantManager;
import me.anon.model.EmptyAction;
import me.anon.model.Garden;
import me.anon.model.NoteAction;
import me.anon.model.Plant;
import me.anon.model.PlantStage;

/**
 * // TODO: Add class description
 *
 * @author 7LPdWcaW
 * @documentation // TODO Reference flow doc
 * @project GrowTracker
 */
@Views.Injectable
public class PlantListFragment extends Fragment
{
	private PlantAdapter adapter;
	@Setter private Garden garden;

	public static PlantListFragment newInstance(@Nullable Garden garden)
	{
		PlantListFragment fragment = new PlantListFragment();
		fragment.setGarden(garden);

		return fragment;
	}

	@Views.InjectView(R.id.action_container) private View actionContainer;
	@Views.InjectView(R.id.recycler_view) private RecyclerView recycler;

	private ArrayList<PlantStage> filterList = new ArrayList<>();
	private boolean hideHarvested = false;

	@Override public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		setHasOptionsMenu(true);
	}

	@Nullable @Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View view = inflater.inflate(R.layout.plant_list_view, container, false);
		Views.inject(this, view);

		return view;
	}

	@Override public void onActivityCreated(final Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);

		getActivity().setTitle(garden == null ? "All" : garden.getName() + " plants");

		adapter = new PlantAdapter(getActivity());

		if (MainApplication.isTablet())
		{
			GridLayoutManager layoutManager = new GridLayoutManager(getActivity(), 2);
			RecyclerView.ItemDecoration spacesItemDecoration = new RecyclerView.ItemDecoration()
			{
				private int space = (int)(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12, getResources().getDisplayMetrics()) / 2f);

				@Override
				public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state)
				{
					if (parent.getPaddingLeft() != space)
					{
						parent.setPadding(space, space, space, space);
						parent.setClipToPadding(false);
					}

					outRect.top = space;
					outRect.bottom = space;
					outRect.left = space;
					outRect.right = space;
				}
			};

			recycler.setLayoutManager(layoutManager);
			recycler.addItemDecoration(spacesItemDecoration);
		}
		else
		{
			recycler.setLayoutManager(new LinearLayoutManager(getActivity()));
		}

		recycler.setAdapter(adapter);

		ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(adapter)
		{
			@Override public boolean isLongPressDragEnabled()
			{
				return !beingFiltered();
			}
		};
		ItemTouchHelper touchHelper = new ItemTouchHelper(callback);
		touchHelper.attachToRecyclerView(recycler);

		if (garden != null)
		{
			actionContainer.setVisibility(View.VISIBLE);
		}

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

		if (garden == null)
		{
			PlantManager.getInstance().setPlants(plants);
			PlantManager.getInstance().save();
		}
		else
		{
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
	}

	@Views.OnClick public void onFabAddClick(View view)
	{
		Intent addPlant = new Intent(getActivity(), AddPlantActivity.class);

		if (garden != null)
		{
			addPlant.putExtra("garden_index", GardenManager.getInstance().getGardens().indexOf(garden));
		}

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

					@Override public void onSnackBarAction(Object o)
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

					@Override public void onSnackBarAction(Object o)
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
				SnackBar.show(getActivity(), "Watering added", new SnackBarListener()
				{
					@Override public void onSnackBarStarted(Object o)
					{
						if (getView() != null)
						{
							FabAnimator.animateUp(getView().findViewById(R.id.fab_add));
						}
					}

					@Override public void onSnackBarAction(Object object)
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

		if (garden != null)
		{
			menu.findItem(R.id.edit_garden).setVisible(true);
			menu.findItem(R.id.delete_garden).setVisible(true);
		}

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
					int index = GardenManager.getInstance().getGardens().indexOf(PlantListFragment.this.garden);
					GardenManager.getInstance().getGardens().set(index, garden);
					GardenManager.getInstance().save();
					PlantListFragment.this.garden = garden;

					getActivity().setTitle(garden == null ? "All" : garden.getName() + " plants");
					filter();

					((MainActivity)getActivity()).setNavigationView();
				}
			});
			dialogFragment.show(getFragmentManager(), null);

			return true;
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

						GardenManager.getInstance().getGardens().remove(garden);
						GardenManager.getInstance().save();

						SnackBar.show(getActivity(), "Garden deleted", "undo", new SnackBarListener()
						{
							@Override public void onSnackBarStarted(Object o){}
							@Override public void onSnackBarFinished(Object o){}

							@Override public void onSnackBarAction(Object o)
							{
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

			int[] ids = {R.id.filter_planted, R.id.filter_germination, R.id.filter_cutting, R.id.filter_vegetation, R.id.filter_flowering, R.id.filter_drying, R.id.filter_curing, R.id.filter_harvested};
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

		if ((garden == null && plants.size() < plantList.size()) || garden != null)
		{
			adapter.setShowOnly(plants);
		}
		else
		{
			adapter.setShowOnly(null);
		}

		adapter.notifyDataSetChanged();
	}
}
