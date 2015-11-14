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
import me.anon.model.Nutrient;

/**
 * // TODO: Add class description
 *
 * @author 7LPdWcaW
 * @documentation // TODO Reference flow doc
 * @project GrowTracker
 */
@Views.Injectable
public class AddNutrientDialogFragment extends DialogFragment
{
	public interface OnAddNutrientListener
	{
		public void onNutrientSelected(Nutrient nutrient);
	}

	private Nutrient nutrient;
	@Views.InjectView(R.id.n) private TextView n;
	@Views.InjectView(R.id.p) private TextView p;
	@Views.InjectView(R.id.k) private TextView k;
	@Views.InjectView(R.id.ca) private TextView ca;
	@Views.InjectView(R.id.s) private TextView s;
	@Views.InjectView(R.id.mg) private TextView mg;
	@Setter private OnAddNutrientListener onAddNutrientListener;

	@SuppressLint("ValidFragment")
	public AddNutrientDialogFragment(Nutrient nutrient)
	{
		this.nutrient = nutrient;
	}

	@SuppressLint("ValidFragment")
	public AddNutrientDialogFragment()
	{
	}

	@Override public Dialog onCreateDialog(Bundle savedInstanceState)
	{
		View view = getActivity().getLayoutInflater().inflate(R.layout.nutrient_dialog_view, null, false);
		Views.inject(this, view);

		if (nutrient != null)
		{
			n.setText(nutrient.getNpc() == null ? "" : String.valueOf(nutrient.getNpc()));
			p.setText(nutrient.getPpc() == null ? "" : String.valueOf(nutrient.getPpc()));
			k.setText(nutrient.getKpc() == null ? "" : String.valueOf(nutrient.getKpc()));
			s.setText(nutrient.getSpc() == null ? "" : String.valueOf(nutrient.getSpc()));
			ca.setText(nutrient.getCapc() == null ? "" : String.valueOf(nutrient.getCapc()));
			mg.setText(nutrient.getMgpc() == null ? "" : String.valueOf(nutrient.getMgpc()));
		}

		return new AlertDialog.Builder(getActivity())
			.setTitle("Nutrient")
			.setView(view)
			.setPositiveButton("Ok", new DialogInterface.OnClickListener()
			{
				public void onClick(DialogInterface dialog, int whichButton)
				{
					Nutrient nutrient = new Nutrient();

					Double npc = TextUtils.isEmpty(n.getText()) ? null : Double.valueOf(n.getText().toString());
					Double ppc = TextUtils.isEmpty(p.getText()) ? null : Double.valueOf(p.getText().toString());
					Double kpc = TextUtils.isEmpty(k.getText()) ? null : Double.valueOf(k.getText().toString());
					Double spc = TextUtils.isEmpty(s.getText()) ? null : Double.valueOf(s.getText().toString());
					Double capc = TextUtils.isEmpty(ca.getText()) ? null : Double.valueOf(ca.getText().toString());
					Double mgpc = TextUtils.isEmpty(mg.getText()) ? null : Double.valueOf(mg.getText().toString());

					nutrient.setNpc(npc);
					nutrient.setPpc(ppc);
					nutrient.setKpc(kpc);
					nutrient.setSpc(spc);
					nutrient.setCapc(capc);
					nutrient.setMgpc(mgpc);

					if (onAddNutrientListener != null)
					{
						onAddNutrientListener.onNutrientSelected(nutrient);
					}
				}
			})
			.setNeutralButton("Clear", new DialogInterface.OnClickListener()
			{
				@Override public void onClick(DialogInterface dialog, int which)
				{
					if (onAddNutrientListener != null)
					{
						onAddNutrientListener.onNutrientSelected(null);
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
