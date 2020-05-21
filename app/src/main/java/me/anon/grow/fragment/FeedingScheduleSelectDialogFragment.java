package me.anon.grow.fragment;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
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
public class FeedingScheduleSelectDialogFragment extends DialogFragment
{
	public interface OnFeedingSelectedListener
	{
		public void onFeedingSelected(FeedingScheduleDate date);
	}

	@Views.InjectView(R.id.recycler_view) private RecyclerView recyclerView;
	private FeedingDateAdapter adapter;
	private OnFeedingSelectedListener onFeedingSelected;
	private ArrayList<Plant> plants;
	private FeedingSchedule schedule;

	public void setOnFeedingSelectedListener(OnFeedingSelectedListener onFeedingSelected)
	{
		this.onFeedingSelected = onFeedingSelected;
	}

	public static FeedingScheduleSelectDialogFragment newInstance(FeedingSchedule schedule, ArrayList<Plant> plants)
	{
		FeedingScheduleSelectDialogFragment fragment = new FeedingScheduleSelectDialogFragment();
		fragment.plants = new ArrayList(plants);
		fragment.schedule = schedule;
		return fragment;
	}

	public FeedingScheduleSelectDialogFragment()
	{
	}

	@Override public void onSaveInstanceState(@NonNull Bundle outState)
	{
		outState.putParcelableArrayList("plants", plants);
		outState.putParcelable("schedule", schedule);
		super.onSaveInstanceState(outState);
	}

	@Override public Dialog onCreateDialog(Bundle savedInstanceState)
	{
		View view = getActivity().getLayoutInflater().inflate(R.layout.feeding_list_dialog_view, null, false);
		Views.inject(this, view);

		if (savedInstanceState != null)
		{
			plants = savedInstanceState.getParcelableArrayList("plants");
			schedule = savedInstanceState.getParcelable("schedule");
		}

		adapter = new FeedingDateAdapter();
		adapter.setPlants(plants);
		adapter.setItems(schedule.getSchedules());
		recyclerView.setAdapter(adapter);
		recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
		recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), LinearLayout.VERTICAL));

		final AlertDialog dialog = new AlertDialog.Builder(getActivity())
			.setTitle(R.string.feeding_schedule_title)
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
				for (int index = 0; index < recyclerView.getAdapter().getItemCount(); index++)
				{
					recyclerView.scrollToPosition(index);
					for (int childIndex = 0; childIndex < recyclerView.getChildCount(); childIndex++)
					{
						if (recyclerView.getChildAt(childIndex).getTag() == Boolean.TRUE)
						{
							recyclerView.scrollToPosition(recyclerView.getChildAdapterPosition(recyclerView.getChildAt(childIndex)));
							return;
						}
					}
				}
			}
		});

		return dialog;
	}
}
