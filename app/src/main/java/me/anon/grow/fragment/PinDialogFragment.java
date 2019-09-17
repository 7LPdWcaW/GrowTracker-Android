package me.anon.grow.fragment;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import me.anon.grow.R;
import me.anon.lib.Views;

@Views.Injectable
public class PinDialogFragment extends DialogFragment
{
	public static interface OnDialogConfirmed
	{
		public void onDialogConfirmed(DialogInterface dialog, @Nullable String input);
	}

	public static interface OnDialogCancelled
	{
		public void onDialogCancelled();
	}

	@Views.InjectView(R.id.pin) private EditText input;

	public EditText getInput()
	{
		return input;
	}

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
		dialog.setPositiveButton(R.string.accept, new DialogInterface.OnClickListener()
		{
			@Override public void onClick(DialogInterface dialog, int which)
			{

			}
		});
		dialog.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener()
		{
			@Override public void onClick(DialogInterface dialog, int which)
			{
				if (onDialogCancelled != null)
				{
					onDialogCancelled.onDialogCancelled();
				}
			}
		});

		final Dialog show = dialog.create();
		show.setOnShowListener(new DialogInterface.OnShowListener()
		{
			@Override public void onShow(DialogInterface dialog)
			{
				((AlertDialog)show).getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);
				((AlertDialog)show).getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener()
				{
					@Override public void onClick(View view)
					{
						if (onDialogConfirmed != null && !TextUtils.isEmpty(input.getText()))
						{
							onDialogConfirmed.onDialogConfirmed(dialog, input.getText().toString());
						}
					}
				});
				input.addTextChangedListener(new TextWatcher()
				{
					@Override public void beforeTextChanged(CharSequence s, int start, int count, int after){}
					@Override public void onTextChanged(CharSequence s, int start, int before, int count){}

					@Override public void afterTextChanged(Editable s)
					{
						((AlertDialog)show).getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(s.length() > 0);
					}
				});

			}
		});

		return show;
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
