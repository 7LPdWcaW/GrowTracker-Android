package me.anon.grow.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.esotericsoftware.kryo.Kryo;
import com.kenny.snackbar.SnackBar;
import com.kenny.snackbar.SnackBarListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;

import me.anon.controller.adapter.ActionAdapter;
import me.anon.controller.adapter.SimpleItemTouchHelperCallback;
import me.anon.controller.provider.PlantWidgetProvider;
import me.anon.grow.EditWateringActivity;
import me.anon.grow.R;
import me.anon.lib.Views;
import me.anon.lib.helper.FabAnimator;
import me.anon.lib.manager.PlantManager;
import me.anon.model.Action;
import me.anon.model.EmptyAction;
import me.anon.model.NoteAction;
import me.anon.model.Plant;
import me.anon.model.StageChange;
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

	private boolean watering = true;
	private boolean notes = true, stages = true;
	private ArrayList<Action.ActionName> selected = new ArrayList<>();
	private boolean beingDragged = false;

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

		selected.addAll(new ArrayList<Action.ActionName>(Arrays.asList(Action.ActionName.values())));
		adapter = new ActionAdapter();
		adapter.setOnActionSelectListener(this);

		setActions();

		recycler.setLayoutManager(new LinearLayoutManager(getActivity()));
		recycler.setAdapter(adapter);

		ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(adapter)
		{
			@Override public boolean isLongPressDragEnabled()
			{
				return selected.size() == Action.ActionName.values().length && watering && notes && stages;
			}
		};
		ItemTouchHelper touchHelper = new ItemTouchHelper(callback);
		touchHelper.attachToRecyclerView(recycler);

		adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver()
		{
			@Override public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount)
			{
				if (selected.size() == Action.ActionName.values().length && watering && notes && stages)
				{
					ArrayList<Action> actions = new ArrayList<Action>();
					actions.addAll((ArrayList<Action>)adapter.getActions());
					Collections.reverse(actions);

					plant.setActions(actions);
				}
			}
		});
	}

	@Override public void onDestroy()
	{
		super.onDestroy();
		PlantManager.getInstance().upsert(plantIndex, plant);
	}

	public void setActions()
	{
		ArrayList<Action> actions = new ArrayList<>();
		actions.addAll(PlantManager.getInstance().getPlants().get(plantIndex).getActions());
		Collections.reverse(actions);
		actions.removeAll(Collections.singleton(null));
		adapter.setActions(plant, actions);
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
							PlantWidgetProvider.triggerUpdateAll(getView().getContext());
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

	@Override public void onActionDuplicate(Action action)
	{
		action.setDate(action.getDate() + new Random().nextInt(1000));
		PlantManager.getInstance().getPlants().get(plantIndex).getActions().add(action);
		PlantManager.getInstance().save();
		setActions();
		adapter.notifyDataSetChanged();

		SnackBar.show(getActivity(), "Action duplicated", "Undo", new SnackBarListener()
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
					PlantWidgetProvider.triggerUpdateAll(getView().getContext());
				}
			}

			@Override public void onSnackBarAction(Object o)
			{
				PlantManager.getInstance().getPlants().get(plantIndex).getActions().remove(PlantManager.getInstance().getPlants().get(plantIndex).getActions().size() - 1);
				PlantManager.getInstance().save();
				setActions();
				adapter.notifyDataSetChanged();
			}
		});
	}

	@Override public void onActionCopy(final Action action)
	{
		PlantSelectDialogFragment dialogFragment = new PlantSelectDialogFragment(true);
		dialogFragment.setOnDialogActionListener(new PlantSelectDialogFragment.OnDialogActionListener()
		{
			@Override public void onDialogAccept(final ArrayList<Integer> indexes, boolean showImage)
			{
				for (int plantIndex = 0; plantIndex < indexes.size(); plantIndex++)
				{
					PlantManager.getInstance().getPlants().get(indexes.get(plantIndex)).getActions().add(new Kryo().copy(action));
				}

				PlantManager.getInstance().save();
				setActions();
				adapter.notifyDataSetChanged();

				String plantName = "multiple plants";
				plantName = indexes.size() == 0 ? PlantManager.getInstance().getPlants().get(indexes.get(0)).getName() : plantName;

				SnackBar.show(getActivity(), "Action added to " + plantName, "Undo", new SnackBarListener()
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
							PlantWidgetProvider.triggerUpdateAll(getView().getContext());
						}
					}

					@Override public void onSnackBarAction(Object o)
					{
						for (int plantIndex = 0; plantIndex < indexes.size(); plantIndex++)
						{
							Plant plant = PlantManager.getInstance().getPlants().get(indexes.get(plantIndex));
							plant.getActions().remove(plant.getActions().size() - 1);
						}

						PlantManager.getInstance().save();
						setActions();
						adapter.notifyDataSetChanged();
					}
				});
			}
		});

		dialogFragment.show(getFragmentManager(), null);
	}

	@Override public void onActionEdit(final Action action)
	{
		final int originalIndex = plant.getActions().indexOf(action);

		if (action instanceof Water)
		{
			Intent edit = new Intent(getActivity(), EditWateringActivity.class);
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
								FabAnimator.animateUp(getView().findViewById(R.id.fab_add));
							}
						}

						@Override public void onSnackBarFinished(Object o)
						{
							if (getView() != null)
							{
								FabAnimator.animateDown(getView().findViewById(R.id.fab_add));
								PlantWidgetProvider.triggerUpdateAll(getView().getContext());
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
			ActionDialogFragment dialogFragment = ActionDialogFragment.newInstance((EmptyAction)action);
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
		else if (action instanceof StageChange)
		{
			StageDialogFragment dialogFragment = StageDialogFragment.newInstance((StageChange)action);
			dialogFragment.setOnStageUpdated(new StageDialogFragment.OnStageUpdated()
			{
				@Override public void onStageUpdated(final StageChange action)
				{
					plant.getActions().set(originalIndex, action);
					PlantManager.getInstance().upsert(plantIndex, plant);
					setActions();
					adapter.notifyDataSetChanged();

					SnackBar.show(getActivity(), "Stage updated", "undo", new SnackBarListener()
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
								PlantWidgetProvider.triggerUpdateAll(getView().getContext());
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
					PlantWidgetProvider.triggerUpdateAll(getView().getContext());
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
		if (item.isCheckable())
		{
			item.setChecked(!item.isChecked());
		}

		if (item.getItemId() == R.id.filter_actions)
		{
			CharSequence[] actionItems = new CharSequence[Action.ActionName.values().length];
			boolean[] selectedItems = new boolean[Action.ActionName.values().length];

			for (int index = 0; index < actionItems.length; index++)
			{
				actionItems[index] = Action.ActionName.values()[index].getPrintString();
				selectedItems[index] = selected.contains(Action.ActionName.values()[index]);
			}

			new AlertDialog.Builder(getActivity())
				.setTitle("Actions")
				.setMultiChoiceItems(actionItems, selectedItems, new DialogInterface.OnMultiChoiceClickListener()
				{
					@Override public void onClick(DialogInterface dialog, int which, boolean isChecked)
					{
						if (isChecked)
						{
							selected.add(Action.ActionName.values()[which]);
						}
						else
						{
							selected.remove(Action.ActionName.values()[which]);
						}
					}
				})
				.setPositiveButton("Done", new DialogInterface.OnClickListener()
				{
					@Override public void onClick(DialogInterface dialog, int which)
					{
						filter();
					}
				})
				.show();
		}
		else if (item.getItemId() == R.id.filter_waterings)
		{
			watering = item.isChecked();
		}
		else if (item.getItemId() == R.id.filter_notes)
		{
			notes = item.isChecked();
		}
		else if (item.getItemId() == R.id.filter_stages)
		{
			stages = item.isChecked();
		}

		filter();

		return true;
	}

	private void filter()
	{
		ArrayList<Action> items = new ArrayList<>();
		items.addAll(PlantManager.getInstance().getPlants().get(plantIndex).getActions());
		Collections.reverse(items);

		for (int index = 0; index < items.size(); index++)
		{
			if (items.get(index) instanceof EmptyAction)
			{
				if (!selected.contains(((EmptyAction)items.get(index)).getAction()))
				{
					items.set(index, null);
				}
			}
			else if (!notes && items.get(index) instanceof NoteAction)
			{
				items.set(index, null);
			}
			else if (!stages && items.get(index) instanceof StageChange)
			{
				items.set(index, null);
			}
			else if (!watering && items.get(index) instanceof Water)
			{
				items.set(index, null);
			}
		}

		items.removeAll(Collections.singleton(null));
		adapter.setActions(plant, items);
		adapter.notifyDataSetChanged();
	}
}
