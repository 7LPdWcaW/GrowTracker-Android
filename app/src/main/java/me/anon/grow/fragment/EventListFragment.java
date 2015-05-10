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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.kenny.snackbar.SnackBar;
import com.kenny.snackbar.SnackBarListener;

import java.util.ArrayList;
import java.util.Collections;

import me.anon.controller.adapter.ActionAdapter;
import me.anon.grow.AddFeedingActivity;
import me.anon.grow.R;
import me.anon.lib.Views;
import me.anon.lib.manager.PlantManager;
import me.anon.model.Action;
import me.anon.model.EmptyAction;
import me.anon.model.Plant;

/**
 * // TODO: Add class description
 *
 * @author 
 * @documentation // TODO Reference flow doc
 * @project GrowTracker
 */
@Views.Injectable
public class EventListFragment extends Fragment implements ActionAdapter.OnActionDeletedListener
{
	private ActionAdapter adapter;

	@Views.InjectView(R.id.recycler_view) private RecyclerView recycler;

	private int plantIndex = -1;
	private Plant plant;

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

	@Override public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);

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
		adapter.setOnActionDeletedListener(this);
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

		super.onActivityResult(requestCode, resultCode, data);
	}


	@Views.OnClick public void onFabAddClick(View view)
	{
		new AlertDialog.Builder(getActivity())
			.setTitle("Select an option")
			.setItems(Action.ActionName.names(), new DialogInterface.OnClickListener()
			{
				@Override public void onClick(DialogInterface dialog, int which)
				{
					if (which == 0 || which == 1)
					{
						Intent feeding = new Intent(getActivity(), AddFeedingActivity.class);
						feeding.putExtra("plant_index", plantIndex);
						feeding.putExtra("water", which == 1);
						startActivityForResult(feeding, 2);
					}
					else
					{
						final EmptyAction action = new EmptyAction(Action.ActionName.values()[which]);
						plant.getActions().add(action);
						PlantManager.getInstance().upsert(plantIndex, plant);

						SnackBar.show(getActivity(), action.getAction().getPrintString() + " added", "undo", new SnackBarListener()
						{
							@Override public void onSnackBarStarted(Object o){}
							@Override public void onSnackBarFinished(Object o){}

							@Override public void onSnackBarAction(Object o)
							{
								plant.getActions().remove(action);
								PlantManager.getInstance().upsert(plantIndex, plant);
							}
						});
					}

					setActions();
					adapter.notifyDataSetChanged();
					dialog.dismiss();
				}
			})
			.show();
	}

	@Override public void onActionDeleted(final Action action)
	{
		final int originalIndex = plant.getActions().indexOf(action);
		plant.getActions().remove(action);
		setActions();
		adapter.notifyDataSetChanged();

		SnackBar.show(getActivity(), "Event deleted", "Undo", new SnackBarListener()
		{
			@Override public void onSnackBarStarted(Object o){}
			@Override public void onSnackBarFinished(Object o){}

			@Override public void onSnackBarAction(Object o)
			{
				plant.getActions().add(originalIndex, action);
				setActions();
				adapter.notifyDataSetChanged();
			}
		});
	}
}
