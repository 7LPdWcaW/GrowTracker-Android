package me.anon.grow.fragment;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.esotericsoftware.kryo.Kryo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import me.anon.controller.adapter.ActionAdapter;
import me.anon.controller.adapter.SimpleItemTouchHelperCallback;
import me.anon.controller.provider.PlantWidgetProvider;
import me.anon.grow.EditWateringActivity;
import me.anon.grow.R;
import me.anon.lib.SnackBar;
import me.anon.lib.SnackBarListener;
import me.anon.lib.Views;
import me.anon.lib.helper.FabAnimator;
import me.anon.lib.manager.PlantManager;
import me.anon.model.Action;
import me.anon.model.EmptyAction;
import me.anon.model.NoteAction;
import me.anon.model.Plant;
import me.anon.model.PlantStage;
import me.anon.model.StageChange;
import me.anon.model.Water;
import me.anon.view.ActionHolder;

@Views.Injectable
public class EventListFragment extends Fragment implements ActionAdapter.OnActionSelectListener
{
	private ActionAdapter adapter;

	@Views.InjectView(R.id.recycler_view) private RecyclerView recycler;

	private Plant plant;

	private boolean watering = true;
	private boolean notes = true, stages = true;
	private ArrayList<Action.ActionName> selected = new ArrayList<>();

	public static final int REQUEST_WATERING = 3;

	public static EventListFragment newInstance(Bundle args)
	{
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

		getActivity().setTitle(R.string.events_title);

		if (getArguments() != null)
		{
			plant = getArguments().getParcelable("plant");
			getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
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
			@Override public boolean canDropOver(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder current, @NonNull RecyclerView.ViewHolder target)
			{
				return current instanceof ActionHolder && target instanceof ActionHolder;
			}

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
		PlantManager.getInstance().upsert(plant);

		super.onDestroy();
	}

	public void setActions()
	{
		ArrayList<Action> actions = new ArrayList<>();
		actions.addAll(plant.getActions());
		Collections.reverse(actions);
		actions.removeAll(Collections.singleton(null));
		adapter.setActions(plant, actions);
	}

	@Override public void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if (resultCode == Activity.RESULT_OK && data.getExtras().containsKey("plant"))
		{
			plant = data.getExtras().getParcelable("plant");
			PlantManager.getInstance().upsert(plant);
			setResult();
		}

		if (requestCode == REQUEST_WATERING)
		{
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
				PlantManager.getInstance().upsert(plant);
				setResult();

				SnackBar.show(getActivity(), action.getAction().getPrintString() + " " + getString(R.string.added), getString(R.string.undo), new SnackBarListener()
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

					@Override public void onSnackBarAction(View v)
					{
						plant.getActions().remove(action);
						PlantManager.getInstance().upsert(plant);
						setResult();
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
		plant.getActions().add(action);
		PlantManager.getInstance().upsert(plant);
		setResult();

		setActions();
		adapter.notifyDataSetChanged();

		SnackBar.show(getActivity(), getString(R.string.action_duplicated), getString(R.string.undo), new SnackBarListener()
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

			@Override public void onSnackBarAction(View v)
			{
				plant.getActions().remove(plant.getActions().size() - 1);
				PlantManager.getInstance().upsert(plant);
				setResult();

				setActions();
				adapter.notifyDataSetChanged();
			}
		});
	}

	@Override public void onActionCopy(final Action action)
	{
		PlantSelectDialogFragment dialogFragment = new PlantSelectDialogFragment(true);
		dialogFragment.setDisabled(PlantManager.getInstance().indexOf(plant));
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

				String plantName = getString(R.string.multiple_plants);
				plantName = indexes.size() == 0 ? PlantManager.getInstance().getPlants().get(indexes.get(0)).getName() : plantName;

				SnackBar.show(getActivity(), getString(R.string.added_to) + plantName, getString(R.string.undo), new SnackBarListener()
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

					@Override public void onSnackBarAction(View v)
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
			edit.putExtra("plant_index", PlantManager.getInstance().indexOf(plant));
			edit.putExtra("action_index", originalIndex);
			startActivityForResult(edit, REQUEST_WATERING);
		}
		else if (action instanceof NoteAction)
		{
			NoteDialogFragment dialogFragment = new NoteDialogFragment((NoteAction)action);
			dialogFragment.setOnDialogConfirmed(new NoteDialogFragment.OnDialogConfirmed()
			{
				@Override public void onDialogConfirmed(String notes)
				{
					final NoteAction noteAction = new NoteAction(System.currentTimeMillis(), notes);

					plant.getActions().set(originalIndex, noteAction);
					PlantManager.getInstance().upsert(plant);
					setActions();
					adapter.notifyDataSetChanged();

					SnackBar.show(getActivity(), getString(R.string.note_updated), getString(R.string.undo), new SnackBarListener()
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

						@Override public void onSnackBarAction(View v)
						{
							plant.getActions().set(originalIndex, action);
							PlantManager.getInstance().upsert(plant);
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
					PlantManager.getInstance().upsert(plant);
					setActions();
					adapter.notifyDataSetChanged();

					SnackBar.show(getActivity(), action.getAction().getPrintString() + " " + getString(R.string.updated), getString(R.string.undo), new SnackBarListener()
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
							plant.getActions().set(originalIndex, action);
							PlantManager.getInstance().upsert(plant);
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
					if (action.getNewStage() == PlantStage.PLANTED)
					{
						plant.setPlantDate(action.getDate());
					}

					plant.getActions().set(originalIndex, action);
					PlantManager.getInstance().upsert(plant);
					setActions();
					adapter.notifyDataSetChanged();

					SnackBar.show(getActivity(), getString(R.string.stage_updated), getString(R.string.undo), new SnackBarListener()
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

						@Override public void onSnackBarAction(View v)
						{
							plant.getActions().set(originalIndex, action);
							PlantManager.getInstance().upsert(plant);
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

		setResult();

		SnackBar.show(getActivity(), getString(R.string.event_deleted), getString(R.string.undo), new SnackBarListener()
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

			@Override public void onSnackBarAction(View v)
			{
				plant.getActions().add(originalIndex, action);
				setActions();
				adapter.notifyDataSetChanged();

				setResult();
			}
		});
	}

	private void setResult()
	{
		Intent intent = new Intent();
		intent.putExtra("plant", plant);
		getActivity().setResult(Activity.RESULT_OK, intent);
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
				actionItems[index] = getString(Action.ActionName.values()[index].getPrintString());
				selectedItems[index] = selected.contains(Action.ActionName.values()[index]);
			}

			new AlertDialog.Builder(getActivity())
				.setTitle(R.string.filter_actions)
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
				.setPositiveButton(R.string.done, new DialogInterface.OnClickListener()
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
		items.addAll(plant.getActions());
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
