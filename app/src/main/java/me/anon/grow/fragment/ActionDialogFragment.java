package me.anon.grow.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import lombok.Setter;
import me.anon.grow.R;
import me.anon.lib.Views;
import me.anon.model.Action;
import me.anon.model.EmptyAction;

@Views.Injectable
public class ActionDialogFragment extends DialogFragment
{
	public static interface OnActionSelected
	{
		public void onActionSelected(Action.ActionName action, String notes);
	}

	@Views.InjectView(R.id.actions) private Spinner actionsSpinner;
	@Views.InjectView(R.id.notes) private EditText notes;

	@Setter private OnActionSelected onActionSelected;

	private EmptyAction action;

	public ActionDialogFragment(){}

	public ActionDialogFragment(EmptyAction action)
	{
		this.action = action;
	}

	@Override public Dialog onCreateDialog(Bundle savedInstanceState)
	{
		final Context context = getActivity();

		AlertDialog.Builder dialog = new AlertDialog.Builder(context);
		dialog.setTitle("Add action");
		View view = LayoutInflater.from(getActivity()).inflate(R.layout.action_dialog, null);

		Views.inject(this, view);

		String[] actions = new String[Action.ActionName.names().length - 2];
		System.arraycopy(Action.ActionName.names(), 2, actions, 0, actions.length);

		actionsSpinner.setAdapter(new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_dropdown_item, actions));

		if (action != null)
		{
			notes.setText(action.getNotes());
			int selectionIndex = 0;

			for (int index = 0; index < actions.length; index++)
			{
				String actionName = actions[index];
				if (actionName.equalsIgnoreCase(action.getAction().getPrintString()))
				{
					selectionIndex = index;
					break;
				}
			}

			actionsSpinner.setSelection(selectionIndex);
		}

		dialog.setView(view);
		dialog.setPositiveButton("Add", new DialogInterface.OnClickListener()
		{
			@Override public void onClick(DialogInterface dialog, int which)
			{
				if (onActionSelected != null)
				{
					onActionSelected.onActionSelected(Action.ActionName.values()[actionsSpinner.getSelectedItemPosition() + 2], TextUtils.isEmpty(notes.getText()) ? null : notes.getText().toString());
				}
			}
		});
		dialog.setNegativeButton("Cancel", null);

		return dialog.create();
	}
}
