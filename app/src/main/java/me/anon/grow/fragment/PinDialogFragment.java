package me.anon.grow.fragment;

import android.annotation.SuppressLint;
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
import android.view.WindowManager;
import android.widget.EditText;

import me.anon.grow.R;
import me.anon.lib.Views;

@Views.Injectable
public class PinDialogFragment extends DialogFragment
{
	public static interface OnDialogConfirmed
	{
		public void onDialogConfirmed(@Nullable String input);
	}

	public static interface OnDialogCancelled
	{
		public void onDialogCancelled();
	}

	@Views.InjectView(R.id.pin) private EditText input;

	private OnDialogConfirmed onDialogConfirmed;
	private OnDialogCancelled onDialogCancelled;
	private String title = "Pin";

	public void setOnDialogConfirmed(OnDialogConfirmed onDialogConfirmed)
	{
		this.onDialogConfirmed = onDialogConfirmed;
	}

	public void setOnDialogCancelled(OnDialogCancelled onDialogCancelled)
	{
		this.onDialogCancelled = onDialogCancelled;
	}

	public void setTitle(String title)
	{
		this.title = title;
	}

	@SuppressLint("ValidFragment")
	public PinDialogFragment(){}

	@Override public Dialog onCreateDialog(Bundle savedInstanceState)
	{
		final Context context = getActivity();

		AlertDialog.Builder dialog = new AlertDialog.Builder(context);
		dialog.setTitle(title);
		View view = LayoutInflater.from(getActivity()).inflate(R.layout.pin_dialog, null);

		Views.inject(this, view);

		dialog.setView(view);
		dialog.setPositiveButton("Accept", new DialogInterface.OnClickListener()
		{
			@Override public void onClick(DialogInterface dialog, int which)
			{
				if (onDialogConfirmed != null)
				{
					onDialogConfirmed.onDialogConfirmed(TextUtils.isEmpty(input.getText()) ? null : input.getText().toString());
				}
			}
		});
		dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener()
		{
			@Override public void onClick(DialogInterface dialog, int which)
			{
				if (onDialogCancelled != null)
				{
					onDialogCancelled.onDialogCancelled();
				}
			}
		});

		return dialog.create();
	}

	@Override public void onCancel(DialogInterface dialog)
	{
		super.onCancel(dialog);

		if (onDialogCancelled != null)
		{
			onDialogCancelled.onDialogCancelled();
		}
	}

	@Override public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);
		getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
	}
}
