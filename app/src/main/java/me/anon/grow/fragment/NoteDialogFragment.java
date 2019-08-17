package me.anon.grow.fragment;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import me.anon.grow.R;
import me.anon.lib.Views;
import me.anon.model.NoteAction;

@Views.Injectable
public class NoteDialogFragment extends DialogFragment
{
	public static interface OnDialogConfirmed
	{
		public void onDialogConfirmed(String notes);
	}

	@Views.InjectView(R.id.notes) private EditText notes;
	private NoteAction action;

	private OnDialogConfirmed onDialogConfirmed;
	public DialogInterface.OnCancelListener onCancelListener;

	public void setOnDialogConfirmed(OnDialogConfirmed onDialogConfirmed)
	{
		this.onDialogConfirmed = onDialogConfirmed;
	}

	@SuppressLint("ValidFragment")
	public NoteDialogFragment(){}

	@SuppressLint("ValidFragment")
	public NoteDialogFragment(NoteAction action)
	{
		this.action = action;
	}

	@Override public Dialog onCreateDialog(Bundle savedInstanceState)
	{
		final Context context = getActivity();

		AlertDialog.Builder dialog = new AlertDialog.Builder(context);
		dialog.setTitle(getString((action == null ? R.string.add : R.string.edit)) + " " + getString(R.string.note));
		View view = LayoutInflater.from(getActivity()).inflate(R.layout.note_dialog, null);

		Views.inject(this, view);

		if (action != null)
		{
			notes.setText(action.getNotes());
		}

		dialog.setView(view);
		dialog.setPositiveButton(action == null ? R.string.add : R.string.edit, new DialogInterface.OnClickListener()
		{
			@Override public void onClick(DialogInterface dialog, int which)
			{
				if (onDialogConfirmed != null)
				{
					onDialogConfirmed.onDialogConfirmed(TextUtils.isEmpty(notes.getText()) ? null : notes.getText().toString());
				}
			}
		});
		dialog.setNegativeButton(R.string.cancel, null);

		return dialog.create();
	}

	@Override public void onCancel(@NonNull DialogInterface dialog)
	{
		super.onCancel(dialog);
		if (onCancelListener != null) onCancelListener.onCancel(dialog);
	}
}
