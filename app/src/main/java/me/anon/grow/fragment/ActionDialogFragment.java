package me.anon.grow.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

import me.anon.grow.R;
import me.anon.lib.Views;
import me.anon.model.Action;
import me.anon.model.EmptyAction;

@Views.Injectable
public class ActionDialogFragment extends DialogFragment
{
	public static interface OnActionSelected
	{
		public void onActionSelected(EmptyAction action);
	}

	@Views.InjectView(R.id.actions) private Spinner actionsSpinner;
	@Views.InjectView(R.id.notes) private EditText notes;
	@Views.InjectView(R.id.date) private TextView date;

	private OnActionSelected onActionSelected;

	public void setOnActionSelected(OnActionSelected onActionSelected)
	{
		this.onActionSelected = onActionSelected;
	}

	private EmptyAction action;
	private boolean edit = false;

	public static ActionDialogFragment newInstance()
	{
		return newInstance(null);
	}

	public static ActionDialogFragment newInstance(@Nullable EmptyAction action)
	{
		ActionDialogFragment fragment = new ActionDialogFragment();
		fragment.action = action;
		fragment.edit = action != null;

		return fragment;
	}

	public ActionDialogFragment(){}

	@Override public Dialog onCreateDialog(Bundle savedInstanceState)
	{
		final Context context = getActivity();

		AlertDialog.Builder dialog = new AlertDialog.Builder(context);
		dialog.setTitle((edit ? "Edit" : "Add") + " action");
		View view = LayoutInflater.from(getActivity()).inflate(R.layout.action_dialog, null);

		Views.inject(this, view);

		if (action == null)
		{
			action = new EmptyAction(null);
		}

		if (savedInstanceState != null)
		{
			action.setDate(savedInstanceState.getLong("date", System.currentTimeMillis()));
		}

		final String[] actions = new String[Action.ActionName.names().length];
		System.arraycopy(Action.ActionName.names(), 0, actions, 0, actions.length);

		actionsSpinner.setAdapter(new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_dropdown_item, actions));

		Calendar date = Calendar.getInstance();
		date.setTimeInMillis(action.getDate());

		final DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(getActivity());
		final DateFormat timeFormat = android.text.format.DateFormat.getTimeFormat(getActivity());

		String dateStr = dateFormat.format(new Date(action.getDate())) + " " + timeFormat.format(new Date(action.getDate()));

		this.date.setText(dateStr);
		this.date.setOnClickListener(new View.OnClickListener()
		{
			@Override public void onClick(View v)
			{
				final DateDialogFragment fragment = new DateDialogFragment(action.getDate());
				fragment.setOnDateSelected(new DateDialogFragment.OnDateSelectedListener()
				{
					@Override public void onDateSelected(Calendar date)
					{
						String dateStr = dateFormat.format(date.getTime()) + " " + timeFormat.format(date.getTime());
						ActionDialogFragment.this.date.setText(dateStr);

						action.setDate(date.getTimeInMillis());
						onCancelled();
					}

					@Override public void onCancelled()
					{
						getFragmentManager().beginTransaction().remove(fragment).commit();
					}
				});
				getFragmentManager().beginTransaction().add(fragment, "date").commit();
			}
		});

		notes.setText(action.getNotes());
		int selectionIndex = 0;

		for (int index = 0; index < actions.length; index++)
		{
			String actionName = actions[index];
			if (action.getAction() != null && actionName.equalsIgnoreCase(action.getAction().getPrintString()))
			{
				selectionIndex = index;
				break;
			}
		}

		actionsSpinner.setSelection(selectionIndex);

		dialog.setView(view);
		dialog.setPositiveButton(edit ? "Edit" : "Add", new DialogInterface.OnClickListener()
		{
			@Override public void onClick(DialogInterface dialog, int which)
			{
				if (onActionSelected != null)
				{
					action.setAction(Action.ActionName.values()[actionsSpinner.getSelectedItemPosition()]);
					action.setNotes(TextUtils.isEmpty(notes.getText()) ? null : notes.getText().toString());

					onActionSelected.onActionSelected(action);
				}
			}
		});
		dialog.setNegativeButton("Cancel", null);

		return dialog.create();
	}

	@Override public void onSaveInstanceState(Bundle outState)
	{
		super.onSaveInstanceState(outState);
		outState.putLong("date", action.getDate());
	}
}
