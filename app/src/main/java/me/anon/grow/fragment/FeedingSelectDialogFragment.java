package me.anon.grow.fragment;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import me.anon.controller.adapter.FeedingDateAdapter;
import me.anon.grow.R;
import me.anon.lib.Views;
import me.anon.lib.helper.TimeHelper;
import me.anon.model.FeedingSchedule;
import me.anon.model.FeedingScheduleDate;
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
public class FeedingSelectDialogFragment extends DialogFragment
{
	public interface OnFeedingSelectedListener
	{
		public void onFeedingSelected(FeedingScheduleDate date);
	}

	@Views.InjectView(R.id.recycler_view) private RecyclerView recyclerView;
	private FeedingDateAdapter adapter;
	private OnFeedingSelectedListener onFeedingSelected;
	private Plant plant;
	private FeedingSchedule schedule;

	public void setOnFeedingSelectedListener(OnFeedingSelectedListener onFeedingSelected)
	{
		this.onFeedingSelected = onFeedingSelected;
	}

	@SuppressLint("ValidFragment")
	public FeedingSelectDialogFragment(FeedingSchedule schedule, Plant plant)
	{
		this.plant = plant;
		this.schedule = schedule;
	}


	@SuppressLint("ValidFragment")
	public FeedingSelectDialogFragment()
	{
	}

	@Override public Dialog onCreateDialog(Bundle savedInstanceState)
	{
		View view = getActivity().getLayoutInflater().inflate(R.layout.feeding_list_dialog_view, null, false);
		Views.inject(this, view);

		adapter = new FeedingDateAdapter();
		adapter.setPlant(plant);
		adapter.setItems(schedule.getSchedules());
		recyclerView.setAdapter(adapter);
		recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
		recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), LinearLayout.VERTICAL));

		final AlertDialog dialog = new AlertDialog.Builder(getActivity())
			.setTitle("Feedings")
			.setView(view)
			.create();

		adapter.setOnItemSelectCallback(new Function1<FeedingScheduleDate, Unit>()
		{
			@Override public Unit invoke(FeedingScheduleDate feedingScheduleDate)
			{
				if (onFeedingSelected != null)
				{
					onFeedingSelected.onFeedingSelected(feedingScheduleDate);
				}

				dialog.dismiss();
				return null;
			}
		});

		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
		dialog.setOnShowListener(new DialogInterface.OnShowListener()
		{
			@Override public void onShow(DialogInterface dialog)
			{
				PlantStage lastStage = adapter.getLastStage();
				int days = (int)TimeHelper.toDays(adapter.getPlantStages().get(lastStage));
				int suggestedIndex = 0;
				for (FeedingScheduleDate feedingScheduleDate : adapter.getItems())
				{
					if (lastStage.ordinal() >= feedingScheduleDate.getStageRange()[0].ordinal())
					{
						if (days >= feedingScheduleDate.getDateRange()[0]
						&& ((days <= feedingScheduleDate.getDateRange()[1] && lastStage.ordinal() == feedingScheduleDate.getStageRange()[0].ordinal())
							|| (lastStage.ordinal() < feedingScheduleDate.getStageRange()[1].ordinal())))
						{
							break;
						}
					}

					suggestedIndex++;
				}

				recyclerView.scrollToPosition(suggestedIndex);
			}
		});

		return dialog;
	}
}
