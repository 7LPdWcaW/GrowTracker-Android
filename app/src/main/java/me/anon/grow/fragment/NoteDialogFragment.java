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
import android.widget.TextView;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

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
		public void onDialogConfirmed(String notes, Date date);
	}

	@Views.InjectView(R.id.notes) private EditText notes;
	@Views.InjectView(R.id.date) private TextView date;
	private NoteAction action;
	private Date actionDate = new Date();

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

		actionDate = Calendar.getInstance().getTime();
		if (action != null)
		{
			notes.setText(action.getNotes());
			actionDate = new Date(action.getDate());
		}

		final DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(getActivity());
		final DateFormat timeFormat = android.text.format.DateFormat.getTimeFormat(getActivity());

		String dateStr = dateFormat.format(actionDate) + " " + timeFormat.format(actionDate);

		this.date.setText(dateStr);
		this.date.setOnClickListener(new View.OnClickListener()
		{
			@Override public void onClick(View v)
			{
				final DateDialogFragment fragment = DateDialogFragment.newInstance(actionDate.getTime());
				fragment.setOnDateSelected(new DateDialogFragment.OnDateSelectedListener()
				{
					@Override public void onDateSelected(Calendar date)
					{
						String dateStr = dateFormat.format(date.getTime()) + " " + timeFormat.format(date.getTime());
						NoteDialogFragment.this.date.setText(dateStr);

						actionDate = date.getTime();

						onCancelled();
					}

					@Override public void onCancelled()
					{
						getChildFragmentManager().beginTransaction().remove(fragment).commit();
					}
				});
				getChildFragmentManager().beginTransaction().add(fragment, "date").commit();
			}
		});

		dialog.setView(view);
		dialog.setPositiveButton(action == null ? R.string.add : R.string.edit, new DialogInterface.OnClickListener()
		{
			@Override public void onClick(DialogInterface dialog, int which)
			{
				if (onDialogConfirmed != null)
				{
					onDialogConfirmed.onDialogConfirmed(
						TextUtils.isEmpty(notes.getText()) ? null : notes.getText().toString(),
						actionDate
					);
				}
			}
		});
		dialog.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener()
		{
			@Override public void onClick(DialogInterface dialogInterface, int i)
			{
				onCancel(dialogInterface);
			}
		});

		return dialog.create();
	}

	@Override public void onCancel(@NonNull DialogInterface dialog)
	{
		super.onCancel(dialog);
		if (onCancelListener != null) onCancelListener.onCancel(dialog);
	}
}
