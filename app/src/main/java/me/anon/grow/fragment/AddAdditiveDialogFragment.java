package me.anon.grow.fragment;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import me.anon.grow.R;
import me.anon.lib.Unit;
import me.anon.lib.Views;
import me.anon.model.Additive;

/**
 * // TODO: Add class description
 *
 * @author 7LPdWcaW
 * @documentation // TODO Reference flow doc
 * @project GrowTracker
 */
@Views.Injectable
public class AddAdditiveDialogFragment extends DialogFragment
{
	public interface OnAdditiveSelectedListener
	{
		public void onAdditiveSelected(Additive additive);
		public void onAdditiveDeleteRequested(Additive additive);
	}

	private Additive additive;
	@Views.InjectView(R.id.description) private TextView description;
	@Views.InjectView(R.id.amount) private TextView amount;
	private OnAdditiveSelectedListener onAdditiveSelectedListener;

	public void setOnAdditiveSelectedListener(OnAdditiveSelectedListener onAdditiveSelectedListener)
	{
		this.onAdditiveSelectedListener = onAdditiveSelectedListener;
	}

	@SuppressLint("ValidFragment")
	public AddAdditiveDialogFragment(Additive additive)
	{
		this.additive = additive;
	}

	@SuppressLint("ValidFragment")
	public AddAdditiveDialogFragment()
	{
	}

	@Override public Dialog onCreateDialog(Bundle savedInstanceState)
	{
		View view = getActivity().getLayoutInflater().inflate(R.layout.additives_dialog_view, null, false);
		Views.inject(this, view);

		final Unit selectedUnit = Unit.getSelectedMeasurementUnit(getActivity());

		amount.setHint(selectedUnit.getLabel());

		if (additive != null)
		{
			double converted = Unit.ML.to(selectedUnit, additive.getAmount());
			String amountStr = converted == Math.floor(converted) ? String.valueOf((int)converted) : String.valueOf(converted);

			description.setText(additive.getDescription());
			amount.setText(amountStr);
		}

		final AlertDialog dialog = new AlertDialog.Builder(getActivity())
			.setTitle("Additive")
			.setView(view)
			.setPositiveButton("Ok", null)
			.setNeutralButton("Delete", new DialogInterface.OnClickListener()
			{
				@Override public void onClick(DialogInterface dialog, int which)
				{
					if (onAdditiveSelectedListener != null)
					{
						onAdditiveSelectedListener.onAdditiveDeleteRequested(additive);
					}
				}
			})
			.setNegativeButton("Cancel", new DialogInterface.OnClickListener()
			{
				public void onClick(DialogInterface dialog, int whichButton)
				{
					dialog.dismiss();
				}
			}).create();

		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

		dialog.setOnShowListener(new DialogInterface.OnShowListener()
		{
			@Override public void onShow(DialogInterface dialogInterface)
			{
				if (additive == null)
				{
					InputMethodManager systemService = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
					systemService.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
					description.requestFocus();
				}

				dialog.getButton(Dialog.BUTTON_NEUTRAL).setVisibility(additive == null ? View.GONE : View.VISIBLE);
				dialog.getButton(Dialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener()
				{
					@Override public void onClick(View v)
					{
						description.setError(null);

						Additive additive = new Additive();

						String desc = TextUtils.isEmpty(description.getText()) ? null : description.getText().toString();
						Double amt = TextUtils.isEmpty(amount.getText()) ? 0 : Double.valueOf(amount.getText().toString());

						if (desc == null)
						{
							description.setError("Field is required");
							return;
						}

						additive.setDescription(desc);
						additive.setAmount(selectedUnit.to(Unit.ML, amt));

						if (onAdditiveSelectedListener != null)
						{
							onAdditiveSelectedListener.onAdditiveSelected(additive);
						}

						dialog.dismiss();
					}
				});
			}
		});

		return dialog;
	}
}
