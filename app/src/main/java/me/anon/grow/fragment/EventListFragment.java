package me.anon.grow.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.kenny.snackbar.SnackBar;
import com.kenny.snackbar.SnackBarListener;

import java.util.ArrayList;
import java.util.Collections;

import me.anon.controller.adapter.ActionAdapter;
import me.anon.grow.EditFeedingActivity;
import me.anon.grow.R;
import me.anon.lib.Views;
import me.anon.lib.helper.FabAnimator;
import me.anon.lib.manager.PlantManager;
import me.anon.model.Action;
import me.anon.model.EmptyAction;
import me.anon.model.Feed;
import me.anon.model.NoteAction;
import me.anon.model.Plant;
import me.anon.model.Water;

/**
 * // TODO: Add class description
 *
 * @author 7LPdWcaW
 * @documentation // TODO Reference flow doc
 * @project GrowTracker
 */
@Views.Injectable
public class EventListFragment extends Fragment implements ActionAdapter.OnActionSelectListener
{
	private ActionAdapter adapter;

	@Views.InjectView(R.id.recycler_view) private RecyclerView recycler;

	private int plantIndex = -1;
	private Plant plant;

	private boolean feeding = true, watering = true, actions = true;

	/**
	 * @param plantIndex If -1, assume new plant
	 * @return Instantiated details fragment
	 */
	public static EventListFragment newInstance(int plantIndex)
	{
		Bundle args = new Bundle();
		args.putInt("plant_index", plantIndex);

		EventListFragment fragment = new EventListFragment();
		fragment.setArguments(args);

		return fragment;
	}

	@Nullable @Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View view = inflater.inflate(R.layout.event_list_view, container, false);
		Views.inject(this, view);

		return view;
	}

	@Override public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Override public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);

		getActivity().setTitle("Past actions");

		if (getArguments() != null)
		{
			plantIndex = getArguments().getInt("plant_index");

			if (plantIndex > -1)
			{
				plant = PlantManager.getInstance().getPlants().get(plantIndex);
				getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
			}
		}

		if (plant == null)
		{
			getActivity().finish();
			return;
		}

		adapter = new ActionAdapter();
		adapter.setOnActionSelectListener(this);
		setActions();
		recycler.setLayoutManager(new LinearLayoutManager(getActivity()));
		recycler.setAdapter(adapter);
	}

	public void setActions()
	{
		ArrayList<Action> actions = new ArrayList<>();
		actions.addAll(PlantManager.getInstance().getPlants().get(plantIndex).getActions());
		Collections.reverse(actions);
		actions.removeAll(Collections.singleton(null));
		adapter.setActions(actions);
	}

	@Override public void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if (requestCode == 2)
		{
			if (resultCode != Activity.RESULT_CANCELED)
			{
				PlantManager.getInstance().upsert(plantIndex, plant);
				setActions();
				adapter.notifyDataSetChanged();
			}
		}
		else if (requestCode == 3)
		{
			plant = PlantManager.getInstance().getPlants().get(plantIndex);
			setActions();
			adapter.notifyDataSetChanged();
		}

		super.onActivityResult(requestCode, resultCode, data);
	}

	@Views.OnClick public void onFabAddClick(View view)
	{
		ActionDialogFragment dialogFragment = new ActionDialogFragment();
		dialogFragment.setOnActionSelected(new ActionDialogFragment.OnActionSelected()
		{
			@Override public void onActionSelected(final EmptyAction action)
			{
				plant.getActions().add(action);
				PlantManager.getInstance().upsert(plantIndex, plant);

				SnackBar.show(getActivity(), action.getAction().getPrintString() + " added", "undo", new SnackBarListener()
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
						plant.getActions().remove(action);
						PlantManager.getInstance().upsert(plantIndex, plant);
					}
				});

				setActions();
				adapter.notifyDataSetChanged();
			}
		});
		dialogFragment.show(getFragmentManager(), null);
	}

	@Override public void onActionEdit(final Action action)
	{
		final int originalIndex = plant.getActions().indexOf(action);

		if (action instanceof Water)
		{
			Intent edit = new Intent(getActivity(), EditFeedingActivity.class);
			edit.putExtra("plant_index", plantIndex);
			edit.putExtra("action_index", originalIndex);
			startActivityForResult(edit, 3);
		}
		else if (action instanceof NoteAction)
		{
			NoteDialogFragment dialogFragment = new NoteDialogFragment((NoteAction)action);
			dialogFragment.setOnDialogConfirmed(new NoteDialogFragment.OnDialogConfirmed()
			{
				@Override public void onDialogConfirmed(String notes)
				{
					final NoteAction noteAction = new NoteAction(notes);

					plant.getActions().set(originalIndex, noteAction);
					PlantManager.getInstance().upsert(plantIndex, plant);
					setActions();
					adapter.notifyDataSetChanged();

					SnackBar.show(getActivity(), "Note updated", "undo", new SnackBarListener()
					{
						@Override public void onSnackBarStarted(Object o)
						{
							if (getView() != null)
							{
								FabAnimator.animateUp(getView().findViewById(R.id.fab_complete));
							}
						}

						@Override public void onSnackBarFinished(Object o)
						{
							if (getView() != null)
							{
								FabAnimator.animateDown(getView().findViewById(R.id.fab_complete));
							}
						}

						@Override public void onSnackBarAction(Object o)
						{
							plant.getActions().set(originalIndex, action);
							PlantManager.getInstance().upsert(plantIndex, plant);
							setActions();
							adapter.notifyDataSetChanged();
						}
					});
				}
			});
			dialogFragment.show(getFragmentManager(), null);
		}
		else if (action instanceof EmptyAction)
		{
			ActionDialogFragment dialogFragment = new ActionDialogFragment((EmptyAction)action);
			dialogFragment.setOnActionSelected(new ActionDialogFragment.OnActionSelected()
			{
				@Override public void onActionSelected(final EmptyAction action)
				{
					plant.getActions().set(originalIndex, action);
					PlantManager.getInstance().upsert(plantIndex, plant);
					setActions();
					adapter.notifyDataSetChanged();

					SnackBar.show(getActivity(), action.getAction().getPrintString() + " updated", "undo", new SnackBarListener()
					{
						@Override public void onSnackBarStarted(Object o)
						{
							if (getView() != null)
							{
								FabAnimator.animateUp(getView().findViewById(R.id.fab_complete));
							}
						}

						@Override public void onSnackBarFinished(Object o)
						{
							if (getView() != null)
							{
								FabAnimator.animateDown(getView().findViewById(R.id.fab_complete));
							}
						}

						@Override public void onSnackBarAction(Object o)
						{
							plant.getActions().set(originalIndex, action);
							PlantManager.getInstance().upsert(plantIndex, plant);
							setActions();
							adapter.notifyDataSetChanged();
						}
					});
				}
			});
			dialogFragment.show(getFragmentManager(), null);
		}
	}

	@Override public void onActionDeleted(final Action action)
	{
		final int originalIndex = plant.getActions().indexOf(action);
		plant.getActions().remove(action);
		setActions();
		adapter.notifyDataSetChanged();

		SnackBar.show(getActivity(), "Event deleted", "Undo", new SnackBarListener()
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
				plant.getActions().add(originalIndex, action);
				setActions();
				adapter.notifyDataSetChanged();
			}
		});
	}

	@Override public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
	{
		inflater.inflate(R.menu.event_filter_menu, menu);
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override public boolean onOptionsItemSelected(MenuItem item)
	{
		ArrayList<Action> items = new ArrayList<>();
		items.addAll(PlantManager.getInstance().getPlants().get(plantIndex).getActions());
		Collections.reverse(items);

		item.setChecked(!item.isChecked());

		if (item.getItemId() == R.id.filter_actions)
		{
			actions = item.isChecked();
		}
		else if (item.getItemId() == R.id.filter_waterings)
		{
			watering = item.isChecked();
		}
		else if (item.getItemId() == R.id.filter_feedings)
		{
			feeding = item.isChecked();
		}

		for (int index = 0; index < items.size(); index++)
		{
			if (!actions && items.get(index) instanceof EmptyAction)
			{
				items.set(index, null);
			}
			else if (!feeding && items.get(index).getClass() == Feed.class)
			{
				items.set(index, null);
			}
			else if (!watering && items.get(index).getClass() == Water.class)
			{
				items.set(index, null);
			}
		}

		items.removeAll(Collections.singleton(null));
		adapter.setActions(items);
		adapter.notifyDataSetChanged();

		return true;
	}
}
