package me.anon.grow.fragment;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import lombok.Setter;
import me.anon.grow.R;
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
	}

	private Additive additive;
	@Views.InjectView(R.id.description) private TextView description;
	@Views.InjectView(R.id.amount) private TextView amount;
	@Setter private OnAdditiveSelectedListener onAddNutrientListener;

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
		View view = getActivity().getLayoutInflater().inflate(R.layout.nutrient_dialog_view, null, false);
		Views.inject(this, view);

		if (additive != null)
		{
			description.setText(additive.getDescription());
			amount.setText(String.valueOf(additive.getAmount()));
		}

		return new AlertDialog.Builder(getActivity())
			.setTitle("Additive")
			.setView(view)
			.setPositiveButton("Ok", new DialogInterface.OnClickListener()
			{
				public void onClick(DialogInterface dialog, int whichButton)
				{
					Additive additive = new Additive();

					String desc = TextUtils.isEmpty(description.getText()) ? null : description.getText().toString();
					Integer amt = TextUtils.isEmpty(amount.getText()) ? null : Integer.valueOf(amount.getText().toString());

					additive.setDescription(desc);
					additive.setAmount(amt);

					if (onAddNutrientListener != null)
					{
						onAddNutrientListener.onAdditiveSelected(additive);
					}
				}
			})
			.setNeutralButton("Clear", new DialogInterface.OnClickListener()
			{
				@Override public void onClick(DialogInterface dialog, int which)
				{
					if (onAddNutrientListener != null)
					{
						onAddNutrientListener.onAdditiveSelected(null);
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
	}
}
