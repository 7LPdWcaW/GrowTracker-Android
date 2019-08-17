package me.anon.grow.fragment;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;

import java.util.ArrayList;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import me.anon.controller.adapter.ActionAdapter;
import me.anon.grow.R;
import me.anon.lib.Views;
import me.anon.model.Action;
import me.anon.view.ActionHolder;
import me.anon.view.ImageActionHolder;

/**
 * // TODO: Add class description
 *
 * @author 7LPdWcaW
 * @documentation // TODO Reference flow doc
 * @project GrowTracker
 */
@Views.Injectable
public class ActionSelectDialogFragment extends DialogFragment
{
	public interface OnActionSelectedListener
	{
		public void onActionSelected(Action action);
	}

	@Views.InjectView(R.id.recycler_view) private RecyclerView recyclerView;
	private ActionAdapter adapter;
	private OnActionSelectedListener onActionSelected;

	public void setOnActionSelectedListener(OnActionSelectedListener onActionSelected)
	{
		this.onActionSelected = onActionSelected;
	}

	@SuppressLint("ValidFragment")
	public ActionSelectDialogFragment(ArrayList<Action> actions)
	{
		ArrayList<Class> exclude = new ArrayList<>();
		exclude.add(ImageActionHolder.class);

		adapter = new ActionAdapter()
		{
			@Override public void onBindViewHolder(RecyclerView.ViewHolder vh, int index)
			{
				super.onBindViewHolder(vh, index);
				int padding = (int)getResources().getDimension(R.dimen.padding_8dp);
				vh.itemView.setPadding(0, 0, 0, 0);
				vh.itemView.findViewById(R.id.date_container).setVisibility(View.GONE);
				((View)vh.itemView.findViewById(R.id.content_container).getParent()).setPadding(0, 0, 0, 0);

				if (vh instanceof ActionHolder)
				{
					((ActionHolder)vh).getCard().setCardBackgroundColor(0);
					((ActionHolder)vh).getCard().setContentPadding(padding, padding, padding * 2, (int)(padding * 2.5));
				}
			}
		};

		adapter.setShowDate(false);
		adapter.setShowActions(false);
		adapter.setActions(null, actions, exclude);
	}

	@SuppressLint("ValidFragment")
	public ActionSelectDialogFragment()
	{
	}

	@Override public Dialog onCreateDialog(Bundle savedInstanceState)
	{
		View view = getActivity().getLayoutInflater().inflate(R.layout.action_list_dialog_view, null, false);
		Views.inject(this, view);

		recyclerView.setAdapter(adapter);
		recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
		recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), LinearLayout.VERTICAL));

		final AlertDialog dialog = new AlertDialog.Builder(getActivity())
			.setTitle(R.string.actions)
			.setView(view)
			.create();

		adapter.setOnItemSelectCallback(new ActionAdapter.OnItemSelectCallback()
		{
			@Override public void onItemSelected(Action action)
			{
				if (onActionSelected != null)
				{
					onActionSelected.onActionSelected(action);
				}

				dialog.dismiss();
			}
		});

		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

		return dialog;
	}
}
