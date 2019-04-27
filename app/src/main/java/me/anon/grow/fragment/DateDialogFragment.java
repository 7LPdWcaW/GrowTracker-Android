package me.anon.grow.fragment;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.Fragment;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.DatePicker;
import android.widget.TimePicker;

import java.util.Calendar;

public class DateDialogFragment extends Fragment
{
	private long time;

	public static interface OnDateSelectedListener
	{
		public void onDateSelected(Calendar date);
		public void onCancelled();
	}

	private OnDateSelectedListener onDateSelected;

	public void setOnDateSelected(OnDateSelectedListener onDateSelected)
	{
		this.onDateSelected = onDateSelected;
	}

	@SuppressLint("ValidFragment")
	public DateDialogFragment(){}

	@SuppressLint("ValidFragment")
	public DateDialogFragment(long time)
	{
		this.time = time;
	}

	@Override public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);

		final Calendar date = Calendar.getInstance();
		date.setTimeInMillis(time);

		DatePickerDialog dialog = new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener()
		{
			@Override public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth)
			{
				date.set(year, monthOfYear, dayOfMonth);

				TimePickerDialog dialog = new TimePickerDialog(getActivity(), new TimePickerDialog.OnTimeSetListener()
				{
					@Override public void onTimeSet(TimePicker view, int hourOfDay, int minute)
					{
						date.set(Calendar.HOUR_OF_DAY, hourOfDay);
						date.set(Calendar.MINUTE, minute);

						if (onDateSelected != null)
						{
							onDateSelected.onDateSelected(date);
						}
					}
				}, date.get(Calendar.HOUR_OF_DAY), date.get(Calendar.MINUTE), true);
				dialog.setOnDismissListener(new DialogInterface.OnDismissListener()
				{
					@Override public void onDismiss(DialogInterface dialog)
					{
						if (onDateSelected != null)
						{
							onDateSelected.onCancelled();
						}
					}
				});
				dialog.show();
			}
		}, date.get(Calendar.YEAR), date.get(Calendar.MONTH), date.get(Calendar.DAY_OF_MONTH));
		dialog.setOnDismissListener(new DialogInterface.OnDismissListener()
		{
			@Override public void onDismiss(DialogInterface dialog)
			{
				if (onDateSelected != null)
				{
					onDateSelected.onCancelled();
				}
			}
		});
		dialog.setOnCancelListener(new DialogInterface.OnCancelListener()
		{
			@Override public void onCancel(DialogInterface dialog)
			{
				if (onDateSelected != null)
				{
					onDateSelected.onCancelled();
				}
			}
		});
		dialog.show();
	}
}
